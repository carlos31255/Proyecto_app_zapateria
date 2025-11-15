package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleEntregaUiState(
    val entrega: EntregaDTO? = null,
    val productos: List<DetalleBoletaDTO> = emptyList(),
    val observacionInput: String = "",
    val isLoading: Boolean = true,
    val isConfirming: Boolean = false,
    val error: String? = null,
    val actualizacionExitosa: Boolean = false
)

@HiltViewModel
class ConfirmarEntregaViewModel @Inject constructor(
    private val entregasRepository: EntregasRemoteRepository,
    private val ventasRepository: VentasRemoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entregaId: Int = checkNotNull(savedStateHandle["idEntrega"])

    private val _uiState = MutableStateFlow(DetalleEntregaUiState())
    val uiState: StateFlow<DetalleEntregaUiState> = _uiState

    init {
        cargarDetallesEntrega()
    }

    private fun cargarDetallesEntrega() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val entregaResult = entregasRepository.obtenerEntregaPorId(entregaId)

                if (entregaResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar entrega: ${entregaResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }

                val entrega = entregaResult.getOrNull()
                if (entrega == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Entrega no encontrada"
                        )
                    }
                    return@launch
                }

                val boletaResult = ventasRepository.obtenerBoletaPorId(entrega.idBoleta)
                val productos = if (boletaResult.isSuccess) {
                    boletaResult.getOrNull()?.detalles ?: emptyList()
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        entrega = entrega,
                        productos = productos,
                        observacionInput = entrega.observacion ?: "",
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun onObservacionChange(nuevaObservacion: String) {
        _uiState.update { it.copy(observacionInput = nuevaObservacion) }
    }

    fun marcarComoEntregado() {
        val entregaActual = _uiState.value.entrega ?: return

        _uiState.update { it.copy(isConfirming = true, error = null) }

        viewModelScope.launch {
            try {
                val result = entregasRepository.completarEntrega(
                    entregaId = entregaActual.idEntrega ?: return@launch,
                    observacion = _uiState.value.observacionInput.ifBlank { null }
                )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isConfirming = false,
                            actualizacionExitosa = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isConfirming = false,
                            error = "Error al confirmar entrega: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isConfirming = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
}

