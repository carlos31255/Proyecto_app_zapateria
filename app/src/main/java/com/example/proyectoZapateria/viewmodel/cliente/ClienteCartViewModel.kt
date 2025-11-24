package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.repository.remote.AuthRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.repository.remote.CartRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteCartViewModel @Inject constructor(
    private val cartRepository: CartRemoteRepository, // Remote wrapper over local for migration
    private val ventasRepository: VentasRemoteRepository,       // Remoto: Para crear la venta
    private val inventarioRepository: InventarioRemoteRepository, // Remoto: Para consultar stock y productos
    private val personaRemoteRepository: PersonaRemoteRepository, // Remoto: Dirección
    private val authRemoteRepository: AuthRemoteRepository
) : ViewModel() {

    data class CartItemUi(
        val cartItem: CartItemResponse,
        val producto: ProductoDTO?
    )

    data class UiState(
        val isLoading: Boolean = true,
        val items: List<CartItemUi> = emptyList(),
        val total: Long = 0L, // Cambiado a Long para evitar desbordamiento
        val error: String? = null,
        val isCheckingOut: Boolean = false,
        val checkoutMessage: String? = null,
        val checkoutSuccess: Boolean = false,
        val shouldNavigateToHome: Boolean = false,
        val address: String = "Sin dirección configurada"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCart()
        cargarDatosUsuario()
    }

    // obtener los datos del usuario (direccion)
    private fun cargarDatosUsuario() {
        viewModelScope.launch {
            val current = authRemoteRepository.currentUser.value ?: return@launch
            try {
                val result = personaRemoteRepository.obtenerPersonaPorId(current.idPersona)
                if (result.isSuccess) {
                    val persona = result.getOrNull()
                    val direccion = if (persona != null && !persona.calle.isNullOrBlank()) {
                        "${persona.calle} ${persona.numeroPuerta ?: ""}"
                    } else {
                        "Sin dirección configurada"
                    }
                    _uiState.value = _uiState.value.copy(address = direccion)
                }
            } catch (e: Exception) {
                Log.e("ClienteCartVM", "Error cargando persona: ${e.message}")
            }
        }
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = authRemoteRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Sesión expirada")
                    return@launch
                }

                // Obtener lista remota (CartItemResponse)
                var lista = cartRepository.getCartForCliente(current.idPersona).first()

                Log.d("ClienteCartVM", "loadCart: currentUser=${current.username} id=${current.idPersona}")
                Log.d("ClienteCartVM", "loadCart: received lista.size=${lista.size}")

                // Verificar stock remoto y ajustar/eliminar items si es necesario
                val ajustes = mutableListOf<String>()
                for (item in lista) {
                    try {
                        val invResult = inventarioRepository.getInventarioPorModelo(item.modeloId)
                        val inventarioRemoto = invResult.getOrNull()?.find { it.talla.trim().equals(item.talla.trim(), ignoreCase = true) }

                        if (inventarioRemoto == null) {
                            // No existe en remoto -> eliminar del carrito
                            val clienteId = item.clienteId
                            val idItem = item.id ?: 0L
                            cartRepository.deleteById(clienteId, idItem)
                            ajustes.add("Producto ${item.modeloId} eliminado: sin stock")
                        } else if (inventarioRemoto.cantidad < item.cantidad) {
                            // Ajustar cantidad al máximo disponible
                            val nuevaCantidad = inventarioRemoto.cantidad
                            if (nuevaCantidad <= 0) {
                                val clienteId = item.clienteId
                                val idItem = item.id ?: 0L
                                cartRepository.deleteById(clienteId, idItem)
                                ajustes.add("Producto ${item.modeloId} eliminado: stock 0")
                            } else {
                                val currentUser = authRemoteRepository.currentUser.value
                                val req = CartItemRequest(
                                    id = item.id,
                                    clienteId = item.clienteId,
                                    modeloId = item.modeloId,
                                    talla = item.talla,
                                    cantidad = nuevaCantidad,
                                    precioUnitario = item.precioUnitario,
                                    nombreProducto = item.nombreProducto
                                )
                                if (currentUser != null) cartRepository.addOrUpdate(req, currentUser.idPersona)
                                else cartRepository.addOrUpdate(req)
                                ajustes.add("Cantidad de ${item.modeloId} ajustada a $nuevaCantidad por stock")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("ClienteCartVM", "No se pudo verificar stock remoto para modelo ${item.modeloId}: ${e.message}")
                    }
                }

                // Si hubo ajustes, recargar la lista actualizada
                if (ajustes.isNotEmpty()) {
                    lista = cartRepository.getCartForCliente(current.idPersona).first()
                    _uiState.value = _uiState.value.copy(error = ajustes.joinToString("; "))
                }

                // Mapeo: por cada item obtener ProductoDTO remoto (si posible)
                val itemsUi = lista.map { item ->
                    val modeloRes = try {
                        inventarioRepository.getModeloById(item.modeloId)
                    } catch (e: Exception) {
                        Result.failure<ProductoDTO>(e)
                    }
                    val producto = modeloRes.getOrNull()
                    CartItemUi(item, producto)
                }

                val total = lista.sumOf { it.cantidad.toLong() * it.precioUnitario }
                _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi, total = total)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun incrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            val item = itemUi.cartItem
            _uiState.value = _uiState.value.copy(error = null)

            try {
                val result = inventarioRepository.getInventarioPorModelo(item.modeloId)
                val inventarioRemoto = result.getOrNull()?.find { it.talla?.trim()?.equals(item.talla.trim(), ignoreCase = true) == true }

                if (inventarioRemoto == null) {
                    _uiState.value = _uiState.value.copy(error = "Producto no disponible en el servidor.")
                    return@launch
                }

                val nuevaCantidad = item.cantidad + 1
                if (inventarioRemoto.cantidad < nuevaCantidad) {
                    _uiState.value = _uiState.value.copy(
                        error = "Stock insuficiente. Máximo disponible: ${inventarioRemoto.cantidad}"
                    )
                    return@launch
                }

                val current = authRemoteRepository.currentUser.value
                val req = CartItemRequest(
                    id = item.id,
                    clienteId = item.clienteId,
                    modeloId = item.modeloId,
                    talla = item.talla,
                    cantidad = nuevaCantidad,
                    precioUnitario = item.precioUnitario,
                    nombreProducto = item.nombreProducto
                )
                if (current != null) cartRepository.addOrUpdate(req, current.idPersona) else cartRepository.addOrUpdate(req)
                loadCart()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error de conexión: ${e.message}")
            }
        }
    }

    fun decrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val item = itemUi.cartItem
                if (item.cantidad <= 1) {
                    cartRepository.deleteById(item.clienteId, item.id ?: 0L)
                } else {
                    val updatedReq = CartItemRequest(
                        id = item.id,
                        clienteId = item.clienteId,
                        modeloId = item.modeloId,
                        talla = item.talla,
                        cantidad = item.cantidad - 1,
                        precioUnitario = item.precioUnitario,
                        nombreProducto = item.nombreProducto
                    )
                    cartRepository.addOrUpdate(updatedReq)
                }
                loadCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error: ${e.message}")
                loadCart()
            }
        }
    }

    fun removeItem(itemUi: CartItemUi) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val item = itemUi.cartItem
                cartRepository.deleteById(item.clienteId, item.id ?: 0L)
                loadCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar: ${e.message}")
                loadCart()
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val current = authRemoteRepository.currentUser.value ?: return@launch
                cartRepository.clear(current.idPersona)
                loadCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al vaciar carrito: ${e.message}")
                loadCart()
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingOut = true, error = null)
            try {
                val current = authRemoteRepository.currentUser.value ?: return@launch
                val idCliente = current.idPersona
                val items = cartRepository.getCartForCliente(idCliente).first()

                if (items.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Carrito vacío")
                    return@launch
                }

                //  Construir lista de detalles para el Backend
                val detallesDTO = mutableListOf<DetalleBoletaDTO>()

                // obtener los IDs de inventario reales desde el backend y re-verificar stock
                for (item in items) {
                    val invResult = inventarioRepository.getInventarioPorModelo(item.modeloId)
                    val remoto = invResult.getOrNull()?.find { it.talla?.trim()?.equals(item.talla.trim(), ignoreCase = true) == true }

                    if (remoto == null) {
                        throw Exception("Stock insuficiente para el producto ${item.modeloId} (Talla ${item.talla})")
                    }

                    // Re-verificación final: volver a consultar cantidad justo antes de agregar
                    val invFinal = inventarioRepository.getInventarioPorModelo(item.modeloId)
                    val remotoFinal = invFinal.getOrNull()?.find { it.talla?.trim()?.equals(item.talla.trim(), ignoreCase = true) == true }

                    if (remotoFinal == null || remotoFinal.cantidad < item.cantidad) {
                        throw Exception("Stock insuficiente para el producto ${item.modeloId} (Talla ${item.talla})")
                    }

                    detallesDTO.add(
                        DetalleBoletaDTO(
                            id = null,
                            boletaId = null,
                            inventarioId = remotoFinal.id ?: 0L,
                            nombreProducto = remotoFinal.nombre,
                            talla = item.talla,
                            cantidad = item.cantidad,
                            precioUnitario = item.precioUnitario,
                            subtotal = item.precioUnitario * item.cantidad
                        )
                    )
                }

                val request = CrearBoletaRequest(
                    clienteId = idCliente,
                    metodoPago = "App Android",
                    observaciones = "Dirección de entrega: ${_uiState.value.address}",
                    detalles = detallesDTO
                )

                val response = ventasRepository.crearBoleta(request)

                if (response.isSuccess) {
                    cartRepository.clear(idCliente)
                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        checkoutSuccess = true,
                        checkoutMessage = "¡Compra exitosa! Boleta #${response.getOrNull()?.id}",
                        shouldNavigateToHome = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Error: ${response.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCheckingOut = false, error = e.message)
            }
        }
    }

    fun resetNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToHome = false)
    }
}
