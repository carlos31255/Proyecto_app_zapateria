package com.example.proyectoZapateria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.model.UsuarioCompleto
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.domain.validation.validateConfirm
import com.example.proyectoZapateria.domain.validation.validateEmail
import com.example.proyectoZapateria.domain.validation.validateNameLettersOnly
import com.example.proyectoZapateria.domain.validation.validatePhoneDigitsOnly
import com.example.proyectoZapateria.domain.validation.validateStrongPassword
import com.example.proyectoZapateria.domain.validation.validateStreet
import com.example.proyectoZapateria.domain.validation.validateHouseNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val calle: String = "",
    val numeroPuerta: String = "",

    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmPassError: String? = null,
    val calleError: String? = null,
    val numeroPuertaError: String? = null,

    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
)

// ViewModel que usa microservicios remotos con Hilt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionPreferences: SessionPreferences,
    private val clienteRemoteRepository: ClienteRemoteRepository
) : ViewModel() {

    // Estado de UI para login y registro
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Estado para el usuario logueado - ahora usa UsuarioCompleto
    val currentUser: StateFlow<UsuarioCompleto?> = authRepository.currentUser

    // Flag público que indica si se está restaurando la sesión al arrancar
    private val _isRestoringSession = MutableStateFlow(true)
    val isRestoringSession: StateFlow<Boolean> = _isRestoringSession

    // Ejecutar la restauración de sesión después de inicializar los StateFlows
    init {
        cargarSesionGuardada()
    }

    // ========== HANDLERS LOGIN ==========


    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isLoading) return

        viewModelScope.launch {
            _login.update { it.copy(isLoading = true, errorMsg = null, success = false) }
            delay(500)

            try {
                val usernameInput = s.email.trim()
                Log.d("AuthViewModel", "submitLogin: intentando login con username='$usernameInput'")

                // Llamar al método login del AuthRepository que usa el microservicio
                val result = authRepository.login(usernameInput, s.pass)

                result.onSuccess { usuarioCompleto ->
                    // Guardar la sesión en DataStore para persistencia
                    sessionPreferences.saveSession(
                        userId = usuarioCompleto.idPersona,
                        username = usuarioCompleto.username,
                        userRole = usuarioCompleto.nombreRol,
                        userRoleId = usuarioCompleto.idRol
                    )

                    Log.d("AuthViewModel", "submitLogin: login exitoso para id=${usuarioCompleto.idPersona}")

                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = true,
                            errorMsg = null
                        )
                    }
                }.onFailure { error ->
                    Log.e("AuthViewModel", "submitLogin: error en login: ${error.message}", error)
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = error.message ?: "Usuario o contraseña inválidos"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "submitLogin: excepción en proceso de login: ${e.message}", e)
                _login.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMsg = "Error al iniciar sesión: ${e.message}"
                    )
                }
            }
        }
    }

    // ========== HANDLERS REGISTRO ==========

    fun onRegisterNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update {
            it.copy(
                pass = value,
                passError = validateStrongPassword(value),
                confirmPassError = if (it.confirm.isNotBlank()) validateConfirm(value, it.confirm) else null
            )
        }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmPassError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    // Dirección handlers
    fun onRegisterCalleChange(value: String) {
        _register.update { it.copy(calle = value, calleError = validateStreet(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterNumeroPuertaChange(value: String) {
        _register.update { it.copy(numeroPuerta = value, numeroPuertaError = validateHouseNumber(value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(
            s.nameError,
            s.emailError,
            s.phoneError,
            s.passError,
            s.confirmPassError,
            s.calleError,
            s.numeroPuertaError
        ).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() && s.calle.isNotBlank() && s.numeroPuerta.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isLoading) return

        viewModelScope.launch {
            _register.update { it.copy(isLoading = true, errorMsg = null, success = false) }
            delay(500)

            try {
                val nombreCompleto = s.name.trim().split(" ", limit = 2)
                val nombre = nombreCompleto.getOrNull(0) ?: ""
                val apellido = nombreCompleto.getOrNull(1) ?: ""

                if (nombre.isBlank()) {
                    _register.update {
                        it.copy(isLoading = false, success = false, errorMsg = "Debe ingresar al menos un nombre")
                    }
                    return@launch
                }

                // Llamar al método register del AuthRepository que usa el microservicio
                val result = authRepository.register(
                    nombre = nombre,
                    apellido = apellido,
                    email = s.email.trim(),
                    telefono = s.phone.trim(),
                    password = s.pass,
                    calle = s.calle.trim(),
                    numeroPuerta = s.numeroPuerta.trim()
                )

                result.onSuccess { usuarioCompleto ->
                    Log.d("AuthViewModel", "submitRegister: Registro exitoso para: ${usuarioCompleto.username}")

                    // Crear el cliente con categoría "Regular"
                    try {
                        val clienteDTO = com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO(
                            idPersona = usuarioCompleto.idPersona,
                            categoria = "Regular",
                            nombreCompleto = "${usuarioCompleto.nombre} ${usuarioCompleto.apellido}",
                            email = usuarioCompleto.email,
                            telefono = usuarioCompleto.telefono,
                            activo = true
                        )
                        val clienteResult = clienteRemoteRepository.crearCliente(clienteDTO)
                        if (clienteResult.isSuccess) {
                            Log.d("AuthViewModel", "submitRegister: Cliente creado con categoría Regular")
                        } else {
                            Log.w("AuthViewModel", "submitRegister: No se pudo crear el cliente: ${clienteResult.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "submitRegister: Error al crear cliente: ${e.message}", e)
                        // No bloqueamos el registro por este error
                    }

                    _register.update {
                        it.copy(isLoading = false, success = true, errorMsg = null)
                    }
                }.onFailure { error ->
                    Log.e("AuthViewModel", "submitRegister: error en registro: ${error.message}", error)
                    _register.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = error.message ?: "Error al registrar"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "submitRegister: excepción en proceso de registro: ${e.message}", e)
                _register.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMsg = "Error al registrar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // Limpiar completamente el formulario de registro
    fun clearRegisterForm() {
        _register.value = RegisterUiState()
    }

    // ========== LOGOUT ==========

    fun logout() {
        viewModelScope.launch {
            // Limpiar sesión del repositorio
            authRepository.logout()

            // Limpiar sesión guardada en DataStore
            sessionPreferences.clearSession()

            // Resetear estado del login
            _login.value = LoginUiState()
        }
    }

    // FUNCIÓN TEMPORAL PARA PROBAR API
    fun testConexionMicroservicio() {
        viewModelScope.launch {
            _login.value = _login.value.copy(
                isLoading = true,
                errorMsg = null
            )

            Log.d("API_TEST", "🔵 Iniciando prueba de conexión con microservicio...")

            val result = clienteRemoteRepository.obtenerTodosLosClientes()

            result.onSuccess { clientes ->
                Log.d("API_TEST", "✅ CONEXIÓN EXITOSA!")
                Log.d("API_TEST", "📊 Clientes encontrados: ${clientes.size}")
                clientes.forEach { cliente ->
                    Log.d("API_TEST", "  - ${cliente.nombreCompleto} (${cliente.email})")
                }

                _login.value = _login.value.copy(
                    isLoading = false,
                    errorMsg = "✅ Conexión exitosa: ${clientes.size} clientes encontrados"
                )
            }.onFailure { error ->
                Log.e("API_TEST", "❌ ERROR DE CONEXIÓN: ${error.message}")
                Log.e("API_TEST", "Stack trace:", error)

                _login.value = _login.value.copy(
                    isLoading = false,
                    errorMsg = "❌ Error: ${error.message}"
                )
            }
        }
    }

    /**
     * Carga la sesión guardada desde DataStore al iniciar la app
     */
    private fun cargarSesionGuardada() {
        viewModelScope.launch {
            // Asegurar que la marca de restauración no vaya a lanzar NPE
            try {
                _isRestoringSession.value = true
            } catch (e: Exception) {
                Log.w("AuthViewModel", "cargarSesionGuardada: _isRestoringSession no inicializado: ${e.message}")
            }
            try {
                // Intentar leer la sessionData una vez (timeout de 5s) en IO
                val sessionData = withTimeoutOrNull(5000) {
                    withContext(Dispatchers.IO) { sessionPreferences.sessionData.first() }
                }

                if (sessionData != null) {
                    // Cargar el usuario completo desde el microservicio
                    try {
                        // Llamada al repositorio remoto en IO con timeout para evitar colgar la restauración
                        val result = withTimeoutOrNull(5000) {
                            withContext(Dispatchers.IO) { authRepository.obtenerUsuarioPorId(sessionData.userId) }
                        }

                        if (result == null) {
                            Log.w("AuthViewModel", "cargarSesionGuardada: timeout al obtener usuario remoto")
                            withContext(Dispatchers.IO) { sessionPreferences.clearSession() }
                        } else {
                            result.onSuccess { usuarioCompleto ->
                                if (usuarioCompleto.estado == "activo" && usuarioCompleto.activo) {
                                    // Restaurar el usuario en el repositorio
                                    authRepository.setCurrentUser(usuarioCompleto)
                                    Log.d("AuthViewModel", "cargarSesionGuardada: Sesión restaurada para: ${usuarioCompleto.username}")
                                } else {
                                    // Si el usuario está inactivo, limpiar la sesión
                                    withContext(Dispatchers.IO) { sessionPreferences.clearSession() }
                                    Log.w("AuthViewModel", "cargarSesionGuardada: Usuario inactivo, sesión limpiada")
                                }
                            }.onFailure { error ->
                                Log.e("AuthViewModel", "cargarSesionGuardada: Error al cargar usuario: ${error.message}")
                                withContext(Dispatchers.IO) { sessionPreferences.clearSession() }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "cargarSesionGuardada: Excepción: ${e.message}", e)
                        withContext(Dispatchers.IO) { sessionPreferences.clearSession() }
                    }
                } else {
                    // No hay sessionData o timeout, no hacemos nada
                    Log.d("AuthViewModel", "cargarSesionGuardada: no hay sessionData o timeout")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "cargarSesionGuardada: excepción general: ${e.message}", e)
            } finally {
                // Marcar que terminamos el intento de restauración (éxito o no)
                try {
                    _isRestoringSession.value = false
                } catch (e: Exception) {
                    Log.w("AuthViewModel", "cargarSesionGuardada: _isRestoringSession no inicializado al finalizar: ${e.message}")
                }
            }
        }
    }
}
