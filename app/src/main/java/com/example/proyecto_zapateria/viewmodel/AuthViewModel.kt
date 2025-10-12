package com.example.proyecto_zapateria.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_zapateria.domain.validation.validateConfirm
import com.example.proyecto_zapateria.domain.validation.validateEmail
import com.example.proyecto_zapateria.domain.validation.validateNameLettersOnly
import com.example.proyecto_zapateria.domain.validation.validatePhoneDigitsOnly
import com.example.proyecto_zapateria.domain.validation.validateStrongPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

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

// coleccion de usuarios simulada

private data class DemoUser(
    val name: String,
    val email: String,
    val phone: String,
    val pass: String
)

class AuthViewModel : ViewModel() {

    // Simulamos una coleccion de usuarios
    companion object {
        private val demoUsers = mutableListOf(
            DemoUser("Juan Perez", "a@duoc.cl", "123456789", "Pp!1"),

            )
}
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

            val ok = user != null && user.pass == s.pass

            _login.update {
                it.copy(
                    isLoading = false,
                    success = ok,
                    errorMsg = if (ok) null else "Email o contraseña inválidos"
                )
            }
        }
    }

    fun clearLoginResult(){
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // Handlers de cambios en formularios registro
    fun onRegisterNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() } // solo letras y espacios
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() } // solo dígitos
        _register.update {
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update {
            it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmPassError = validateConfirm(it.pass,it.confirm)) }
        recomputeRegisterCanSubmit()
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
