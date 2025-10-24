package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioConPersonaYRol
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

// ViewModel que usa Room
class AuthViewModel(
    private val personaRepository: PersonaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Estado para el usuario logueado
    private val _currentUser = MutableStateFlow<UsuarioConPersonaYRol?>(null)
    val currentUser: StateFlow<UsuarioConPersonaYRol?> = _currentUser

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
                // Buscar el usuario por username
                val usuarioCompleto = usuarioRepository.getUsuarioByUsername(s.email)

                if (usuarioCompleto == null) {
                    _login.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Usuario o contraseña inválidos"
                        )
                    }
                    return@launch
                }

                // Verificar si el usuario está activo
                if (usuarioCompleto.estado != "activo") {
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
                val passwordValida = PasswordHasher.checkPassword(s.pass, persona.passHash)

                if (passwordValida) {
                    // Guardar el usuario autenticado
                    _currentUser.value = usuarioCompleto
                }

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

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmPassError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
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
                    calle = null,
                    numeroPuerta = null,
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

    // ========== LOGOUT ==========

    fun logout() {
        _currentUser.value = null
        _login.value = LoginUiState()
    }
}

