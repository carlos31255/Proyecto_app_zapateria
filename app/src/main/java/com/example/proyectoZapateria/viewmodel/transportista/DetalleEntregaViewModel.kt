package com.example.proyectoZapateria.viewmodel.transportista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.EntregaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// Usamos OptIn para ExperimentalCoroutinesApi por el flatMapLatest
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetalleEntregaViewModel @Inject constructor(
    private val entregaRepository: EntregaRepository,
    private val detalleBoletaRepository: DetalleBoletaRepository,
    savedStateHandle: SavedStateHandle // Inyecta esto para leer argumentos de navegación
) : ViewModel() {

    // Obtenemos el ID de la entrega desde la navegación
    private val entregaId: Int = checkNotNull(savedStateHandle["idEntrega"])

    // Flow privado que carga los detalles de la entrega (cliente, dirección)
    //    Asumimos que actualizaste tu EntregaRepository
    private val _entregaFlow = entregaRepository.getDetallesPorId(entregaId)

    // Flow privado que carga los productos
    // flatMapLatest para esperar a tener el idBoleta antes de buscar productos
    private val _productosFlow = _entregaFlow.flatMapLatest { entrega ->

        // Verificamos si la entrega NO es nula
        if (entrega != null) {
            // Si no es nula, usamos su id (numeroBoleta) para buscar los productos
            detalleBoletaRepository.getProductos(entrega.numeroBoleta)
        } else {
            // Si la entrega es nula (ej: ID no encontrado),
            // devolvemos un Flow con una lista vacía para no crashear.
            flowOf(emptyList<ProductoDetalle>())
        }
    }.catch {
        // Maneja error si la carga de productos falla
        emit(emptyList<ProductoDetalle>())
    }

    // StateFlow público que combina todo en el UiState
    private val _uiState = MutableStateFlow(DetalleEntregaUiState(isLoading = true))
    val uiState: StateFlow<DetalleEntregaUiState> = _uiState.asStateFlow()

    init {
        // Combinamos los dos flows de datos
        viewModelScope.launch {
            combine(_entregaFlow, _productosFlow) { entrega, productos ->
                DetalleEntregaUiState(
                    entrega = entrega,
                    productos = productos,
                    isLoading = false
                )
            }.catch { e ->
                // Maneja error si la carga principal falla
                _uiState.value = DetalleEntregaUiState(
                    isLoading = false,
                    error = e.message ?: "Error al cargar detalles"
                )
            }.collect { state ->
                // Actualizamos el StateFlow público
                _uiState.value = state
            }
        }
    }

    // Función para que la UI llame al botón "Completar"
    fun marcarComoEntregado() {
        viewModelScope.launch {
            try {
                // Obtenemos la entidad original (simple)
                val entregaSimple = entregaRepository.getEntregaSimple(entregaId)

                if (entregaSimple != null) {
                    // Creamos la copia actualizada
                    val entregaActualizada = entregaSimple.copy(
                        // Usamos "entregado"
                                estadoEntrega = "entregado",
                        // La schema de la BD usa Long para fechas (timestamps)
                        // Room se encarga de la conversión.
                        fechaEntrega = System.currentTimeMillis()
                    )

                    // Actualizamos en la BD
                    entregaRepository.updateEntrega(entregaActualizada)

                    // Notificamos a la UI que fue exitoso
                    _uiState.update { it.copy(actualizacionExitosa = true) }
                } else {
                    _uiState.update { it.copy(error = "No se encontró la entrega") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error al actualizar") }
            }
        }
    }
}