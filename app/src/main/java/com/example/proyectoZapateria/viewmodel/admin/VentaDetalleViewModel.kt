package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaDao
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VentaDetalleUiState(
    val boleta: BoletaVentaEntity? = null,
    val detalles: List<ProductoDetalle> = emptyList(),
    val nombreCliente: String = "",
    val apellidoCliente: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VentaDetalleViewModel @Inject constructor(
    private val boletaVentaDao: BoletaVentaDao,
    private val detalleBoletaDao: DetalleBoletaDao,
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
                // Obtener la boleta
                val boleta = boletaVentaDao.getById(idBoleta)
                if (boleta == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Venta no encontrada"
                    )
                    return@launch
                }

                // Obtener los detalles con información del producto
                detalleBoletaDao.getProductosDeBoleta(idBoleta).collect { detalles ->
                    // Obtener información del cliente desde la consulta con info
                    val ventaConInfo = boletaVentaDao.getAllBoletasConInfo()
                    ventaConInfo.collect { ventas ->
                        val info = ventas.find { it.id_boleta == idBoleta }
                        _uiState.value = _uiState.value.copy(
                            boleta = boleta,
                            detalles = detalles,
                            nombreCliente = info?.nombre_cliente ?: "",
                            apellidoCliente = info?.apellido_cliente ?: "",
                            isLoading = false,
                            error = null
                        )
                    }
                }
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
                boletaVentaDao.cancelarBoleta(idBoleta)
                _successMessage.value = "Venta cancelada exitosamente"
                onSuccess()
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

