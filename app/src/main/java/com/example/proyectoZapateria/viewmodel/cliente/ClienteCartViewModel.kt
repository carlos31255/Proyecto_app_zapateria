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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson

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

                // --- Empezar con snapshot del cache en memoria para una respuesta instantánea ---
                try {
                    val snapshot = cartRepository.getCacheSnapshot(current.idPersona)
                    if (snapshot.isNotEmpty()) {
                        Log.d("ClienteCartVM", "loadCart: using cache snapshot size=${snapshot.size} for cliente=${current.idPersona}")
                        val itemsUi = mutableListOf<CartItemUi>()
                        for (it in snapshot) {
                            itemsUi.add(mapToCartItemUi(it))
                        }
                        val total = snapshot.sumOf { it.cantidad.toLong() * it.precioUnitario }
                        _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi, total = total)
                    }
                } catch (e: Exception) {
                    Log.w("ClienteCartVM", "Failed to read cache snapshot: ${e.message}")
                }

                // small retry: si UI sigue vacía, intentar leer snapshot después de 200ms para cubrir condiciones de carrera
                val initialSnapshotRetryJob = launch {
                    delay(200)
                    if (_uiState.value.items.isEmpty()) {
                        try {
                            val snap2 = cartRepository.getCacheSnapshot(current.idPersona)
                            if (snap2.isNotEmpty()) {
                                Log.d("ClienteCartVM", "loadCart: retry snapshot found size=${snap2.size} for cliente=${current.idPersona}")
                                val itemsUi2 = mutableListOf<CartItemUi>()
                                for (it in snap2) {
                                    itemsUi2.add(mapToCartItemUi(it))
                                }
                                val total2 = snap2.sumOf { it.cantidad.toLong() * it.precioUnitario }
                                _uiState.value = _uiState.value.copy(isLoading = false, items = itemsUi2, total = total2)
                            }
                        } catch (e: Exception) {
                            Log.w("ClienteCartVM", "retry snapshot failed: ${e.message}")
                        }
                    }
                }

                // Suscribirse al flow del repositorio para recibir emisiones futuras
                cartRepository.getCartForCliente(current.idPersona).collect { lista ->
                    try {
                        Log.d("ClienteCartVM", "loadCart: received lista.size=${lista.size} for cliente=${current.idPersona}")

                        // Cancelar el job de retry si la emisión llegó
                        if (initialSnapshotRetryJob.isActive) initialSnapshotRetryJob.cancel()

                        // Si la emisión es vacía pero la UI ya muestra items, confirmar antes de limpiar (evita parpadeos)
                        if (lista.isEmpty() && _uiState.value.items.isNotEmpty()) {
                            // cancelar cualquier chequeo previo y programar uno nuevo
                            pendingEmptyJob?.cancel()
                            pendingEmptyJob = launch {
                                // esperar un poco más para confirmar que no es una emisión transitoria
                                delay(1500)
                                 // si después del delay el snapshot remoto/local sigue vacío, entonces limpiar UI
                                 val snap = cartRepository.getCacheSnapshot(current.idPersona)
                                 // Si hubo una actualización local reciente, no confirmar vacío (evitar race with local writes)
                                 val hadRecentLocal = cartRepository.hasRecentLocalUpdate(current.idPersona, 8000)
                                 val hasPending = cartRepository.hasPendingLocalChanges(current.idPersona)
                                 if (hadRecentLocal || hasPending) {
                                     Log.d("ClienteCartVM", "loadCart: recent local update or pending local changes detected; ignoring transient empty emission for cliente=${current.idPersona} hadRecentLocal=$hadRecentLocal hasPending=$hasPending")
                                     return@launch
                                }
                                 if (snap.isEmpty()) {
                                     Log.d("ClienteCartVM", "loadCart: confirmed empty snapshot after delay for cliente=${current.idPersona}, updating UI to empty")
                                     _uiState.value = _uiState.value.copy(isLoading = false, items = emptyList(), total = 0L)
                                 } else {
                                     Log.d("ClienteCartVM", "loadCart: snapshot not empty after delay (size=${snap.size}), ignoring transient empty emission")
                                 }
                             }
                             return@collect
                         }

                        // Verificar stock remoto y ajustar/eliminar items si es necesario
                        val ajustes = mutableListOf<String>()
                        for (item in lista) {
                            try {
                                // Normalizar talla para usar en comparaciones y mensajes
                                val talla = item.talla.trim()

                                // Si el backend aún no proporcionó la talla para este item, no intentar verificar.
                                if (talla.isBlank() || talla.equals("null", ignoreCase = true)) {
                                    Log.d("ClienteCartVM", "Item modelo=${item.modeloId} sin talla aún; esperando actualización del servidor")
                                    continue
                                }

                                // Usar la versión "logged" que intenta rutas alternativas (alt/query)
                                val invResult = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)

                                // Debug: loguear resultado remoto para depuración
                                try {
                                    val listaRemota = invResult.getOrNull()
                                    Log.d("ClienteCartVM", "invResult success=${invResult.isSuccess} size=${listaRemota?.size ?: 0} for modelo=${item.modeloId}")
                                    listaRemota?.take(5)?.forEach { inv ->
                                        Log.d("ClienteCartVM", "  inv[id=${inv.id} productoId=${inv.productoId} modeloId=${inv.modeloId} talla='${inv.talla}' cantidad=${inv.cantidad} nombre='${inv.nombre}']")
                                    }
                                } catch (_: Exception) {}

                                val inventarioRemoto = try {
                                    val list = invResult.getOrNull() ?: emptyList()
                                    val primary = list.firstOrNull { inv ->
                                        val invTalla = inv.talla
                                        val invModeloKey = inv.modeloId ?: inv.productoId
                                        val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                            inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                                        } else false
                                        (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                                    }
                                    if (primary != null) primary else {
                                        // fallback: match only by talla
                                        val byTalla = list.firstOrNull { it.talla.trim().equals(talla, ignoreCase = true) }
                                        if (byTalla != null) {
                                            Log.w("ClienteCartVM", "Fallback match by talla para modelo=${item.modeloId}: encontrada entrada invId=${byTalla.id} productoId=${byTalla.productoId} modeloId=${byTalla.modeloId}")
                                        }
                                        byTalla
                                    }
                                } catch (e: Exception) {
                                    null
                                }

                                if (inventarioRemoto == null) {
                                    // No se pudo verificar la talla/stock remoto. No borrar automáticamente.
                                    val msg = "No se pudo verificar stock para producto ${item.modeloId} (talla ${item.talla}); se mantuvo en el carrito para verificación en checkout"
                                    Log.w("ClienteCartVM", msg)
                                    ajustes.add(msg)
                                    continue
                                } else if (inventarioRemoto.cantidad < item.cantidad) {
                                     val nuevaCantidad = inventarioRemoto.cantidad
                                     if (nuevaCantidad <= 0) {
                                         val msg = "Producto ${item.modeloId} (talla $talla) sin stock. Se mantiene en el carrito para revisión en checkout"
                                         Log.w("ClienteCartVM", msg)
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
                                         ajustes.add("Cantidad de ${item.modeloId} ajustada a $nuevaCantidad por stock")
                                     }
                                 }
                            } catch (e: Exception) {
                                 Log.w("ClienteCartVM", "No se pudo verificar stock remoto para modelo ${item.modeloId}")
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

                    } catch (e: Exception) {
                        Log.e("ClienteCartVM", "Error processing cart emission: ${e.message}")
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
                        val invModeloKey = inv.modeloId ?: inv.productoId
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
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Carrito vacío")
                    return@launch
                }

                //  Construir lista de detalles para el Backend
                val detallesDTO = mutableListOf<DetalleBoletaDTO>()
                val checkoutErrors = mutableListOf<String>()

                // obtener los IDs de inventario reales desde el backend y re-verificar stock
                for (item in items) {
                    // Normalizar talla: manejar null o la cadena literal "null"
                    val talla = item.talla.trim()
                    if (talla.isBlank() || talla.equals("null", ignoreCase = true)) {
                        checkoutErrors.add("Seleccione talla para el producto ${item.modeloId} (${item.nombreProducto ?: ""})")
                        continue
                    }
                    // Usar versión "logged" (intenta rutas alternativas) y matching robusto
                    val invResult = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)
                    val remoto = try {
                        invResult.getOrNull()?.firstOrNull { inv ->
                            val invTalla = inv.talla
                            val invModeloKey = inv.modeloId ?: inv.productoId
                            val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                            } else false
                            (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                        }
                    } catch (e: Exception) { null }

                    if (remoto == null) {
                        checkoutErrors.add("Producto ${item.modeloId} (talla $talla) no disponible")
                        continue
                    }

                    // Re-verificación final: volver a consultar cantidad justo antes de agregar
                    val invFinal = inventarioRepository.getInventarioPorModeloLogged(item.modeloId)
                    val remotoFinal = try {
                        invFinal.getOrNull()?.firstOrNull { inv ->
                            val invTalla = inv.talla
                            val invModeloKey = inv.modeloId ?: inv.productoId
                            val nameMatch = if (inv.nombre.isNotBlank() && !item.nombreProducto.isNullOrBlank()) {
                                inv.nombre.trim().equals(item.nombreProducto.trim(), ignoreCase = true)
                            } else false
                            (invModeloKey == item.modeloId || nameMatch) && invTalla.trim().equals(talla, ignoreCase = true)
                        }
                    } catch (e: Exception) { null }

                    if (remotoFinal == null || remotoFinal.cantidad < item.cantidad) {
                        checkoutErrors.add("Stock insuficiente para producto ${item.modeloId} (talla $talla): disponible=${remotoFinal?.cantidad ?: 0}, requerido=${item.cantidad}")
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
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = checkoutErrors.joinToString("; "))
                    return@launch
                }

                // Asegurarse que tenemos detalles válidos
                if (detallesDTO.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "No hay artículos válidos para procesar")
                    return@launch
                }

                // Validar inventarioId en detalles
                val invalidDetalle = detallesDTO.firstOrNull { it.inventarioId <= 0L }
                if (invalidDetalle != null) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Detalle inválido sin inventarioId para producto: ${invalidDetalle.nombreProducto ?: invalidDetalle.id}")
                    return@launch
                }

                val request = CrearBoletaRequest(
                    clienteId = idCliente,
                    metodoPago = "App Android",
                    observaciones = "Dirección de entrega: ${_uiState.value.address}",
                    detalles = detallesDTO.map { d -> d.copy(subtotal = (d.precioUnitario * d.cantidad)) }
                )

                Log.d("ClienteCartVM", "checkout: enviando crearBoleta request clienteId=$idCliente detalles=${request.detalles.size}")

                // Log request body JSON para depuración de error 500 en backend
                try {
                    val gson = Gson()
                    val jsonReq = gson.toJson(request)
                    Log.d("ClienteCartVM", "checkout: requestJson=${jsonReq}")
                } catch (_: Exception) {}

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
                    val err = response.exceptionOrNull()
                    val errMsg = err?.message ?: "Error desconocido"
                    // Extraer el body si NetworkUtils lo agregó como ' - body=...'
                    val bodyIndex = errMsg.indexOf(" - body=")
                    val serverBody = if (bodyIndex >= 0) errMsg.substring(bodyIndex + 8) else null
                    Log.e("ClienteCartVM", "checkout: crearBoleta failed: $errMsg")
                    if (!serverBody.isNullOrBlank()) {
                        Log.e("ClienteCartVM", "checkout: server body: $serverBody")
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Error del servidor: ${serverBody.take(800)}")
                    } else {
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Error: $errMsg")
                    }
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
