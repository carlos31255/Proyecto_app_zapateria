package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDetalleUi
import com.example.proyectoZapateria.data.repository.remote.DetalleBoletaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleEntregaUiState(
    val entrega: EntregaDTO? = null,
    val productos: List<ProductoDetalleUi> = emptyList(),
    val observacionInput: String = "",
    val isLoading: Boolean = true,
    val isConfirming: Boolean = false,
    val error: String? = null,
    val actualizacionExitosa: Boolean = false
)

@HiltViewModel
class ConfirmarEntregaViewModel @Inject constructor(
    private val entregasRepository: EntregasRemoteRepository,
    private val detalleBoletaRepository: DetalleBoletaRemoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entregaId: Long = checkNotNull(savedStateHandle["idEntrega"]) as Long

    private val _uiState = MutableStateFlow(DetalleEntregaUiState())
    val uiState: StateFlow<DetalleEntregaUiState> = _uiState

    init {
        cargarDetallesEntrega()
    }

    private fun toUiDetalle(obj: Any): ProductoDetalleUi {
        return when (obj) {
            is ProductoDetalleUi -> obj
            is DetalleBoletaDTO -> ProductoDetalleUi(
                producto = ProductoDTO(id = null, nombre = obj.nombreProducto ?: "-", marcaId = -1L, descripcion = null, precioUnitario = obj.precioUnitario, imagenUrl = null),
                cantidad = obj.cantidad,
                talla = obj.talla ?: "-",
                marcaName = "Desconocida"
            )
            else -> ProductoDetalleUi()
        }
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

                // Para evitar bloquear, obtenemos detalles mediante el repo y actualizamos el estado cuando el flow emita
                _uiState.update {
                    it.copy(
                        entrega = entrega,
                        productos = emptyList(), // inicialmente vacío; UI se actualizará cuando el flow emita
                        observacionInput = entrega.observacion ?: "",
                        isLoading = false,
                        error = null
                    )
                }

                // Lanzar una coroutine para recolectar el flow y actualizar productos
                viewModelScope.launch {
                    try {
                        detalleBoletaRepository.getProductos(entrega.idBoleta).collect { lista ->
                            val mapped = lista.map { toUiDetalle(it as Any) }
                            _uiState.update { it.copy(productos = mapped) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = "Error cargando productos: ${e.message}") }
                    }
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
