package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteCartViewModel @Inject constructor(
    private val cartRepository: CartRepository,           // Local: Solo para guardar items temporalmente
    private val ventasRepository: VentasRemoteRepository,       // Remoto: Para crear la venta
    private val inventarioRepository: InventarioRemoteRepository, // Remoto: Para consultar stock
    private val personaRemoteRepository: PersonaRemoteRepository, // Remoto: Dirección
    private val authRepository: AuthRepository
) : ViewModel() {

    data class CartItemUi(
        val cartItem: CartItemEntity,
        val modelo: ModeloZapatoEntity?
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

    // Mapas para manejar el Debounce (no usados ahora pero se dejan comentados para futuras mejoras)
    // private val debounceJobs = mutableMapOf<String, Job>()
    // private val pendingQuantities = mutableMapOf<String, Int>()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCart()
        cargarDatosUsuario()
    }

    // obtener los datos del usuario (direccion)
    private fun cargarDatosUsuario() {
        viewModelScope.launch {
            val current = authRepository.currentUser.value ?: return@launch
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
                val current = authRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Sesión expirada")
                    return@launch
                }

                // Obtener lista local
                var lista = cartRepository.getCartForCliente(current.idPersona).first()

                // Verificar stock remoto y ajustar/eliminar items locales si es necesario
                val ajustes = mutableListOf<String>()
                for (item in lista) {
                    try {
                        val invResult = inventarioRepository.getInventarioPorModelo(item.idModelo.toLong())
                        val inventarioRemoto = invResult.getOrNull()?.find { it.talla == item.talla }

                        if (inventarioRemoto == null) {
                            // No existe en remoto -> eliminar del carrito
                            cartRepository.delete(item)
                            ajustes.add("Producto ${item.idModelo} eliminado: sin stock")
                        } else if (inventarioRemoto.cantidad < item.cantidad) {
                            // Ajustar cantidad al máximo disponible
                            val nuevaCantidad = inventarioRemoto.cantidad
                            if (nuevaCantidad <= 0) {
                                cartRepository.delete(item)
                                ajustes.add("Producto ${item.idModelo} eliminado: stock 0")
                            } else {
                                cartRepository.addOrUpdate(item.copy(cantidad = nuevaCantidad))
                                ajustes.add("Cantidad de ${item.idModelo} ajustada a $nuevaCantidad por stock")
                            }
                        }
                    } catch (e: Exception) {
                        // Si falla la verificación remota, no hacemos cambios, solo informamos
                        Log.w("ClienteCartVM", "No se pudo verificar stock remoto para modelo ${item.idModelo}: ${e.message}")
                    }
                }

                // Si hubo ajustes, recargar la lista local actualizada
                if (ajustes.isNotEmpty()) {
                    lista = cartRepository.getCartForCliente(current.idPersona).first()
                    _uiState.value = _uiState.value.copy(error = ajustes.joinToString("; "))
                }

                // Mapeo simple
                val itemsUi = lista.map { item ->
                    // Usamos un modelo dummy con los datos del carrito
                    val modeloDummy = ModeloZapatoEntity(
                        idModelo = item.idModelo,
                        nombreModelo = "Producto #${item.idModelo}", // Ideal: obtener nombre real de API o cache
                        idMarca = 0L, precioUnitario = item.precioUnitario, descripcion = "", estado = "activo"
                    )
                    CartItemUi(item, modeloDummy)
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
                // Consultar Stock Real al Microservicio
                val result = inventarioRepository.getInventarioPorModelo(item.idModelo.toLong())
                val inventarioRemoto = result.getOrNull()?.find { it.talla == item.talla }

                if (inventarioRemoto == null) {
                    _uiState.value = _uiState.value.copy(error = "Producto no disponible en el servidor.")
                    return@launch
                }

                // Validar
                val nuevaCantidad = item.cantidad + 1
                if (inventarioRemoto.cantidad < nuevaCantidad) {
                    _uiState.value = _uiState.value.copy(
                        error = "Stock insuficiente. Máximo disponible: ${inventarioRemoto.cantidad}"
                    )
                    return@launch
                }

                // Actualizar localmente
                cartRepository.addOrUpdate(item.copy(cantidad = nuevaCantidad))
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
                    cartRepository.delete(item)
                } else {
                    val updated = item.copy(cantidad = item.cantidad - 1)
                    cartRepository.addOrUpdate(updated)
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
                cartRepository.delete(itemUi.cartItem)
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
                val current = authRepository.currentUser.value ?: return@launch
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
                val current = authRepository.currentUser.value ?: return@launch
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
                    val invResult = inventarioRepository.getInventarioPorModelo(item.idModelo)
                    val remoto = invResult.getOrNull()?.find { it.talla == item.talla }

                    if (remoto == null) {
                        throw Exception("Stock insuficiente para el producto ${item.idModelo} (Talla ${item.talla})")
                    }

                    // Re-verificación final: volver a consultar cantidad justo antes de agregar
                    val invFinal = inventarioRepository.getInventarioPorModelo(item.idModelo)
                    val remotoFinal = invFinal.getOrNull()?.find { it.talla == item.talla }

                    if (remotoFinal == null || remotoFinal.cantidad < item.cantidad) {
                        throw Exception("Stock insuficiente para el producto ${item.idModelo} (Talla ${item.talla})")
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

                //  Enviar la Venta al Microservicio
                val request = CrearBoletaRequest(
                    clienteId = idCliente,
                    metodoPago = "App Android",
                    observaciones = "Dirección de entrega: ${_uiState.value.address}",
                    detalles = detallesDTO
                )

                val response = ventasRepository.crearBoleta(request)

                if (response.isSuccess) {
                    // ÉXITO
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
