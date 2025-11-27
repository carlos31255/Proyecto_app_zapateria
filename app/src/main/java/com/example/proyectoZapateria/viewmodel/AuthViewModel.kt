package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.model.UsuarioCompleto
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.GeografiaRemoteRepository
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
    val rut: String = "",
    val pass: String = "",
    val confirm: String = "",
    val calle: String = "",
    val numeroPuerta: String = "",
    val idRegion: Long? = null,
    val idCiudad: Long? = null,
    val idComuna: Long? = null,

    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val rutError: String? = null,
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
    private val authRemoteRepository: AuthRemoteRepository,
    private val sessionPreferences: SessionPreferences,
    private val clienteRemoteRepository: ClienteRemoteRepository,
    private val geografiaRemoteRepository: GeografiaRemoteRepository
) : ViewModel() {

    // Estado de UI para login y registro
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Estado para el usuario logueado - ahora usa UsuarioCompleto
    val currentUser: StateFlow<UsuarioCompleto?> = authRemoteRepository.currentUser

    // Flag público que indica si se está restaurando la sesión al arrancar
    private val _isRestoringSession = MutableStateFlow(true)
    val isRestoringSession: StateFlow<Boolean> = _isRestoringSession

    // Mensaje de error global al iniciar la app (por ejemplo: microservicio inaccesible)
    private val _startupError = MutableStateFlow<String?>(null)
    val startupError: StateFlow<String?> = _startupError

    // Geografía: regiones/ciudades/comunas para el formulario de registro
    private val _regiones = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>>(emptyList())
    val regiones: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>> = _regiones

    private val _ciudades = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>>(emptyList())
    val ciudades: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>> = _ciudades

    private val _comunas = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>(emptyList())
    val comunas: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>> = _comunas

    // Estados de carga para la geografía
    private val _loadingRegiones = MutableStateFlow(false)
    val loadingRegiones: StateFlow<Boolean> = _loadingRegiones

    private val _loadingCiudades = MutableStateFlow(false)
    val loadingCiudades: StateFlow<Boolean> = _loadingCiudades

    private val _loadingComunas = MutableStateFlow(false)
    val loadingComunas: StateFlow<Boolean> = _loadingComunas

    // Ejecutar la restauración de sesión después de inicializar los StateFlows
    init {
        cargarSesionGuardada()

        // Cargar regiones al inicio (opcional: UI puede llamar explícitamente)
        viewModelScope.launch {
            loadRegiones()
        }
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

                val result = authRemoteRepository.login(usernameInput, s.pass)

                result.onSuccess { usuarioCompleto ->
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val maxAttempts = 3
                            var attempt = 0
                            var ok = false
                            while (attempt < maxAttempts && !ok) {
                                attempt++
                                val saved = try {
                                    withTimeoutOrNull(2000L) {
                                        sessionPreferences.saveSession(
                                            userId = usuarioCompleto.idPersona,
                                            username = usuarioCompleto.username,
                                            userRole = usuarioCompleto.nombreRol,
                                            userRoleId = usuarioCompleto.idRol
                                        )
                                        true
                                    }
                                } catch (_: Exception) {
                                    null
                                }
                                if (saved == true) ok = true else {
                                    delay(300L * attempt)
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = true,
                            errorMsg = null
                        )
                    }
                }.onFailure { error ->
                    val msg = mapErrorToUserMessage(error)
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = msg
                        )
                    }
                }
            } catch (e: Exception) {
                _login.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMsg = mapErrorToUserMessage(e)
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
        // Permitir + solo al inicio, seguido de dígitos, con límite de 15 caracteres
        val filtered = if (value.startsWith("+")) {
            "+" + value.substring(1).filter { it.isDigit() }
        } else {
            value.filter { it.isDigit() }
        }

        // Aplicar límite de 15 caracteres
        val limited = if (filtered.length > 15) filtered.take(15) else filtered

        _register.update { it.copy(phone = limited, phoneError = validatePhoneDigitsOnly(limited)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterRutChange(value: String) {
        // Formato RUT: 12345678-9 (permitir solo dígitos y guión)
        val filtered = value.filter { it.isDigit() || it == '-' }

        // Validar formato, pero solo si el valor no está vacío
        val rutError = if (filtered.isNotBlank() && !filtered.matches(Regex("^\\d{7,8}-[\\dkK]$"))) {
            "RUT inválido (Ej: 12345678-9)"
        } else null

        // Siempre limpiar el error al cambiar el valor (incluyendo errores del servidor)
        _register.update { it.copy(rut = filtered, rutError = rutError) }
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

    fun onSelectRegion(regionId: Long?) {
        // set region and clear ciudades/comunas selection
        _register.update { it.copy(idRegion = regionId, idCiudad = null, idComuna = null) }
        if (regionId != null) {
            loadCiudadesPorRegion(regionId)
        } else {
            // limpiar listas si se quita la región
            clearCiudades()
            clearComunas()
        }
    }

    fun onSelectCiudad(ciudadId: Long?) {
        _register.update { it.copy(idCiudad = ciudadId, idComuna = null) }
        if (ciudadId != null) {
            loadComunasPorCiudad(ciudadId)
        } else {
            // limpiar comunas si se quita la ciudad
            clearComunas()
        }
    }

    fun onSelectComuna(comunaId: Long?) {
        _register.update { it.copy(idComuna = comunaId) }
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

                val result = authRemoteRepository.register(
                    nombre = nombre,
                    apellido = apellido,
                    email = s.email.trim(),
                    telefono = s.phone.trim(),
                    rut = s.rut.trim().ifBlank { null },
                    password = s.pass,
                    idComuna = s.idComuna,
                    calle = s.calle.trim(),
                    numeroPuerta = s.numeroPuerta.trim()
                )

                result.onSuccess { usuarioCompleto ->
                    try {
                        val clienteDTO = com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO(
                            idPersona = usuarioCompleto.idPersona,
                            categoria = "Regular",
                            nombreCompleto = "${usuarioCompleto.nombre} ${usuarioCompleto.apellido}",
                            email = usuarioCompleto.email,
                            telefono = usuarioCompleto.telefono,
                            activo = true
                        )
                        clienteRemoteRepository.crearCliente(clienteDTO)
                    } catch (_: Exception) {
                    }

                    _register.update {
                        it.copy(isLoading = false, success = true, errorMsg = null)
                    }
                }.onFailure { error ->
                    _register.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = error.message ?: "Error al registrar"
                        )
                    }
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
            authRemoteRepository.logout()
            sessionPreferences.clearSession()
            _login.value = LoginUiState(
                email = "",
                pass = "",
                emailError = null,
                passError = null,
                isLoading = false,
                canSubmit = false,
                success = false,
                errorMsg = null
            )
            _register.value = RegisterUiState()
        }
    }

    // FUNCIÓN TEMPORAL PARA PROBAR API
    fun testConexionMicroservicio() {
        viewModelScope.launch {
            _login.value = _login.value.copy(
                isLoading = true,
                errorMsg = null
            )

            val result = clienteRemoteRepository.obtenerTodosLosClientes()

            result.onSuccess { clientes ->
                _login.value = _login.value.copy(
                    isLoading = false,
                    errorMsg = "✅ Conexión exitosa: ${clientes.size} clientes encontrados"
                )
            }.onFailure { error ->
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
            try {
                _isRestoringSession.value = true
            } catch (_: Exception) {
            }
            try {
                val sessionData = withContext(Dispatchers.IO) { sessionPreferences.sessionData.first() }

                if (sessionData != null) {
                    try {
                        val maxAttempts = 5
                        var attempt = 0
                        var result: kotlin.Result<com.example.proyectoZapateria.data.model.UsuarioCompleto>? = null

                        while (attempt < maxAttempts) {
                            attempt++
                            try {
                                result = withContext(Dispatchers.IO) {
                                    try {
                                        withTimeoutOrNull(3000L) { authRemoteRepository.obtenerUsuarioPorId(sessionData.userId) }
                                    } catch (_: Exception) { null }
                                }
                                if (result != null) break
                            } catch (_: Exception) {
                            }
                            kotlinx.coroutines.delay(300L * attempt)
                        }

                        if (result != null) {
                            result.onSuccess { usuarioCompleto ->
                                if (usuarioCompleto.estado == "activo" && usuarioCompleto.activo) {
                                    authRemoteRepository.setCurrentUser(usuarioCompleto)
                                } else {
                                    withContext(Dispatchers.IO) { sessionPreferences.clearSession() }
                                }
                            }.onFailure { error ->
                                val userMsg = mapErrorToUserMessage(error)
                                _startupError.value = userMsg
                            }
                        }
                    } catch (e: Exception) {
                        _startupError.value = mapErrorToUserMessage(e)
                    }
                }
            } catch (e: Exception) {
                _startupError.value = mapErrorToUserMessage(e)
            } finally {
                _isRestoringSession.value = false
            }
        }
    }

    // Añadir función auxiliar
     private fun mapErrorToUserMessage(e: Throwable?): String {
         val raw = e?.message ?: ""
         return when {
             raw.contains("JsonReader.setLenient", ignoreCase = true) -> "Error en el servidor (respuesta inesperada)"
             raw.contains("malform", ignoreCase = true) -> "Error en el servidor (respuesta inesperada)"
             raw.contains("Sin conexión", ignoreCase = true) -> "Sin conexión. Verifique su red"
             raw.contains("Timeout de conexión", ignoreCase = true) -> "Tiempo de conexión agotado. Intente de nuevo"
             raw.contains("Respuesta del servidor no es JSON válido", ignoreCase = true) -> "Error en el servidor (respuesta inesperada)"
             raw.contains("Timeout", ignoreCase = true) -> "Tiempo de conexión agotado"
             raw.contains("Credenciales inválidas", ignoreCase = true) -> "Email o contraseña incorrectos"
             raw.contains("Usuario no encontrado", ignoreCase = true) -> "Usuario no encontrado"
             raw.contains("Usuario inactivo", ignoreCase = true) -> "Su cuenta está desactivada"
             raw.contains("Error 401", ignoreCase = true) -> "Email o contraseña incorrectos"
             raw.contains("401", ignoreCase = true) -> "Email o contraseña incorrectos"
             raw.contains("Error 500", ignoreCase = true) -> "Error en el servidor. Intente más tarde"
             raw.contains("Error 404", ignoreCase = true) -> "Usuario no encontrado"
             else -> e?.message ?: "Error desconocido"
         }
     }

    // Limpiar mensaje de error de inicio
    fun clearStartupError() {
        _startupError.value = null
    }

    // Cargar regiones
    fun loadRegiones() {
        viewModelScope.launch {
            _loadingRegiones.value = true
            val res = geografiaRemoteRepository.obtenerTodasLasRegiones()
            res.onSuccess { _regiones.value = it }
            _loadingRegiones.value = false
        }
    }

    // Cargar ciudades por región seleccionada
    fun loadCiudadesPorRegion(regionId: Long) {
        viewModelScope.launch {
            _loadingCiudades.value = true
            val res = geografiaRemoteRepository.obtenerCiudadesPorRegion(regionId)
            res.onSuccess { _ciudades.value = it }
            _loadingCiudades.value = false
        }
    }

    // Cargar comunas por ciudad seleccionada
    fun loadComunasPorCiudad(ciudadId: Long) {
        viewModelScope.launch {
            _loadingComunas.value = true
            val res = geografiaRemoteRepository.obtenerComunasPorCiudad(ciudadId)
            res.onSuccess { _comunas.value = it }
            _loadingComunas.value = false
        }
    }

    // Cargar todas las ciudades (opcional)
    fun loadAllCiudades() {
        viewModelScope.launch {
            val res = geografiaRemoteRepository.obtenerTodasLasCiudades()
            res.onSuccess { _ciudades.value = it }
        }
    }

    // Cargar todas las comunas (opcional)
    fun loadAllComunas() {
        viewModelScope.launch {
            val res = geografiaRemoteRepository.obtenerTodasLasComunas()
            res.onSuccess { _comunas.value = it }
        }
    }

    // Limpiar listas auxiliares
    fun clearCiudades() {
        _ciudades.value = emptyList()
    }

    fun clearComunas() {
        _comunas.value = emptyList()
    }
}
