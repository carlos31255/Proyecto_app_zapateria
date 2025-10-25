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
                Log.d("PerfilVM", "Cargando perfil para transportista ID: $transportistaId")

                // Obtener datos de la persona
                val perfilCompleto = transportistaRepository.getPerfilTransportista(transportistaId)

                if (perfilCompleto != null) {
                    Log.d("PerfilVM", "Perfil obtenido: ${perfilCompleto.getNombreCompleto()}")

                    // Obtener estadísticas de entregas
                    entregaRepository.getEntregasPorTransportista(transportistaId)
                        .collect { entregas ->
                            val completadas = entregas.count { it.estadoEntrega == "completada" }
                            val pendientes = entregas.count { it.estadoEntrega == "pendiente" }
                            val total = completadas + pendientes

                            Log.d("PerfilVM", "Estadísticas: Total=$total, Completadas=$completadas, Pendientes=$pendientes")

                            _uiState.value = TransportistaPerfilUiState(
                                nombre = perfilCompleto.getNombreCompleto(),
                                email = perfilCompleto.email ?: "Sin email",
                                telefono = perfilCompleto.telefono ?: "Sin teléfono",
                                licencia = perfilCompleto.licencia ?: "No especificada",
                                vehiculo = perfilCompleto.vehiculo ?: "No especificado",
                                totalEntregas = total,
                                entregasCompletadas = completadas,
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
}