package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.DetalleBoletaRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClientePedidosVM"

data class PedidoConEntrega(
    val boleta: BoletaDTO,
    val entrega: EntregaDTO?
)

@HiltViewModel
class ClientePedidosViewModel @Inject constructor(
    private val ventasRepository: VentasRemoteRepository,
    private val entregasRepository: EntregasRemoteRepository,
    private val detalleBoletaRepository: DetalleBoletaRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository
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
                val current = authRemoteRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }

                val idPersona = current.idPersona

                Log.d(TAG, "Cargando boletas para idPersona=$idPersona")
                val boletasResult = ventasRepository.obtenerBoletasPorCliente(idPersona)

                if (boletasResult.isFailure) {
                    Log.e(TAG, "Error fetching boletas: ${boletasResult.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar pedidos: ${boletasResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val boletas = boletasResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Boletas obtenidas: ${boletas.size}")

                Log.d(TAG, "Cargando todas las entregas")
                val todasLasEntregasResult = entregasRepository.obtenerTodasLasEntregas()
                val todasLasEntregas = if (todasLasEntregasResult.isSuccess) {
                    todasLasEntregasResult.getOrNull() ?: emptyList()
                } else {
                    Log.e(TAG, "Error fetching entregas: ${todasLasEntregasResult.exceptionOrNull()?.message}")
                    emptyList()
                }
                Log.d(TAG, "Entregas obtenidas: ${todasLasEntregas.size}")

                val pedidosConEntrega = boletas.map { boleta ->
                    val entrega = todasLasEntregas.find { it.idBoleta == boleta.id }
                    Log.d(TAG, "Boleta ${boleta.id} - Entrega: ${entrega?.estadoEntrega ?: "SIN ENTREGA"}")
                    PedidoConEntrega(boleta, entrega)
                }

                _uiState.value = _uiState.value.copy(isLoading = false, pedidos = pedidosConEntrega)
                Log.d(TAG, "Total pedidos cargados: ${pedidosConEntrega.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar pedidos: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun getProductosForBoleta(boletaId: Long) = detalleBoletaRepository.getProductos(boletaId)

    fun confirmarPedidoCompletado(idEntrega: Long, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val entregaResult = entregasRepository.obtenerEntregaPorId(idEntrega)

                if (entregaResult.isFailure) {
                    callback(false, "Entrega no encontrada")
                    return@launch
                }

                val entrega = entregaResult.getOrNull()
                Log.d(TAG, "Confirmar entrega $idEntrega - Estado actual: ${entrega?.estadoEntrega}")

                if (entrega == null) {
                    callback(false, "Entrega no encontrada")
                    return@launch
                }

                if (entrega.estadoEntrega != "entregada") {
                    Log.w(TAG, "Estado incorrecto: ${entrega.estadoEntrega}, se esperaba 'entregada'")
                    callback(false, "El pedido debe estar entregado primero (Estado actual: ${entrega.estadoEntrega})")
                    return@launch
                }

                val result = entregasRepository.cambiarEstadoEntrega(idEntrega, "completada")

                if (result.isSuccess) {
                    Log.d(TAG, "Entrega $idEntrega actualizada a 'completada'")
                    loadPedidos()
                    callback(true, null)
                } else {
                    callback(false, "Error al actualizar entrega: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al confirmar pedido: ${e.message}", e)
                callback(false, e.message ?: "Error al confirmar el pedido")
            }
        }
    }
}
