package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Usamos OptIn para ExperimentalCoroutinesApi por el flatMapLatest
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConfirmarEntregaViewModel @Inject constructor(
    private val entregaRepository: EntregaRepository,
    private val detalleBoletaRepository: DetalleBoletaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtenemos el ID de la entrega desde la navegación
    private val entregaId: Int = checkNotNull(savedStateHandle["idEntrega"])

    // Estado de la UI
    private val _uiState = MutableStateFlow(DetalleEntregaUiState())
    val uiState: StateFlow<DetalleEntregaUiState> = _uiState

    // Job para la recolección de productos; lo cancelamos si cambiara el id de boleta
    private var productosJob: Job? = null

    init {
        cargarDetallesEntrega()
    }

    private fun cargarDetallesEntrega() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            entregaRepository.getDetallesPorId(entregaId).collect { detalles ->
                // Actualizamos la entrega en el uiState
                _uiState.update {
                    it.copy(
                        entrega = detalles,
                        isLoading = false
                    )
                }

                // Cancelar la recolección previa de productos (si existiera)
                productosJob?.cancel()

                // Iniciar la recolección de productos para la boleta asociada
                val d = detalles
                productosJob = viewModelScope.launch {
                    try {
                        detalleBoletaRepository.getProductos(d.numeroBoleta).collect { productos ->
                            _uiState.update { it.copy(productos = productos) }
                        }
                    } catch (e: Exception) {
                        // No bloqueamos la UI por fallo al cargar productos; sólo registramos el error
                        _uiState.update { it.copy(error = e.message ?: "Error al cargar productos") }
                    }
                }
            }
        }
    }

    fun onObservacionChange(newObservacion: String) {
        _uiState.update { it.copy(observacionInput = newObservacion) }
    }

    fun marcarComoEntregado() {
        if (_uiState.value.isConfirming || _uiState.value.isConfirmed) return

        _uiState.update { it.copy(isConfirming = true, error = null) }
        viewModelScope.launch {
            try {
                val observacion = _uiState.value.observacionInput.trim().ifEmpty { null }
                val success = entregaRepository.confirmarEntrega(entregaId, observacion)

                _uiState.update {
                    if (success) {
                        it.copy(
                            isConfirming = false,
                            isConfirmed = true,
                            error = null,
                            actualizacionExitosa = true
                        )
                    } else {
                        it.copy(
                            isConfirming = false,
                            error = "Error al confirmar la entrega en la base de datos."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isConfirming = false,
                        error = e.message ?: "Error al actualizar"
                    )
                }
            }
        }
    }
}
