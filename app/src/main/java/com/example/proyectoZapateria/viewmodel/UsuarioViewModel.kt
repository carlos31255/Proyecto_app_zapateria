package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.rol.RolEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioConPersonaYRol
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.TransportistaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository
import com.example.proyectoZapateria.utils.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearUsuarioState(
    val nombre: String = "",
    val apellido: String = "",
    val rut: String = "",
    val telefono: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rolSeleccionado: RolEntity? = null,

    // Campos específicos para transportista
    val licencia: String = "",
    val vehiculo: String = "",

    val nombreError: String? = null,
    val apellidoError: String? = null,
    val rutError: String? = null,
    val telefonoError: String? = null,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val rolError: String? = null,
    val licenciaError: String? = null,
    val vehiculoError: String? = null,

    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val personaRepository: PersonaRepository,
    private val rolRepository: com.example.proyectoZapateria.data.repository.RolRepository,
    private val clienteRepository: com.example.proyectoZapateria.data.repository.ClienteRepository,
    private val transportistaRepository: TransportistaRepository
) : ViewModel() {

    // Lista de todos los usuarios
    val usuarios: StateFlow<List<UsuarioConPersonaYRol>> = usuarioRepository.getAllConPersonaYRol()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Roles disponibles
    private val _roles = MutableStateFlow<List<RolEntity>>(emptyList())
    val roles: StateFlow<List<RolEntity>> = _roles.asStateFlow()

    // Estado de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Usuarios filtrados
    private val _usuariosFiltrados = MutableStateFlow<List<UsuarioConPersonaYRol>>(emptyList())
    val usuariosFiltrados: StateFlow<List<UsuarioConPersonaYRol>> = _usuariosFiltrados.asStateFlow()

    // Estado de creación de usuario
    private val _crearUsuarioState = MutableStateFlow(CrearUsuarioState())
    val crearUsuarioState: StateFlow<CrearUsuarioState> = _crearUsuarioState.asStateFlow()

    // Estado de carga general
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensaje de error general
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mensaje de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        cargarRoles()
        observarUsuarios()
    }

    private fun observarUsuarios() {
        viewModelScope.launch {
            usuarios.collect { listaUsuarios ->
                _usuariosFiltrados.value = filtrarUsuarios(listaUsuarios, _searchQuery.value)
                _isLoading.value = false
            }
        }
    }

    private fun cargarRoles() {
        viewModelScope.launch {
            try {
                rolRepository.getAllRoles().collect { listaRoles ->
                    _roles.value = listaRoles
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar roles: ${e.message}"
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        _usuariosFiltrados.value = filtrarUsuarios(usuarios.value, query)
    }

    private fun filtrarUsuarios(usuarios: List<UsuarioConPersonaYRol>, query: String): List<UsuarioConPersonaYRol> {
        if (query.isBlank()) return usuarios

        return usuarios.filter { usuario ->
            usuario.nombre.contains(query, ignoreCase = true) ||
            usuario.apellido.contains(query, ignoreCase = true) ||
            usuario.rut.contains(query, ignoreCase = true) ||
            usuario.email?.contains(query, ignoreCase = true) == true ||
            usuario.username.contains(query, ignoreCase = true) ||
            usuario.nombreRol.contains(query, ignoreCase = true)
        }
    }

    // Funciones para actualizar el estado de creación
    fun actualizarNombre(nombre: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            nombre = nombre,
            nombreError = null
        )
    }

    fun actualizarApellido(apellido: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            apellido = apellido,
            apellidoError = null
        )
    }

    fun actualizarRut(rut: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            rut = rut,
            rutError = null
        )
    }

    fun actualizarTelefono(telefono: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            telefono = telefono,
            telefonoError = null
        )
    }

    fun actualizarEmail(email: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun actualizarUsername(username: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun actualizarPassword(password: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun actualizarConfirmPassword(confirmPassword: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun actualizarRolSeleccionado(rol: RolEntity?) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            rolSeleccionado = rol,
            rolError = null
        )
    }

    fun actualizarLicencia(licencia: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            licencia = licencia,
            licenciaError = null
        )
    }

    fun actualizarVehiculo(vehiculo: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            vehiculo = vehiculo,
            vehiculoError = null
        )
    }

    fun crearUsuario() {
        viewModelScope.launch {
            val state = _crearUsuarioState.value

            // Validaciones usando UserFormValidation
            var hayErrores = false
            var updatedState = state

            // Validar nombre (solo letras)
            val nombreError = com.example.proyectoZapateria.domain.validation.validateNameLettersOnly(state.nombre)
            if (nombreError != null) {
                updatedState = updatedState.copy(nombreError = nombreError)
                hayErrores = true
            }

            // Validar apellido (solo letras)
            val apellidoError = com.example.proyectoZapateria.domain.validation.validateNameLettersOnly(state.apellido)
            if (apellidoError != null) {
                updatedState = updatedState.copy(apellidoError = apellidoError)
                hayErrores = true
            }

            // Validar RUT (básico - requerido)
            if (state.rut.isBlank()) {
                updatedState = updatedState.copy(rutError = "El RUT es requerido")
                hayErrores = true
            }

            // Validar teléfono (solo si no está vacío)
            if (state.telefono.isNotBlank()) {
                val telefonoError = com.example.proyectoZapateria.domain.validation.validatePhoneDigitsOnly(state.telefono)
                if (telefonoError != null) {
                    updatedState = updatedState.copy(telefonoError = telefonoError)
                    hayErrores = true
                }
            }

            // Validar email
            val emailError = com.example.proyectoZapateria.domain.validation.validateEmail(state.email)
            if (emailError != null) {
                updatedState = updatedState.copy(emailError = emailError)
                hayErrores = true
            }

            // Validar username (requerido)
            if (state.username.isBlank()) {
                updatedState = updatedState.copy(usernameError = "El username es requerido")
                hayErrores = true
            }

            // Validar contraseña fuerte
            val passwordError = com.example.proyectoZapateria.domain.validation.validateStrongPassword(state.password)
            if (passwordError != null) {
                updatedState = updatedState.copy(passwordError = passwordError)
                hayErrores = true
            }

            // Validar confirmación de contraseña
            val confirmError = com.example.proyectoZapateria.domain.validation.validateConfirm(state.password, state.confirmPassword)
            if (confirmError != null) {
                updatedState = updatedState.copy(confirmPasswordError = confirmError)
                hayErrores = true
            }

            // Validar rol seleccionado
            if (state.rolSeleccionado == null) {
                updatedState = updatedState.copy(rolError = "Debe seleccionar un rol")
                hayErrores = true
            }

            // Validar campos específicos para Transportista (idRol = 3)
            if (state.rolSeleccionado?.idRol == 3) {
                if (state.licencia.isBlank()) {
                    updatedState = updatedState.copy(licenciaError = "La licencia es requerida para transportistas")
                    hayErrores = true
                }
                if (state.vehiculo.isBlank()) {
                    updatedState = updatedState.copy(vehiculoError = "El vehículo es requerido para transportistas")
                    hayErrores = true
                }
            }

            // Actualizar estado con todos los errores
            _crearUsuarioState.value = updatedState

            if (hayErrores) return@launch

            _crearUsuarioState.value = _crearUsuarioState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Crear persona
                val persona = PersonaEntity(
                    idPersona = 0,
                    nombre = state.nombre,
                    apellido = state.apellido,
                    rut = state.rut,
                    telefono = state.telefono.ifBlank { null },
                    email = state.email.ifBlank { null },
                    idComuna = null,
                    calle = null,
                    numeroPuerta = null,
                    username = state.username,
                    passHash = PasswordHasher.hashPassword(state.password),
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                )

                val personaId = personaRepository.insert(persona)

                // Crear usuario
                val usuario = UsuarioEntity(
                    idPersona = personaId.toInt(),
                    idRol = state.rolSeleccionado!!.idRol
                )

                usuarioRepository.insert(usuario)

                // Si el rol es Cliente (idRol = 4), crear también el ClienteEntity
                if (state.rolSeleccionado.idRol == 4) {
                    val cliente = ClienteEntity(
                        idPersona = personaId.toInt(),
                        categoria = "regular" // Categoría por defecto para nuevos clientes
                    )
                    clienteRepository.insert(cliente)
                }

                // Si el rol es Transportista (idRol = 3), crear también el TransportistaEntity
                if (state.rolSeleccionado.idRol == 3) {
                    val transportista = TransportistaEntity(
                        idPersona = personaId.toInt(),
                        licencia = state.licencia.ifBlank { null },
                        vehiculo = state.vehiculo.ifBlank { null }
                    )
                    transportistaRepository.insertTransportista(transportista)
                }

                _successMessage.value = "Usuario creado exitosamente"
                _crearUsuarioState.value = CrearUsuarioState(success = true)

            } catch (e: Exception) {
                _crearUsuarioState.value = _crearUsuarioState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear usuario: ${e.message}"
                )
            }
        }
    }

    fun eliminarUsuario(usuario: UsuarioConPersonaYRol) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // En lugar de eliminar, desactivar el usuario cambiando su estado
                val persona = personaRepository.getById(usuario.idPersona)
                if (persona != null) {
                    val personaDesactivada = persona.copy(estado = "inactivo")
                    personaRepository.update(personaDesactivada)
                    _successMessage.value = "Usuario desactivado exitosamente"
                } else {
                    _errorMessage.value = "No se encontró el usuario"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error al desactivar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarMensajes() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun resetearFormulario() {
        _crearUsuarioState.value = CrearUsuarioState()
    }
}

