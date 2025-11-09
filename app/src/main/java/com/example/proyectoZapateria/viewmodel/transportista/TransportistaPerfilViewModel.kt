package com.example.proyectoZapateria.viewmodel.transportista

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import com.example.proyectoZapateria.data.repository.TransportistaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransportistaPerfilViewModel @Inject constructor(
    private val transportistaRepository: TransportistaRepository,
    private val entregaRepository: EntregaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaPerfilUiState())
    val uiState: StateFlow<TransportistaPerfilUiState> = _uiState.asStateFlow()

    // Estados para edición
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editNombre = MutableStateFlow("")
    val editNombre: StateFlow<String> = _editNombre.asStateFlow()

    private val _editApellido = MutableStateFlow("")
    val editApellido: StateFlow<String> = _editApellido.asStateFlow()

    private val _editEmail = MutableStateFlow("")
    val editEmail: StateFlow<String> = _editEmail.asStateFlow()

    private val _editTelefono = MutableStateFlow("")
    val editTelefono: StateFlow<String> = _editTelefono.asStateFlow()

    private val _editLicencia = MutableStateFlow("")
    val editLicencia: StateFlow<String> = _editLicencia.asStateFlow()

    private val _editVehiculo = MutableStateFlow("")
    val editVehiculo: StateFlow<String> = _editVehiculo.asStateFlow()

    private var currentIdPersona: Int? = null

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Obtener el ID del transportista desde el usuario actual
                val currentUser = authRepository.currentUser.firstOrNull()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay sesión activa"
                    )
                    return@launch
                }

                val transportistaId = currentUser.idPersona
                currentIdPersona = transportistaId
                Log.d("PerfilVM", "Cargando perfil para transportista ID: $transportistaId")

                // Obtener datos de la persona
                val perfilCompleto = transportistaRepository.getPerfilTransportista(transportistaId)

                if (perfilCompleto != null) {
                    Log.d("PerfilVM", "Perfil obtenido: ${perfilCompleto.getNombreCompleto()}")

                    // Guardar datos originales para edición
                    val nombreParts = perfilCompleto.getNombreCompleto().split(" ", limit = 2)
                    _editNombre.value = nombreParts.getOrNull(0) ?: ""
                    _editApellido.value = nombreParts.getOrNull(1) ?: ""
                    _editEmail.value = perfilCompleto.email ?: ""
                    _editTelefono.value = perfilCompleto.telefono ?: ""
                    _editLicencia.value = perfilCompleto.licencia ?: ""
                    _editVehiculo.value = perfilCompleto.vehiculo ?: ""

                    // Obtener estadísticas de entregas
                    entregaRepository.getEntregasPorTransportista(transportistaId)
                        .collect { entregas ->
                            val completadas = entregas.count { it.estadoEntrega == "completada" }
                            val entregadas = entregas.count { it.estadoEntrega == "entregada" }
                            val pendientes = entregas.count { it.estadoEntrega == "pendiente" }
                            val total = entregas.size

                            Log.d("PerfilVM", "Estadísticas: Total=$total, Completadas=$completadas, Entregadas=$entregadas, Pendientes=$pendientes")

                            _uiState.value = TransportistaPerfilUiState(
                                nombre = perfilCompleto.getNombreCompleto(),
                                email = perfilCompleto.email ?: "Sin email",
                                telefono = perfilCompleto.telefono ?: "Sin teléfono",
                                licencia = perfilCompleto.licencia ?: "No especificada",
                                vehiculo = perfilCompleto.vehiculo ?: "No especificado",
                                totalEntregas = total,
                                entregasCompletadas = completadas + entregadas, // Completadas incluye entregadas
                                entregasPendientes = pendientes,
                                isLoading = false
                            )
                        }
                } else {
                    Log.e("PerfilVM", "No se encontró perfil para ID: $transportistaId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró información del transportista"
                    )
                }
            } catch (e: Exception) {
                Log.e("PerfilVM", "Error al cargar perfil: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun actualizarPerfil() {
        cargarPerfil()
    }

    fun startEdit() {
        _isEditing.value = true
    }

    fun cancelEdit() {
        _isEditing.value = false
        // Restaurar valores originales
        val nombreParts = _uiState.value.nombre.split(" ", limit = 2)
        _editNombre.value = nombreParts.getOrNull(0) ?: ""
        _editApellido.value = nombreParts.getOrNull(1) ?: ""
        _editEmail.value = _uiState.value.email
        _editTelefono.value = _uiState.value.telefono
        _editLicencia.value = _uiState.value.licencia
        _editVehiculo.value = _uiState.value.vehiculo
    }

    fun updateEditField(
        nombre: String? = null,
        apellido: String? = null,
        email: String? = null,
        telefono: String? = null,
        licencia: String? = null,
        vehiculo: String? = null
    ) {
        nombre?.let { _editNombre.value = it }
        apellido?.let { _editApellido.value = it }
        email?.let { _editEmail.value = it }
        telefono?.let { _editTelefono.value = it }
        licencia?.let { _editLicencia.value = it }
        vehiculo?.let { _editVehiculo.value = it }
    }

    fun guardarCambios(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val idPersona = currentIdPersona
                if (idPersona == null) {
                    callback(false, "No hay sesión activa")
                    return@launch
                }

                // Validaciones básicas
                if (_editNombre.value.isBlank()) {
                    callback(false, "El nombre es requerido")
                    return@launch
                }
                if (_editApellido.value.isBlank()) {
                    callback(false, "El apellido es requerido")
                    return@launch
                }
                if (_editEmail.value.isBlank()) {
                    callback(false, "El email es requerido")
                    return@launch
                }
                if (_editTelefono.value.isBlank()) {
                    callback(false, "El teléfono es requerido")
                    return@launch
                }
                if (_editLicencia.value.isBlank()) {
                    callback(false, "La licencia es requerida")
                    return@launch
                }
                if (_editVehiculo.value.isBlank()) {
                    callback(false, "El vehículo es requerido")
                    return@launch
                }

                // Actualizar datos
                val actualizado = transportistaRepository.actualizarPerfilTransportista(
                    idPersona = idPersona,
                    nombre = _editNombre.value,
                    apellido = _editApellido.value,
                    email = _editEmail.value,
                    telefono = _editTelefono.value,
                    licencia = _editLicencia.value,
                    vehiculo = _editVehiculo.value
                )

                if (actualizado) {
                    _isEditing.value = false
                    cargarPerfil()
                    callback(true, null)
                } else {
                    callback(false, "Error al actualizar el perfil")
                }
            } catch (e: Exception) {
                Log.e("PerfilVM", "Error al guardar cambios: ${e.message}", e)
                callback(false, e.message ?: "Error desconocido")
            }
        }
    }
}

