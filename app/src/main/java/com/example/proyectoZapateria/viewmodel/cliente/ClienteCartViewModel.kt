package com.example.proyectoZapateria.viewmodel.cliente

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteCartViewModel @Inject constructor(
    private val cartRepository: CartRemoteRepository, // Remote wrapper over local for migration
    private val ventasRepository: VentasRemoteRepository,       // Remoto: Para crear la venta
    private val inventarioRepository: InventarioRemoteRepository, // Remoto: Para consultar stock y productos
    private val personaRemoteRepository: PersonaRemoteRepository, // Remoto: Dirección
    private val authRemoteRepository: AuthRemoteRepository,
    private val entregasRepository: com.example.proyectoZapateria.data.repository.remote.EntregasRemoteRepository // Remoto: Para crear entregas
) : ViewModel() {

    data class CartItemUi(
        val cartItem: CartItemResponse,
        val producto: ProductoDTO?,
        val imageBytes: ByteArray? = null
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

    private var cartCollectJob: Job? = null
    // Job para confirmar emisiones vacías transitorias
    private var pendingEmptyJob: Job? = null

    init {
        loadCart()
        cargarDatosUsuario()
    }

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
            } catch (_: Exception) {
            }
        }
    }

    fun loadCart() {
        // Cancelar colección previa (si existe) para evitar collectors duplicados
        cartCollectJob?.cancel()
        cartCollectJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = authRemoteRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Sesión expirada")
                    return@launch
                }

                try {
                    val snapshot = cartRepository.getCacheSnapshot(current.idPersona)
                    if (snapshot.isNotEmpty()) {
                        val itemsUi = mutableListOf<CartItemUi>()
                        for (it in snapshot) {
                            itemsUi.add(mapToCartItemUi(it))
                        }
                        val total = snapshot.sumOf { it.cantidad.toLong() * it.precioUnitario }
                        _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi, total = total)
                    }
                } catch (_: Exception) {
                }

                val initialSnapshotRetryJob = launch {
                    delay(200)
                    if (_uiState.value.items.isEmpty()) {
                        try {
                            val snap2 = cartRepository.getCacheSnapshot(current.idPersona)
                            if (snap2.isNotEmpty()) {
                                val itemsUi2 = mutableListOf<CartItemUi>()
                                for (it in snap2) {
                                    itemsUi2.add(mapToCartItemUi(it))
                                }
                                val total2 = snap2.sumOf { it.cantidad.toLong() * it.precioUnitario }
                                _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi2, total = total2)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }

                cartRepository.getCartForCliente(current.idPersona).collect { lista ->
                    try {
                        if (initialSnapshotRetryJob.isActive) initialSnapshotRetryJob.cancel()

                        if (lista.isEmpty() && _uiState.value.items.isNotEmpty()) {
                            pendingEmptyJob?.cancel()
                            pendingEmptyJob = launch {
                                delay(1500)
                                 val snap = cartRepository.getCacheSnapshot(current.idPersona)
                                 val hadRecentLocal = cartRepository.hasRecentLocalUpdate(current.idPersona, 8000)
                                 val hasPending = cartRepository.hasPendingLocalChanges(current.idPersona)
                                 if (hadRecentLocal || hasPending) {
                                     return@launch
                                }
                                 if (snap.isEmpty()) {
                                     _uiState.value = _uiState.value.copy(isLoading = false, items = emptyList(), total = 0L)
                                 }
                             }
                             return@collect
                         }

                        val ajustes = mutableListOf<String>()
                        for (item in lista) {
                            try {
                                val talla = item.talla.trim()

                                if (talla.isBlank() || talla.equals("null", ignoreCase = true)) {
                                    continue
                                }

                                val invResult = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)


                                val inventarioRemoto = try {
                                    val list = invResult.getOrNull() ?: emptyList()
                                    val primary = list.firstOrNull { inv ->
                                        val invTalla = inv.talla
                                        val invModeloKey = inv.productoId
                                        val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                            inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                                        } else false
                                        (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                                    }
                                    if (primary != null) {
                                        primary
                                    } else {
                                        list.firstOrNull { it.talla.trim().equals(talla, ignoreCase = true) }
                                    }
                                } catch (_: Exception) {
                                    null
                                }

                                if (inventarioRemoto == null) {
                                    val nombreProducto = item.nombreProducto ?: "Producto"
                                    val msg = "No pudimos verificar la disponibilidad de $nombreProducto (talla $talla). Verifica al finalizar la compra"
                                    ajustes.add(msg)
                                    continue
                                } else if (inventarioRemoto.cantidad < item.cantidad) {
                                     val nuevaCantidad = inventarioRemoto.cantidad
                                     val nombreProducto = item.nombreProducto ?: "el producto"
                                     if (nuevaCantidad <= 0) {
                                         val msg = "Lo sentimos, $nombreProducto (talla $talla) se agotó. Por favor elimínalo de tu carrito"
                                         ajustes.add(msg)
                                         continue
                                     } else {
                                         val currentUser = authRemoteRepository.currentUser.value
                                         val req = CartItemRequest(
                                             id = item.id,
                                             clienteId = item.clienteId,
                                             modeloId = item.modeloId,
                                             talla = talla,
                                             cantidad = nuevaCantidad,
                                             precioUnitario = item.precioUnitario,
                                             nombreProducto = item.nombreProducto
                                         )
                                        if (req.id != null && req.id != 0L) {
                                            cartRepository.addOrUpdate(req)
                                        } else {
                                            val idCli = currentUser?.idPersona ?: item.clienteId
                                            cartRepository.addAndReturnCart(req, idCli)
                                        }
                                         ajustes.add("Solo quedan $nuevaCantidad unidad(es) de $nombreProducto. Hemos ajustado tu carrito")
                                     }
                                 }
                            } catch (_: Exception) {
                             }
                         }

                        // Mapeo: por cada item obtener ProductoDTO remoto (si posible)
                        val itemsUi = mutableListOf<CartItemUi>()
                        for (it in lista) {
                            itemsUi.add(mapToCartItemUi(it))
                        }

                        val total = lista.sumOf { it.cantidad.toLong() * it.precioUnitario }

                        _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi, total = total)

                        // Si hubo ajustes, anotar mensaje de error para UI (no bloquear)
                        if (ajustes.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(error = ajustes.joinToString("; "))
                        }

                    } catch (_: Exception) {
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun incrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            // prevenir que un pendingEmptyJob borre la UI mientras actualizamos localmente
            pendingEmptyJob?.cancel()
            val item = itemUi.cartItem
            _uiState.value = _uiState.value.copy(error = null)

            try {
                val result = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)
                val inventarioRemoto = try {
                    result.getOrNull()?.firstOrNull { inv ->
                        val invTalla = inv.talla
                        val invModeloKey = inv.productoId
                        invModeloKey == item.modeloId && invTalla.trim().equals(item.talla.trim(), ignoreCase = true)
                    }
                } catch (e: Exception) { null }

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
                    talla = item.talla.trim(),
                    cantidad = nuevaCantidad,
                    precioUnitario = item.precioUnitario,
                    nombreProducto = item.nombreProducto
                )
                // Usar addOrUpdate para actualizaciones (update-only)
                cartRepository.addOrUpdate(req)
                 // Forzar actualización inmediata del UI desde el snapshot local para evitar que una emisión remota posterior borre la UI
                 val snapAfter = cartRepository.getCacheSnapshot(current?.idPersona ?: item.clienteId)
                val itemsUiAfter = mutableListOf<CartItemUi>()
                for (it in snapAfter) { itemsUiAfter.add(mapToCartItemUi(it)) }
                 val totalAfter = snapAfter.sumOf { it.cantidad.toLong() * it.precioUnitario }
                _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUiAfter, total = totalAfter)
                 // loadCart() will receive the updated list via collect

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error de conexión: ${e.message}")
            }
        }
    }

    fun decrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            pendingEmptyJob?.cancel()
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val item = itemUi.cartItem
                if (item.cantidad <= 1) {
                    cartRepository.deleteById(item.clienteId, item.id ?: 0L)
                    // actualizar UI desde snapshot
                    val snapDel = cartRepository.getCacheSnapshot(item.clienteId)
                    val itemsUiDel = mutableListOf<CartItemUi>()
                    for (it in snapDel) { itemsUiDel.add(mapToCartItemUi(it)) }
                    val totalDel = snapDel.sumOf { it.cantidad.toLong() * it.precioUnitario }
                    _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUiDel, total = totalDel)
                } else {
                    val updatedReq = CartItemRequest(
                        id = item.id,
                        clienteId = item.clienteId,
                        modeloId = item.modeloId,
                        talla = item.talla.trim(),
                        cantidad = item.cantidad - 1,
                        precioUnitario = item.precioUnitario,
                        nombreProducto = item.nombreProducto
                    )
                    cartRepository.addOrUpdate(updatedReq)
                    // actualizar UI desde snapshot
                    val snapUpd = cartRepository.getCacheSnapshot(item.clienteId)
                    val itemsUiUpd = mutableListOf<CartItemUi>()
                    for (it in snapUpd) { itemsUiUpd.add(mapToCartItemUi(it)) }
                    val totalUpd = snapUpd.sumOf { it.cantidad.toLong() * it.precioUnitario }
                    _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUiUpd, total = totalUpd)
                }
                // loadCart() will receive the updated list via collect
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error: ${e.message}")
                // loadCart() will handle emission
            }
        }
    }

    fun removeItem(itemUi: CartItemUi) {
        viewModelScope.launch {
            pendingEmptyJob?.cancel()
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val item = itemUi.cartItem
                cartRepository.deleteById(item.clienteId, item.id ?: 0L)
                // actualizar UI desde snapshot
                val snapRem = cartRepository.getCacheSnapshot(item.clienteId)
                val itemsUiRem = mutableListOf<CartItemUi>()
                for (it in snapRem) { itemsUiRem.add(mapToCartItemUi(it)) }
                val totalRem = snapRem.sumOf { it.cantidad.toLong() * it.precioUnitario }
                _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUiRem, total = totalRem)
                // loadCart() will receive the updated list via collect
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar: ${e.message}")
                // loadCart() will handle emission
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            pendingEmptyJob?.cancel()
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val current = authRemoteRepository.currentUser.value ?: return@launch
                cartRepository.clear(current.idPersona)
                // actualizar UI desde snapshot (vacío)
                _uiState.value = _uiState.value.copy(isLoading = false, items = emptyList(), total = 0L)
                // loadCart() will receive the updated list via collect
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al vaciar carrito: ${e.message}")
                // loadCart() will handle emission
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingOut = true, error = null)
            try {
                val current = authRemoteRepository.currentUser.value ?: return@launch
                val idCliente = current.idPersona
                // Usar los items que muestra la UI en este momento para evitar race conditions
                val uiItems = _uiState.value.items
                val items = uiItems.map { it.cartItem }

                if (items.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Tu carrito está vacío. Agrega productos antes de finalizar la compra")
                    return@launch
                }

                //  Construir lista de detalles para el Backend
                val detallesDTO = mutableListOf<DetalleBoletaDTO>()
                val checkoutErrors = mutableListOf<String>()

                // obtener los IDs de inventario reales desde el backend y re-verificar stock
                for (item in items) {
                    // Normalizar talla: manejar null o la cadena literal "null"
                    val talla = item.talla.trim()
                    val nombreProducto = item.nombreProducto ?: "Producto"

                    if (talla.isBlank() || talla.equals("null", ignoreCase = true)) {
                        checkoutErrors.add("Por favor selecciona una talla para $nombreProducto antes de continuar")
                        continue
                    }
                    // Usar versión "logged" (intenta rutas alternativas) y matching robusto
                    val invResult = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)
                    val remoto = try {
                        invResult.getOrNull()?.firstOrNull { inv ->
                            val invTalla = inv.talla
                            val invModeloKey = inv.productoId
                            val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                            } else false
                            (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                        }
                    } catch (e: Exception) { null }

                    if (remoto == null) {
                        checkoutErrors.add("Lo sentimos, $nombreProducto (talla $talla) ya no está disponible")
                        continue
                    }

                    // Re-verificación final: volver a consultar cantidad justo antes de agregar
                    val invFinal = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)
                    val remotoFinal = try {
                        invFinal.getOrNull()?.firstOrNull { inv ->
                            val invTalla = inv.talla
                            val invModeloKey = inv.productoId
                            val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                            } else false
                            (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                        }
                    } catch (e: Exception) { null }

                    if (remotoFinal == null || remotoFinal.cantidad < item.cantidad) {
                        val disponible = remotoFinal?.cantidad ?: 0
                        if (disponible == 0) {
                            checkoutErrors.add("Lo sentimos, $nombreProducto (talla $talla) se agotó. Alguien más completó su compra antes")
                        } else {
                            checkoutErrors.add("Solo quedan $disponible unidad(es) de $nombreProducto (talla $talla). Por favor ajusta la cantidad en tu carrito")
                        }
                        continue
                    }

                    detallesDTO.add(
                        DetalleBoletaDTO(
                            id = null,
                            boletaId = null,
                            inventarioId = remotoFinal.id ?: 0L,
                            nombreProducto = remotoFinal.nombre,
                            talla = talla,
                            cantidad = item.cantidad,
                            precioUnitario = item.precioUnitario,
                            subtotal = item.precioUnitario * item.cantidad
                        )
                    )
                }

                // Si hubo errores de validación, informar al usuario y cancelar checkout (no se borra el carrito)
                if (checkoutErrors.isNotEmpty()) {
                    val errorMsg = if (checkoutErrors.size == 1) {
                        checkoutErrors.first()
                    } else {
                        "Algunos productos no están disponibles:\n\n" + checkoutErrors.joinToString("\n\n")
                    }
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = errorMsg)
                    return@launch
                }

                // Asegurarse que tenemos detalles válidos
                if (detallesDTO.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Tu carrito no tiene artículos válidos para procesar. Por favor verifica los productos")
                    return@launch
                }

                // Validar inventarioId en detalles
                val invalidDetalle = detallesDTO.firstOrNull { it.inventarioId <= 0L }
                if (invalidDetalle != null) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Hubo un problema al procesar ${invalidDetalle.nombreProducto ?: "un producto"}. Por favor intenta nuevamente")
                    return@launch
                }

                val request = CrearBoletaRequest(
                    clienteId = idCliente,
                    metodoPago = "App Android",
                    observaciones = "Dirección de entrega: ${_uiState.value.address}",
                    detalles = detallesDTO.map { d -> d.copy(subtotal = (d.precioUnitario * d.cantidad)) }
                )


                val response = ventasRepository.crearBoleta(request)

                if (response.isSuccess) {
                    val boletaCreada = response.getOrNull()
                    val boletaId = boletaCreada?.id

                    if (boletaId != null) {
                        try {
                            val entregaRequest = com.example.proyectoZapateria.data.remote.entregas.dto.CrearEntregaRequest(
                                idBoleta = boletaId,
                                idTransportista = null,
                                estadoEntrega = "pendiente",
                                observacion = "Dirección: ${_uiState.value.address}"
                            )
                            entregasRepository.crearEntrega(entregaRequest)
                        } catch (_: Exception) {
                        }
                    }

                    cartRepository.clear(idCliente)
                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        checkoutSuccess = true,
                        checkoutMessage = "¡Compra exitosa! Boleta #$boletaId",
                        shouldNavigateToHome = true
                    )
                } else {
                    val err = response.exceptionOrNull()
                    val errMsg = err?.message ?: "Error desconocido"
                    val bodyIndex = errMsg.indexOf(" - body=")
                    val serverBody = if (bodyIndex >= 0) errMsg.substring(bodyIndex + 8) else null

                    val userFriendlyMsg = when {
                        serverBody?.contains("stock", ignoreCase = true) == true ||
                        errMsg.contains("stock", ignoreCase = true) -> {
                            "Lo sentimos, uno o más productos se agotaron mientras procesábamos tu pedido. Por favor revisa tu carrito e intenta nuevamente"
                        }
                        serverBody?.contains("inventory", ignoreCase = true) == true ||
                        errMsg.contains("inventory", ignoreCase = true) -> {
                            "Lo sentimos, hubo un problema verificando la disponibilidad de los productos. Por favor intenta nuevamente"
                        }
                        errMsg.contains("timeout", ignoreCase = true) ||
                        errMsg.contains("connection", ignoreCase = true) -> {
                            "No pudimos conectarnos con el servidor. Por favor verifica tu conexión a internet e intenta nuevamente"
                        }
                        else -> {
                            "Ocurrió un error al procesar tu compra. Por favor intenta nuevamente en unos momentos"
                        }
                    }

                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = userFriendlyMsg)
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCheckingOut = false, error = e.message)
            }
        }
    }

    fun resetNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToHome = false)
    }

    // Mapea un CartItemResponse a CartItemUi obteniendo ProductoDTO y bytes de imagen si están disponibles
    private suspend fun mapToCartItemUi(item: CartItemResponse): CartItemUi {
        val modeloRes = try { inventarioRepository.getModeloById(item.modeloId) } catch (e: Exception) { Result.failure<ProductoDTO>(e) }
        val producto = modeloRes.getOrNull()
        var bytes: ByteArray? = null
        try {
            val imgRes = inventarioRepository.obtenerImagenProducto(item.modeloId)
            if (imgRes.isSuccess) bytes = imgRes.getOrNull()
        } catch (_: Exception) { /* ignore image errors */ }
        return CartItemUi(item, producto, bytes)
    }
}
