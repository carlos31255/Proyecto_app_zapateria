package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransportistaPerfilViewModel @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val entregaRepository: EntregaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transportistaId: Int = checkNotNull(savedStateHandle["transportistaId"])

    private val _uiState = MutableStateFlow(TransportistaPerfilUiState())
    val uiState: StateFlow<TransportistaPerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Obtener datos de la persona
                val persona = personaRepository.getPersonaById(transportistaId)

                if (persona != null) {
                    // Obtener estadísticas de entregas
                    entregaRepository.getEntregasPorTransportista(transportistaId)
                        .collect { entregas ->
                            val completadas = entregas.count { it.estadoEntrega == "completada" }
                            val pendientes = entregas.count { it.estadoEntrega == "pendiente" }

                            _uiState.value = TransportistaPerfilUiState(
                                nombre = "${persona.nombre} ${persona.apellido}",
                                email = persona.email ?: "Sin email",
                                telefono = persona.telefono ?: "Sin teléfono",
                                totalEntregas = entregas.size,
                                entregasCompletadas = completadas,
                                entregasPendientes = pendientes,
                                isLoading = false,
                                error = null
                            )
                        }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró información del transportista"
                    )
                }
            } catch (e: Exception) {
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