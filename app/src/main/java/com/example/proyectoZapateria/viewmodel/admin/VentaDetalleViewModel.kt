package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.data.repository.ClienteRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VentaDetalleUiState(
    val boleta: BoletaDTO? = null,
    val detalles: List<DetalleBoletaDTO> = emptyList(),
    val nombreCliente: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VentaDetalleViewModel @Inject constructor(
    private val ventasRepository: VentasRemoteRepository,
    private val clienteRepository: ClienteRemoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val idBoleta: Int = checkNotNull(savedStateHandle["idBoleta"])

    private val _uiState = MutableStateFlow(VentaDetalleUiState())
    val uiState: StateFlow<VentaDetalleUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        cargarDetalle()
    }

    private fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val boletaResult = ventasRepository.obtenerBoletaPorId(idBoleta)

                if (boletaResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Venta no encontrada"
                    )
                    return@launch
                }

                val boleta = boletaResult.getOrNull()
                if (boleta == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Venta no encontrada"
                    )
                    return@launch
                }

                val detalles = boleta.detalles ?: emptyList()

                var nombreCliente = ""
                val clienteResult = clienteRepository.obtenerClientePorId(boleta.clienteId)
                if (clienteResult.isSuccess) {
                    val cliente = clienteResult.getOrNull()
                    nombreCliente = cliente?.nombreCompleto ?: "Cliente #${boleta.clienteId}"
                }

                _uiState.value = _uiState.value.copy(
                    boleta = boleta,
                    detalles = detalles,
                    nombreCliente = nombreCliente,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar detalle: ${e.message}"
                )
            }
        }
    }

    fun cancelarVenta(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = ventasRepository.cambiarEstadoBoleta(idBoleta, "CANCELADA")

                if (result.isSuccess) {
                    _successMessage.value = "Venta cancelada exitosamente"
                    cargarDetalle()
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cancelar venta: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cancelar venta: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensaje() {
        _successMessage.value = null
    }
}

