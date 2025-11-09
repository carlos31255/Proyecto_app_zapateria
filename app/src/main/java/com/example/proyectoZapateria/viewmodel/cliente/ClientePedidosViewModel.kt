package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.local.entrega.EntregaDao
import com.example.proyectoZapateria.data.local.entrega.EntregaEntity
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class para pedido con información de entrega
data class PedidoConEntrega(
    val boleta: BoletaVentaEntity,
    val entrega: EntregaEntity?
)

@HiltViewModel
class ClientePedidosViewModel @Inject constructor(
    private val boletaVentaDao: BoletaVentaDao,
    private val detalleBoletaRepository: DetalleBoletaRepository,
    private val authRepository: AuthRepository,
    private val entregaRepository: EntregaRepository,
    private val entregaDao: EntregaDao
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val pedidos: List<PedidoConEntrega> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = authRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }

                val idPersona = current.idPersona
                val boletasFlow = boletaVentaDao.getByCliente(idPersona)
                val boletas = boletasFlow.first()

                // Para cada boleta, obtener su entrega
                val pedidosConEntrega = boletas.map { boleta ->
                    val entrega = entregaDao.getByBoleta(boleta.idBoleta)
                    Log.d("ClientePedidosVM", "Boleta ${boleta.numeroBoleta} - Entrega: ${entrega?.estadoEntrega ?: "SIN ENTREGA"}")
                    PedidoConEntrega(boleta, entrega)
                }

                _uiState.value = _uiState.value.copy(isLoading = false, pedidos = pedidosConEntrega)
                Log.d("ClientePedidosVM", "Total pedidos cargados: ${pedidosConEntrega.size}")
            } catch (e: Exception) {
                Log.e("ClientePedidosVM", "Error al cargar pedidos: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    // Devuelve un Flow con los productos (nombre, talla, cantidad, marca) para una boleta
    fun getProductosForBoleta(idBoleta: Int): Flow<List<ProductoDetalle>> {
        return detalleBoletaRepository.getProductos(idBoleta)
    }

    // Confirmar pedido como completado por el cliente
    fun confirmarPedidoCompletado(idEntrega: Int, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val entrega = entregaDao.getEntregaById(idEntrega)
                Log.d("ClientePedidosVM", "Confirmar entrega $idEntrega - Estado actual: ${entrega?.estadoEntrega}")

                if (entrega == null) {
                    callback(false, "Entrega no encontrada")
                    return@launch
                }

                // Solo se puede completar si está en estado "entregada"
                if (entrega.estadoEntrega != "entregada") {
                    Log.w("ClientePedidosVM", "Estado incorrecto: ${entrega.estadoEntrega}, se esperaba 'entregada'")
                    callback(false, "El pedido debe estar entregado primero (Estado actual: ${entrega.estadoEntrega})")
                    return@launch
                }

                // Actualizar a completada
                val entregaActualizada = entrega.copy(estadoEntrega = "completada")
                entregaDao.updateEntrega(entregaActualizada)
                Log.d("ClientePedidosVM", "Entrega $idEntrega actualizada a 'completada'")

                // Recargar pedidos
                loadPedidos()
                callback(true, null)
            } catch (e: Exception) {
                Log.e("ClientePedidosVM", "Error al confirmar pedido: ${e.message}", e)
                callback(false, e.message ?: "Error al confirmar el pedido")
            }
        }
    }
}