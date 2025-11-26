package com.example.proyectoZapateria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.RolRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.TransportistaRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val rolSeleccionado: RolDTO? = null,

    // Dirección
    val calle: String = "",
    val numeroPuerta: String = "",
    val idRegion: Long? = null,
    val idCiudad: Long? = null,
    val idComuna: Long? = null,

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
    val calleError: String? = null,
    val numeroPuertaError: String? = null,
    val licenciaError: String? = null,
    val vehiculoError: String? = null,

    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val usuarioRemoteRepository: UsuarioRemoteRepository,
    private val personaRemoteRepository: PersonaRemoteRepository,
    private val rolRemoteRepository: RolRemoteRepository,
    private val clienteRemoteRepository: ClienteRemoteRepository,
    private val transportistaRemoteRepository: TransportistaRemoteRepository,
    private val geografiaRemoteRepository: com.example.proyectoZapateria.data.repository.remote.GeografiaRemoteRepository
) : ViewModel() {

    // Lista de todos los usuarios (ahora desde API)
    private val _usuarios = MutableStateFlow<List<UsuarioDTO>>(emptyList())
    val usuarios: StateFlow<List<UsuarioDTO>> = _usuarios.asStateFlow()

    // Roles disponibles (ahora desde API)
    private val _roles = MutableStateFlow<List<RolDTO>>(emptyList())
    val roles: StateFlow<List<RolDTO>> = _roles.asStateFlow()

    // Estado de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Usuarios filtrados
    private val _usuariosFiltrados = MutableStateFlow<List<UsuarioDTO>>(emptyList())
    val usuariosFiltrados: StateFlow<List<UsuarioDTO>> = _usuariosFiltrados.asStateFlow()

    // Estado de creación de usuario
    private val _crearUsuarioState = MutableStateFlow(CrearUsuarioState())
    val crearUsuarioState: StateFlow<CrearUsuarioState> = _crearUsuarioState.asStateFlow()

    // Geografía para dirección
    private val _regiones = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>>(emptyList())
    val regiones: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO>> = _regiones.asStateFlow()

    private val _ciudades = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>>(emptyList())
    val ciudades: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO>> = _ciudades.asStateFlow()

    private val _comunas = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>>(emptyList())
    val comunas: StateFlow<List<com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO>> = _comunas.asStateFlow()

    private val _loadingRegiones = MutableStateFlow(false)
    val loadingRegiones: StateFlow<Boolean> = _loadingRegiones.asStateFlow()

    private val _loadingCiudades = MutableStateFlow(false)
    val loadingCiudades: StateFlow<Boolean> = _loadingCiudades.asStateFlow()

    private val _loadingComunas = MutableStateFlow(false)
    val loadingComunas: StateFlow<Boolean> = _loadingComunas.asStateFlow()

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
        cargarUsuarios()
        cargarRegiones()
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = usuarioRemoteRepository.obtenerTodosLosUsuarios()
                if (result.isSuccess) {
                    val listaUsuarios = result.getOrNull() ?: emptyList()
                    _usuarios.value = listaUsuarios
                    _usuariosFiltrados.value = listaUsuarios
                } else {
                    _errorMessage.value = "Error al cargar usuarios: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar usuarios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun cargarRoles() {
        viewModelScope.launch {
            try {
                val result = rolRemoteRepository.obtenerTodosLosRoles()
                if (result.isSuccess) {
                    _roles.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = "Error al cargar roles: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar roles: ${e.message}"
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        _usuariosFiltrados.value = filtrarUsuarios(_usuarios.value, query)
    }

    private fun filtrarUsuarios(usuarios: List<UsuarioDTO>, query: String): List<UsuarioDTO> {
        if (query.isBlank()) return usuarios

        return usuarios.filter { usuario ->
            usuario.nombreCompleto?.contains(query, ignoreCase = true) == true ||
            usuario.username?.contains(query, ignoreCase = true) == true ||
            usuario.nombreRol?.contains(query, ignoreCase = true) == true
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

    fun actualizarRolSeleccionado(rol: RolDTO?) {
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

    fun actualizarCalle(calle: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            calle = calle,
            calleError = null
        )
    }

    fun actualizarNumeroPuerta(numeroPuerta: String) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            numeroPuerta = numeroPuerta,
            numeroPuertaError = null
        )
    }

    fun seleccionarRegion(regionId: Long?) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            idRegion = regionId,
            idCiudad = null,
            idComuna = null
        )
        if (regionId != null) {
            cargarCiudades(regionId)
        } else {
            _ciudades.value = emptyList()
            _comunas.value = emptyList()
        }
    }

    fun seleccionarCiudad(ciudadId: Long?) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            idCiudad = ciudadId,
            idComuna = null
        )
        if (ciudadId != null) {
            cargarComunas(ciudadId)
        } else {
            _comunas.value = emptyList()
        }
    }

    fun seleccionarComuna(comunaId: Long?) {
        _crearUsuarioState.value = _crearUsuarioState.value.copy(
            idComuna = comunaId
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

            // Validaciones
            var hayErrores = false
            var updatedState = state

            // Validar nombre
            val nombreError = com.example.proyectoZapateria.domain.validation.validateNameLettersOnly(state.nombre)
            if (nombreError != null) {
                updatedState = updatedState.copy(nombreError = nombreError)
                hayErrores = true
            }

            // Validar apellido
            val apellidoError = com.example.proyectoZapateria.domain.validation.validateNameLettersOnly(state.apellido)
            if (apellidoError != null) {
                updatedState = updatedState.copy(apellidoError = apellidoError)
                hayErrores = true
            }

            // Validar RUT
            if (state.rut.isBlank()) {
                updatedState = updatedState.copy(rutError = "El RUT es requerido")
                hayErrores = true
            }

            // Validar teléfono
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

            // Validar username
            if (state.username.isBlank()) {
                updatedState = updatedState.copy(usernameError = "El username es requerido")
                hayErrores = true
            }

            // Validar contraseña
            val passwordError = com.example.proyectoZapateria.domain.validation.validateStrongPassword(state.password)
            if (passwordError != null) {
                updatedState = updatedState.copy(passwordError = passwordError)
                hayErrores = true
            }

            // Validar confirmación
            val confirmError = com.example.proyectoZapateria.domain.validation.validateConfirm(state.password, state.confirmPassword)
            if (confirmError != null) {
                updatedState = updatedState.copy(confirmPasswordError = confirmError)
                hayErrores = true
            }

            // Validar rol
            if (state.rolSeleccionado == null) {
                updatedState = updatedState.copy(rolError = "Debe seleccionar un rol")
                hayErrores = true
            }

            // Validar campos de transportista (idRol = 2)
            if (state.rolSeleccionado?.idRol == 2L) {
                if (state.licencia.isBlank()) {
                    updatedState = updatedState.copy(licenciaError = "La licencia es requerida para transportistas")
                    hayErrores = true
                }
                if (state.vehiculo.isBlank()) {
                    updatedState = updatedState.copy(vehiculoError = "El vehículo es requerido para transportistas")
                    hayErrores = true
                }
            }

            _crearUsuarioState.value = updatedState

            if (hayErrores) return@launch

            _crearUsuarioState.value = _crearUsuarioState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Crear persona en API usando endpoint de admin (requiere RUT)
                val personaDTO = PersonaDTO(
                    idPersona = null,
                    nombre = state.nombre,
                    apellido = state.apellido,
                    rut = state.rut,
                    telefono = state.telefono.ifBlank { null },
                    email = state.email.ifBlank { null },
                    idComuna = state.idComuna,
                    calle = state.calle.ifBlank { null },
                    numeroPuerta = state.numeroPuerta.ifBlank { null },
                    username = state.username,
                    fechaRegistro = System.currentTimeMillis(),
                    estado = "activo"
                )

                // Usar endpoint de admin que requiere RUT (para trabajadores)
                val personaResult = personaRemoteRepository.crearPersonaAdmin(personaDTO)
                if (personaResult.isFailure) {
                    throw Exception(personaResult.exceptionOrNull()?.message ?: "Error al crear persona")
                }

                val personaCreada = personaResult.getOrNull()!!
                val personaId = personaCreada.idPersona!!

                // Crear usuario en API
                val usuarioDTO = UsuarioDTO(
                    idPersona = personaId,
                    idRol = state.rolSeleccionado!!.idRol,
                    nombreCompleto = "${state.nombre} ${state.apellido}",
                    username = state.username,
                    nombreRol = state.rolSeleccionado.nombreRol,
                    activo = true
                )

                val usuarioResult = usuarioRemoteRepository.crearUsuario(usuarioDTO)
                if (usuarioResult.isFailure) {
                    throw Exception(usuarioResult.exceptionOrNull()?.message ?: "Error al crear usuario")
                }

                // Si es cliente (idRol = 3), crear en API
                if (state.rolSeleccionado.idRol == 3L) {
                    val clienteDTO = ClienteDTO(
                        idPersona = personaId,
                        categoria = "regular",
                        nombreCompleto = "${state.nombre} ${state.apellido}",
                        email = state.email,
                        telefono = state.telefono,
                        activo = true
                    )
                    clienteRemoteRepository.crearCliente(clienteDTO)
                }

                // Si es transportista (idRol = 2), crear en API
                if (state.rolSeleccionado.idRol == 2L) {
                    val transportistaDTO = TransportistaDTO(
                        idPersona = personaId,
                        patente = state.licencia.ifBlank { null },
                        tipoVehiculo = state.vehiculo.ifBlank { null },
                        activo = true
                    )
                    val transportistaResult = transportistaRemoteRepository.crear(transportistaDTO)
                    if (transportistaResult.isFailure) {
                        // No hay repositorio local: registrar en logs
                        Log.w("UsuarioVM", "No se pudo crear transportista remoto para personaId=$personaId: ${transportistaResult.exceptionOrNull()?.message}")
                    }
                }

                _successMessage.value = "Usuario creado exitosamente"
                _crearUsuarioState.value = CrearUsuarioState(success = true)
                cargarUsuarios() // Recargar lista

            } catch (e: Exception) {
                _crearUsuarioState.value = _crearUsuarioState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear usuario: ${e.message}"
                )
            }
        }
    }

    fun eliminarUsuario(usuario: UsuarioDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = usuarioRemoteRepository.eliminarUsuario(usuario.idPersona ?: 0)
                if (result.isSuccess) {
                    _successMessage.value = "Usuario desactivado exitosamente"
                    cargarUsuarios() // Recargar lista
                } else {
                    _errorMessage.value = "Error al desactivar usuario: ${result.exceptionOrNull()?.message}"
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

    // Métodos de geografía
    private fun cargarRegiones() {
        viewModelScope.launch {
            _loadingRegiones.value = true
            try {
                val result = geografiaRemoteRepository.obtenerTodasLasRegiones()
                if (result.isSuccess) {
                    _regiones.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UsuarioVM", "Error al cargar regiones: ${e.message}")
            } finally {
                _loadingRegiones.value = false
            }
        }
    }

    private fun cargarCiudades(regionId: Long) {
        viewModelScope.launch {
            _loadingCiudades.value = true
            _ciudades.value = emptyList()
            _comunas.value = emptyList()
            try {
                val result = geografiaRemoteRepository.obtenerCiudadesPorRegion(regionId)
                if (result.isSuccess) {
                    _ciudades.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UsuarioVM", "Error al cargar ciudades: ${e.message}")
            } finally {
                _loadingCiudades.value = false
            }
        }
    }

    private fun cargarComunas(ciudadId: Long) {
        viewModelScope.launch {
            _loadingComunas.value = true
            _comunas.value = emptyList()
            try {
                val result = geografiaRemoteRepository.obtenerComunasPorCiudad(ciudadId)
                if (result.isSuccess) {
                    _comunas.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UsuarioVM", "Error al cargar comunas: ${e.message}")
            } finally {
                _loadingComunas.value = false
            }
        }
    }
}
