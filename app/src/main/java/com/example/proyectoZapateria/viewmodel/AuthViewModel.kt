package com.example.proyectoZapateria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioConPersonaYRol
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.ClienteRemoteRepository
import com.example.proyectoZapateria.data.repository.ClienteRepository
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository
import com.example.proyectoZapateria.domain.validation.validateConfirm
import com.example.proyectoZapateria.domain.validation.validateEmail
import com.example.proyectoZapateria.domain.validation.validateNameLettersOnly
import com.example.proyectoZapateria.domain.validation.validatePhoneDigitsOnly
import com.example.proyectoZapateria.domain.validation.validateStrongPassword
import com.example.proyectoZapateria.domain.validation.validateStreet
import com.example.proyectoZapateria.domain.validation.validateHouseNumber
import com.example.proyectoZapateria.utils.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

// ViewModel que usa Room con Hilt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val sessionPreferences: SessionPreferences,
    private val clienteRepository: ClienteRepository,
    private val clienteRemoteRepository: ClienteRemoteRepository

) : ViewModel() {

    init {
        // Cargar sesión guardada al iniciar
        cargarSesionGuardada()
    }

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Estado para el usuario logueado - ahora viene del AuthRepository
    val currentUser: StateFlow<UsuarioConPersonaYRol?> = authRepository.currentUser

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
                // Normalizar el input
                val usernameInput = s.email.trim()
                Log.d("AuthViewModel", "submitLogin: intentando login con username='$usernameInput'")

                // Esperar a que la precarga de la DB termine (para evitar falsos negativos tras reinstalar)
                val maxWaitMs = 5000L // Aumentado a 5 segundos
                var waited = 0L
                Log.d("AuthViewModel", "submitLogin: esperando precarga de DB...")
                while (!AppDatabase.preloadComplete.value && waited < maxWaitMs) {
                    delay(100)
                    waited += 100
                }
                Log.d("AuthViewModel", "submitLogin: precarga completada o timeout (waited=${waited}ms)")

                // Intentar encontrar el usuario con reintentos cortos.
                // Esto evita un falso negativo si la precarga de la BD (seed) aún está terminando.
                var usuarioCompleto: UsuarioConPersonaYRol? = null
                val maxAttempts = 5 // Aumentado a 5 intentos
                var attempt = 0
                while (attempt < maxAttempts && usuarioCompleto == null) {
                    attempt++
                    Log.d("AuthViewModel", "submitLogin: intento $attempt de $maxAttempts")

                    // Buscar el usuario por username (primera opción)
                    usuarioCompleto = usuarioRepository.getUsuarioByUsername(usernameInput)
                    Log.d("AuthViewModel", "submitLogin: búsqueda por username result=${usuarioCompleto?.idPersona}")

                    // Si no lo encontramos por username, intentar buscar la persona por email y luego el usuario por id
                    if (usuarioCompleto == null) {
                        val persona = personaRepository.getPersonaByEmail(usernameInput)
                        Log.d("AuthViewModel", "submitLogin: búsqueda por email result persona.id=${persona?.idPersona}")
                        if (persona != null) {
                            usuarioCompleto = usuarioRepository.getUsuarioCompletoById(persona.idPersona)
                            Log.d("AuthViewModel", "submitLogin: búsqueda de usuario completo result=${usuarioCompleto?.idPersona}")
                        }
                    }

                    if (usuarioCompleto == null && attempt < maxAttempts) {
                        // Pequeño retraso antes de reintentar (esperar a que termine la precarga)
                        Log.d("AuthViewModel", "submitLogin: usuario no encontrado, esperando antes de reintentar...")
                        delay(500)
                    }
                }

                if (usuarioCompleto == null) {
                    Log.e("AuthViewModel", "submitLogin: usuario no encontrado después de $maxAttempts intentos")
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Usuario no encontrado. Verifica tu email o espera a que la app termine de cargar"
                        )
                    }
                    return@launch
                }

                Log.d("AuthViewModel", "submitLogin: usuarioCompleto found id=${usuarioCompleto.idPersona} username=${usuarioCompleto.username} rol=${usuarioCompleto.nombreRol}")

                // Verificar si el usuario está activo
                if (usuarioCompleto.estado != "activo") {
                    Log.w("AuthViewModel", "submitLogin: usuario inactivo id=${usuarioCompleto.idPersona}")
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Usuario inactivo. Contacte al administrador"
                        )
                    }
                    return@launch
                }

                // Obtener la persona para verificar la contraseña
                val persona = personaRepository.getPersonaById(usuarioCompleto.idPersona)

                if (persona == null) {
                    Log.e("AuthViewModel", "submitLogin: error al cargar persona para id=${usuarioCompleto.idPersona}")
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Error al cargar datos del usuario"
                        )
                    }
                    return@launch
                }

                // Verificar la contraseña
                var passwordValida = PasswordHasher.checkPassword(s.pass, persona.passHash)
                Log.d("AuthViewModel", "submitLogin: verificación de password inicial result=$passwordValida")

                // Si falla en la primera verificación, reintentar una vez tras un pequeño delay (mitiga condiciones de carrera)
                if (!passwordValida) {
                    try {
                        delay(200)
                        val personaRetry = personaRepository.getPersonaById(usuarioCompleto.idPersona)
                        if (personaRetry != null) {
                            passwordValida = PasswordHasher.checkPassword(s.pass, personaRetry.passHash)
                            Log.d("AuthViewModel", "submitLogin: reintento password check result=$passwordValida for id=${usuarioCompleto.idPersona}")
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "submitLogin: error en reintento de password: ${e.message}")
                    }
                }

                if (passwordValida) {
                    // Guardar el usuario autenticado en el repositorio
                    authRepository.setCurrentUser(usuarioCompleto)

                    // Guardar la sesión en DataStore para persistencia
                    sessionPreferences.saveSession(
                        userId = usuarioCompleto.idPersona,
                        username = usuarioCompleto.username,
                        userRole = usuarioCompleto.nombreRol,
                        userRoleId = usuarioCompleto.idRol
                    )

                    Log.d("AuthViewModel", "submitLogin: login exitoso para id=${usuarioCompleto.idPersona}")
                } else {
                    Log.d("AuthViewModel", "submitLogin: password inválida para id=${usuarioCompleto.idPersona}")
                }

                _login.update {
                    it.copy(
                        isLoading = false,
                        success = passwordValida,
                        errorMsg = if (passwordValida) null else "Usuario o contraseña inválidos"
                    )
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
                val existeEmail = personaRepository.existeEmail(s.email.trim())
                if (existeEmail) {
                    _register.update {
                        it.copy(isLoading = false, success = false, errorMsg = "El email ya está registrado")
                    }
                    return@launch
                }

                val nombreCompleto = s.name.trim().split(" ", limit = 2)
                val nombre = nombreCompleto.getOrNull(0) ?: ""
                val apellido = nombreCompleto.getOrNull(1) ?: ""

                if (nombre.isBlank()) {
                    _register.update {
                        it.copy(isLoading = false, success = false, errorMsg = "Debe ingresar al menos un nombre")
                    }
                    return@launch
                }

                val passHashed = PasswordHasher.hashPassword(s.pass)

                val nuevaPersona = PersonaEntity(
                    nombre = nombre,
                    apellido = apellido,
                    rut = "00000000-0",
                    telefono = s.phone.trim(),
                    email = s.email.trim(),
                    idComuna = null,
                    calle = s.calle.trim(),
                    numeroPuerta = s.numeroPuerta.trim(),
                    username = s.email.trim(),
                    passHash = passHashed,
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                )

                val resultPersona = personaRepository.insertPersona(nuevaPersona)

                if (resultPersona.isFailure) {
                    _register.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = resultPersona.exceptionOrNull()?.message ?: "Error al registrar"
                        )
                    }
                    return@launch
                }

                val idPersona = resultPersona.getOrNull()?.toInt() ?: 0

                val nuevoUsuario = UsuarioEntity(
                    idPersona = idPersona,
                    idRol = 4  // Cliente - Los usuarios que se registran son clientes por defecto
                )

                val resultUsuario = usuarioRepository.insertUsuario(nuevoUsuario)

                if (resultUsuario.isFailure) {
                    _register.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Error al crear usuario: ${resultUsuario.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                // Crear entidad Cliente automáticamente con categoría "Regular"
                try {
                    val nuevoCliente = com.example.proyectoZapateria.data.local.cliente.ClienteEntity(
                        idPersona = idPersona,
                        categoria = "Regular"  // Todos los usuarios nuevos empiezan como "Regular"
                    )
                    clienteRepository.insert(nuevoCliente)
                    Log.d("AuthViewModel", "submitRegister: Cliente creado con categoría Regular para idPersona=$idPersona")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "submitRegister: Error al crear cliente: ${e.message}", e)
                    // No bloqueamos el registro por este error, pero lo registramos
                }

                _register.update {
                    it.copy(isLoading = false, success = true, errorMsg = null)
                }
            } catch (e: Exception) {
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
            sessionPreferences.sessionData.collect { sessionData ->
                if (sessionData != null) {
                    // Cargar el usuario completo desde la base de datos
                    try {
                        // getUsuarioCompletoById usa id_persona como parámetro
                        val usuarioCompleto = usuarioRepository.getUsuarioCompletoById(sessionData.userId)

                        if (usuarioCompleto != null && usuarioCompleto.estado == "activo") {
                            // Restaurar el usuario en el repositorio
                            authRepository.setCurrentUser(usuarioCompleto)
                        } else {
                            // Si el usuario ya no existe o está inactivo, limpiar la sesión
                            sessionPreferences.clearSession()
                        }
                    } catch (_: Exception) {
                        sessionPreferences.clearSession()
                    }
                }
            }
        }
    }
}

