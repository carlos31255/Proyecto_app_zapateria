package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.cliente.ClienteConPersona
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.repository.ClienteRepository
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClienteConPedidos(
    val cliente: ClienteConPersona,
    val pedidos: List<BoletaVentaEntity>
)

@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val detalleBoletaRepository: DetalleBoletaRepository
) : ViewModel() {

    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Lista de todos los clientes (directamente desde el repositorio)
    val clientes: StateFlow<List<ClienteConPersona>> = clienteRepository.getAllConPersona()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cliente seleccionado con sus pedidos
    private val _clienteSeleccionado = MutableStateFlow<ClienteConPedidos?>(null)
    val clienteSeleccionado: StateFlow<ClienteConPedidos?> = _clienteSeleccionado.asStateFlow()

    // Productos de un pedido específico
    private val _productosDelPedido = MutableStateFlow<List<ProductoDetalle>>(emptyList())
    val productosDelPedido: StateFlow<List<ProductoDetalle>> = _productosDelPedido.asStateFlow()

    // Estado de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Clientes filtrados - combina clientes y searchQuery para reaccionar a ambos
    val clientesFiltrados: StateFlow<List<ClienteConPersona>> = combine(
        clientes,
        _searchQuery
    ) { listaClientes, query ->
        if (query.isBlank()) {
            listaClientes
        } else {
            listaClientes.filter { cliente ->
                cliente.nombre.contains(query, ignoreCase = true) ||
                cliente.apellido.contains(query, ignoreCase = true) ||
                cliente.rut.contains(query, ignoreCase = true) ||
                cliente.email?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Observar cuando lleguen los clientes para actualizar el estado de carga
        viewModelScope.launch {
            clientes.collect { lista ->
                // Actualizar isLoading a false cuando lleguen datos (incluso si es una lista vacía)
                _isLoading.value = false
            }
        }

        // Timeout de seguridad: si después de 5 segundos no hay datos, desactivar carga
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            if (_isLoading.value) {
                _isLoading.value = false
            }
        }
    }

    /**
     * Recargar clientes manualmente (opcional)
     */
    @Suppress("unused")
    fun cargarClientes() {
        _isLoading.value = true
        _errorMessage.value = null
        // El Flow ya se está recolectando automáticamente
    }

    /**
     * Actualizar query de búsqueda
     */
    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
    }

    /**
     * Cargar detalles de un cliente específico con sus pedidos
     */
    fun cargarDetalleCliente(idCliente: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Obtener información del cliente
                val cliente = clienteRepository.getByIdConPersona(idCliente)

                if (cliente != null) {
                    // Obtener pedidos del cliente (usar first() para obtener solo la primera emisión)
                    val pedidos = detalleBoletaRepository.getBoletasByCliente(idCliente).first()
                    _clienteSeleccionado.value = ClienteConPedidos(
                        cliente = cliente,
                        pedidos = pedidos
                    )
                    _isLoading.value = false
                } else {
                    _errorMessage.value = "Cliente no encontrado"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar detalles del cliente: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar productos de un pedido específico
     */
    fun cargarProductosDePedido(idBoleta: Int) {
        viewModelScope.launch {
            try {
                // Usar first() para obtener solo la primera emisión del Flow
                val productos = detalleBoletaRepository.getProductosDeBoleta(idBoleta).first()
                _productosDelPedido.value = productos
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar productos del pedido: ${e.message}"
            }
        }
    }

    /**
     * Limpiar cliente seleccionado
     */
    fun limpiarClienteSeleccionado() {
        _clienteSeleccionado.value = null
        _productosDelPedido.value = emptyList()
    }

    /**
     * Limpiar mensaje de error
     */
    @Suppress("unused")
    fun limpiarError() {
        _errorMessage.value = null
    }
}

