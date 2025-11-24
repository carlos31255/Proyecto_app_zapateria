package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.ClienteRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.domain.validation.validateProfileEmail
import com.example.proyectoZapateria.domain.validation.validateProfileName
import com.example.proyectoZapateria.domain.validation.validateProfilePhone
import com.example.proyectoZapateria.domain.validation.validateProfileStreet
import com.example.proyectoZapateria.domain.validation.validateProfileHouseNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientePerfilViewModel @Inject constructor(
    private val clienteRemoteRepository: ClienteRemoteRepository,
    private val personaRemoteRepository: PersonaRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val ventasRemoteRepository: VentasRemoteRepository,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    // Estado de edición y campos editables
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _editNombre = MutableStateFlow("")
    private val _editApellido = MutableStateFlow("")
    private val _editEmail = MutableStateFlow("")
    private val _editTelefono = MutableStateFlow("")
    private val _editCalle = MutableStateFlow("")
    private val _editNumeroPuerta = MutableStateFlow("")

    val editNombre = _editNombre.asStateFlow()
    val editApellido = _editApellido.asStateFlow()
    val editEmail = _editEmail.asStateFlow()
    val editTelefono = _editTelefono.asStateFlow()
    val editCalle = _editCalle.asStateFlow()
    val editNumeroPuerta = _editNumeroPuerta.asStateFlow()

    private var personaActual: PersonaDTO? = null

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val currentUser = authRemoteRepository.currentUser.value
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }

                val idPersona = currentUser.idPersona

                // Obtener datos del cliente desde API remota
                val clienteResult = clienteRemoteRepository.obtenerClientePorId(idPersona)
                val personaResult = personaRemoteRepository.obtenerPersonaPorId(idPersona)

                if (clienteResult.isSuccess && personaResult.isSuccess) {
                    val cliente = clienteResult.getOrNull()
                    val persona = personaResult.getOrNull()

                    if (cliente != null && persona != null) {
                        // Estadísticas: contar boletas del cliente (via remoto)
                        var total = 0
                        try {
                            val boletasRes = ventasRemoteRepository.obtenerBoletasPorCliente(idPersona)
                            if (boletasRes.isSuccess) {
                                val boletas = boletasRes.getOrNull()
                                total = boletas?.size ?: 0
                            }
                        } catch (e: Exception) {
                            // si falla, mantenemos total = 0
                            Log.w("ClientePerfilVM", "No se pudo obtener boletas remotas: ${e.message}")
                        }

                        _uiState.value = ClientePerfilUiState(
                            nombre = cliente.nombreCompleto ?: "",
                            email = cliente.email ?: "",
                            telefono = cliente.telefono ?: "",
                            categoria = cliente.categoria ?: "",
                            calle = persona.calle ?: "",
                            numeroPuerta = persona.numeroPuerta ?: "",
                            totalPedidos = total,
                            isLoading = false
                        )

                        // Poblar campos editables
                        personaActual = persona
                        _editNombre.value = persona.nombre ?: ""
                        _editApellido.value = persona.apellido ?: ""
                        _editEmail.value = persona.email ?: ""
                        _editTelefono.value = persona.telefono ?: ""
                        _editCalle.value = persona.calle ?: ""
                        _editNumeroPuerta.value = persona.numeroPuerta ?: ""
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "No se encontró información del cliente o persona")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al obtener datos del cliente")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    // Comienzo edición
    fun startEdit() {
        _isEditing.value = true
    }

    fun cancelEdit() {
        // restaurar valores desde personaActual
        personaActual?.let { p ->
            _editNombre.value = p.nombre ?: ""
            _editApellido.value = p.apellido ?: ""
            _editEmail.value = p.email ?: ""
            _editTelefono.value = p.telefono ?: ""
            _editCalle.value = p.calle ?: ""
            _editNumeroPuerta.value = p.numeroPuerta ?: ""
        }
        _isEditing.value = false
    }

    fun updateEditField(nombre: String? = null, apellido: String? = null, email: String? = null, telefono: String? = null, calle: String? = null, numeroPuerta: String? = null) {
        nombre?.let { _editNombre.value = it }
        apellido?.let { _editApellido.value = it }
        email?.let { _editEmail.value = it }
        telefono?.let { _editTelefono.value = it }
        calle?.let { _editCalle.value = it }
        numeroPuerta?.let { _editNumeroPuerta.value = it }
    }

    // Guardar cambios en PersonaDTO usando API remota
    fun guardarCambios(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val persona = personaActual
                if (persona == null) {
                    onResult(false, "No se encontró la persona")
                    return@launch
                }

                val emailVal = _editEmail.value.trim()
                val nuevoUsername = if (emailVal.isNotBlank()) emailVal else (persona.username ?: "")

                val actualizado = persona.copy(
                    nombre = _editNombre.value.trim(),
                    apellido = _editApellido.value.trim(),
                    email = emailVal.ifBlank { null },
                    telefono = _editTelefono.value.trim().ifBlank { null },
                    calle = _editCalle.value.trim().ifBlank { null },
                    numeroPuerta = _editNumeroPuerta.value.trim().ifBlank { null },
                    username = nuevoUsername
                )

                // Validaciones: nombre, email, teléfono
                val nombreVal = _editNombre.value.trim()
                val nombreErr = validateProfileName(nombreVal)
                if (nombreErr != null) { onResult(false, nombreErr); return@launch }

                if (emailVal.isNotBlank()) {
                    val emailErr = validateProfileEmail(emailVal)
                    if (emailErr != null) { onResult(false, emailErr); return@launch }
                    // Validación de email único lo debe hacer el backend
                }

                val telefonoVal = _editTelefono.value.trim()
                val telErr = validateProfilePhone(telefonoVal)
                if (telErr != null) { onResult(false, telErr); return@launch }

                // Validar dirección
                val calleVal = _editCalle.value.trim()
                if (calleVal.isNotBlank()) {
                    val calleErr = validateProfileStreet(calleVal)
                    if (calleErr != null) { onResult(false, calleErr); return@launch }
                }

                val numeroPuertaVal = _editNumeroPuerta.value.trim()
                if (numeroPuertaVal.isNotBlank()) {
                    val numeroPuertaErr = validateProfileHouseNumber(numeroPuertaVal)
                    if (numeroPuertaErr != null) { onResult(false, numeroPuertaErr); return@launch }
                }

                // Actualizar persona en el API remota
                val res = personaRemoteRepository.actualizarPersona(persona.idPersona ?: 0, actualizado)
                if (res.isSuccess) {
                    // Actualizar sesión si el usuario visible cambió (nombre/email)
                    val current = authRemoteRepository.currentUser.value
                    if (current != null && current.idPersona == persona.idPersona) {
                        // Obtener usuario actualizado desde API
                        val usuarioActualizadoResult = authRemoteRepository.obtenerUsuarioPorId(persona.idPersona)
                        usuarioActualizadoResult.onSuccess { nuevoUsuario ->
                            authRemoteRepository.setCurrentUser(nuevoUsuario)
                            // Actualizar DataStore/sessionPreferences con el nuevo username si cambió
                            try {
                                sessionPreferences.saveSession(
                                    userId = nuevoUsuario.idPersona,
                                    username = nuevoUsuario.username,
                                    userRole = nuevoUsuario.nombreRol,
                                    userRoleId = nuevoUsuario.idRol
                                )
                            } catch (_: Exception) {
                                // no bloquear la operación por fallo en preferences
                            }
                        }
                    }
                    // refrescar UI local
                    cargarPerfil()
                    _isEditing.value = false
                    onResult(true, null)
                } else {
                    onResult(false, res.exceptionOrNull()?.message ?: "Error al guardar")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
