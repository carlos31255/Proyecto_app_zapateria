package com.example.proyectoZapateria.viewmodel.cliente

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

        // Suscribirse a actualizaciones globales de entregas para recargar pedidos cuando cambie algo
        viewModelScope.launch {
            entregasRepository.updatesFlow.collect {
                // Detectada actualización en entregas -> recargando pedidos
                try {
                    loadPedidos()
                } catch (e: Exception) {
                    // Error recargando pedidos tras update: ${e.message}
                }
            }
        }
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

                // Cargando boletas para idPersona=$idPersona
                val boletasResult = ventasRepository.obtenerBoletasPorCliente(idPersona)

                if (boletasResult.isFailure) {
                    // Error fetching boletas: ${boletasResult.exceptionOrNull()?.message}
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar pedidos: ${boletasResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val boletas = boletasResult.getOrNull() ?: emptyList()
                // Boletas obtenidas: ${boletas.size}

                // Cargando todas las entregas
                val todasLasEntregasResult = entregasRepository.obtenerTodasLasEntregas()
                val todasLasEntregas = if (todasLasEntregasResult.isSuccess) {
                    todasLasEntregasResult.getOrNull() ?: emptyList()
                } else {
                    // Error fetching entregas: ${todasLasEntregasResult.exceptionOrNull()?.message}
                    emptyList()
                }
                // Entregas obtenidas: ${todasLasEntregas.size}

                val pedidosConEntrega = boletas.map { boleta ->
                    val entrega = todasLasEntregas.find { it.idBoleta == boleta.id }
                    // Boleta ${boleta.id} - Entrega: ${entrega?.estadoEntrega ?: "SIN ENTREGA"}
                    PedidoConEntrega(boleta, entrega)
                }

                _uiState.value = _uiState.value.copy(isLoading = false, pedidos = pedidosConEntrega)
                // Total pedidos cargados: ${pedidosConEntrega.size}
            } catch (e: Exception) {
                // Error al cargar pedidos: ${e.message}
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
                // Confirmar entrega $idEntrega - Estado actual: ${entrega?.estadoEntrega}

                if (entrega == null) {
                    callback(false, "Entrega no encontrada")
                    return@launch
                }

                if (entrega.estadoEntrega != "entregada") {
                    // Estado incorrecto: ${entrega.estadoEntrega}, se esperaba 'entregada'
                    callback(false, "El pedido debe estar entregado primero (Estado actual: ${entrega.estadoEntrega})")
                    return@launch
                }

                val result = entregasRepository.cambiarEstadoEntrega(idEntrega, "completada", null)

                if (result.isSuccess) {
                    // Entrega $idEntrega actualizada a 'completada'
                    // recargar pedidos para reflejar cambios
                    loadPedidos()
                    callback(true, null)
                } else {
                    callback(false, "Error al actualizar entrega: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                // Error al confirmar pedido: ${e.message}
                callback(false, e.message ?: "Error al confirmar el pedido")
            }
        }
    }
}
