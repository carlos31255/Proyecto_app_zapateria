package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository
import com.example.proyectoZapateria.domain.validation.validateConfirm
import com.example.proyectoZapateria.domain.validation.validateEmail
import com.example.proyectoZapateria.domain.validation.validateNameLettersOnly
import com.example.proyectoZapateria.domain.validation.validatePhoneDigitsOnly
import com.example.proyectoZapateria.domain.validation.validateStrongPassword
import com.example.proyectoZapateria.utils.PasswordHasher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

data class  LoginUiState(
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
    // errores por campo
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmPassError: String? = null,

    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
)

// ViewModel que usa Room en lugar de lista en memoria
class AuthViewModel(
    private val personaRepository: PersonaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    // Estado UI Login
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    // Estado UI Registro
    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Handlers de cambios en formularios login

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    // habilitar botón si no hay errores y campos obligatorios llenos
    private fun recomputeLoginCanSubmit(){
    val s = _login.value
    val can = s.emailError == null &&
                 s.email.isNotBlank() &&
                 s.pass.isNotBlank()
    _login.update { it.copy(canSubmit = can) }
    }
    // Simula envío y verificación de credenciales
    fun submitLogin(){
        val s = _login.value
        if (!s.canSubmit || s.isLoading) return // no hacer nada si no puede
        viewModelScope.launch {
            _login.update { it.copy(isLoading = true, errorMsg = null, success = false) }
            delay(500) // simula tiempo de verificacion

            val user = demoUsers.firstOrNull{ it.email.equals(s.email, ignoreCase = true)}

    // Envío y verificación de credenciales contra base de datos

            _login.update {
        if (!s.canSubmit || s.isLoading) return
                    isLoading = false,
                    success = ok,
            delay(500) // Simula tiempo de verificación

            try {
                // Buscar usuario por email (que es el username en nuestro caso)
                val usuario = usuarioRepository.getUsuarioByUsername(s.email)
                )
                if (usuario == null) {
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Usuario o contraseña inválidos"
                        )
                    }
                    return@launch
                }
        }
                // Verificar que el usuario esté activo
                if (usuario.estado != "activo") {
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
                val persona = personaRepository.getPersonaById(usuario.idPersona)
                if (persona == null) {
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Error al cargar datos del usuario"
                        )
                    }
                    return@launch
                }

                // Verificar contraseña con BCrypt
                val passwordValida = PasswordHasher.checkPassword(s.pass, persona.passHash)

                _login.update {
                    it.copy(
                        isLoading = false,
                        success = passwordValida,
                        errorMsg = if (passwordValida) null else "Usuario o contraseña inválidos"
                    )
                }
            } catch (e: Exception) {
                _login.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMsg = "Error al iniciar sesión: ${e.message}"
                    )
                }
    // Envío y verificación de registro en base de datos
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        if (!s.canSubmit || s.isLoading) return
        recomputeRegisterCanSubmit()
    }
            delay(500) // Simula tiempo de procesamiento
    fun onRegisterEmailChange(value: String) {
            try {
                // Verificar si el email ya existe
                val existeEmail = personaRepository.existeEmail(s.email.trim())
                if (existeEmail) {
                    _register.update {
                        it.copy(isLoading = false, success = false, errorMsg = "El email ya está registrado")
                    }
                    return@launch
        val digitsOnly = value.filter { it.isDigit() } // solo dígitos

                // Separar nombre y apellido (asumiendo formato "Nombre Apellido")
                val nombreCompleto = s.name.trim().split(" ", limit = 2)
                val nombre = nombreCompleto.getOrNull(0) ?: ""
                val apellido = nombreCompleto.getOrNull(1) ?: ""

                if (nombre.isBlank()) {
                    _register.update {
                        it.copy(isLoading = false, success = false, errorMsg = "Debe ingresar al menos un nombre")
                    }
                    return@launch
                }

                // Hash de la contraseña
                val passHashed = PasswordHasher.hashPassword(s.pass)

                // Crear PersonaEntity
                val nuevaPersona = PersonaEntity(
                    nombre = nombre,
                    apellido = apellido,
                    rut = "00000000", // TODO: Solicitar RUT real en el formulario
                    dv = "0",
                    telefono = s.phone.trim(),
                    email = s.email.trim(),
                    idComuna = null, // TODO: Solicitar comuna en el formulario
                    calle = null,
                    numeroPuerta = null,
                    username = s.email.trim(), // Usamos el email como username
                    passHash = passHashed,
                    fechaRegistro = Instant.now(),
                    estado = "activo"
                )

                // Insertar persona en la base de datos
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

                // Crear UsuarioEntity con rol por defecto (asumiendo rol cliente = 3)
                val nuevoUsuario = UsuarioEntity(
                    idPersona = idPersona,
                    idRol = 3 // TODO: Definir constante para rol cliente
                )

                // Insertar usuario
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

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmPassError = validateConfirm(it.pass,value)) }
        recomputeRegisterCanSubmit()
    }
    // habilitar botón si no hay errores y campos obligatorios llenos
    private fun recomputeRegisterCanSubmit(){
        val s = _register.value
        val noErrors = listOf(s.nameError,s.emailError,s.phoneError,s.passError,s.confirmPassError).all { it == null } // sin errores
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() // todos llenos
        _register.update { it.copy(canSubmit = noErrors && filled) } // actualizar el flag
    }
    // Simula envío y verificación de datos
    fun submitRegister(){
        val s = _register.value
        if (!s.canSubmit || s.isLoading) return // no hacer nada si no puede
        viewModelScope.launch {
            _register.update { it.copy(isLoading = true, errorMsg = null, success = false) }
            delay(500) // simula tiempo de verificacion

            val exists = demoUsers.any{ it.email.equals(s.email, ignoreCase = true)}

            if (exists){
                _register.update {
                    it.copy(isLoading = false, success = false, errorMsg = "El email ya está registrado")
                }
                return@launch
            }

            demoUsers.add(
                DemoUser(s.name.trim(),
                    s.email.trim(),
                    s.phone.trim(),
                    s.pass
                )
            )
            _register.update { it.copy(isLoading = false, success = true, errorMsg = null)
            }
        }
    }
    fun clearRegisterResult(){
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}
