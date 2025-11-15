//package com.example.proyectoZapateria.viewmodel.cliente
//
//import android.database.sqlite.SQLiteConstraintException
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.room.withTransaction
//import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
//import com.example.proyectoZapateria.data.local.cart.CartItemEntity
//import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
//import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity
//import com.example.proyectoZapateria.data.repository.CartRepository
//import com.example.proyectoZapateria.data.repository.AuthRepository
//import com.example.proyectoZapateria.data.repository.ModeloZapatoRepository
//import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
//import com.example.proyectoZapateria.data.repository.InventarioRepository
//import com.example.proyectoZapateria.data.repository.TallaRepository
//import com.example.proyectoZapateria.data.repository.EntregaRepository
//import com.example.proyectoZapateria.data.repository.UsuarioRemoteRepository
//import com.example.proyectoZapateria.data.repository.ClienteRemoteRepository
//import com.example.proyectoZapateria.data.repository.PersonaRemoteRepository
//import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
//import com.example.proyectoZapateria.data.local.database.AppDatabase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class ClienteCartViewModel @Inject constructor(
//    private val cartRepository: CartRepository,
//    private val modeloRepository: ModeloZapatoRepository,
//    private val detalleBoletaRepository: DetalleBoletaRepository,
//    private val inventarioRepository: InventarioRepository,
//    private val tallaRepository: TallaRepository,
//    private val usuarioRemoteRepository: UsuarioRemoteRepository,
//    private val boletaVentaDao: BoletaVentaDao,
//    private val clienteRemoteRepository: ClienteRemoteRepository,
//    private val appDatabase: AppDatabase,
//    private val personaRemoteRepository: PersonaRemoteRepository,
//    private val authRepository: AuthRepository,
//    private val entregaRepository: EntregaRepository
//) : ViewModel() {
//
//    data class CartItemUi(
//        val cartItem: CartItemEntity,
//        val modelo: ModeloZapatoEntity?
//    )
//
//    data class UiState(
//        val isLoading: Boolean = true,
//        val items: List<CartItemUi> = emptyList(),
//        val total: Long = 0L, // Cambiado a Long para evitar desbordamiento
//        val error: String? = null,
//        val isCheckingOut: Boolean = false,
//        val checkoutMessage: String? = null,
//        val checkoutSuccess: Boolean = false,
//        val shouldNavigateToHome: Boolean = false
//    )
//
//    private val _uiState = MutableStateFlow(UiState())
//    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
//
//    init {
//        loadCart()
//    }
//
//    fun loadCart() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//            try {
//                val current = authRepository.currentUser.value
//                if (current == null) {
//                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
//                    return@launch
//                }
//
//                val idCliente = current.idPersona
//                val flow = cartRepository.getCartForCliente(idCliente)
//                val lista = flow.first()
//
//                // Mapear cada cartItem a su modelo
//                val enriched = lista.map { cartItem ->
//                    val modelo = modeloRepository.getModeloById(cartItem.idModelo)
//                    CartItemUi(cartItem = cartItem, modelo = modelo)
//                }
//
//                // Calcular total usando el precio actual del modelo (no el guardado en cartItem)
//                val total = enriched.sumOf { itemUi ->
//                    val precio = itemUi.modelo?.precioUnitario ?: itemUi.cartItem.precioUnitario
//                    (precio.toLong() * itemUi.cartItem.cantidad.toLong())
//                }
//
//                _uiState.value = _uiState.value.copy(isLoading = false, items = enriched, total = total, error = null)
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
//            }
//        }
//    }
//
//    fun incrementQuantity(itemUi: CartItemUi) {
//        viewModelScope.launch {
//            val item = itemUi.cartItem
//            val nombreModelo = itemUi.modelo?.nombreModelo ?: "modelo ${item.idModelo}"
//
//            // Limpiar error previo
//            _uiState.value = _uiState.value.copy(error = null)
//
//            try {
//                val nuevaCantidad = item.cantidad + 1
//
//                // Obtener talla y validar inventario
//                val tallaEntity = tallaRepository.getByNumero(item.talla)
//                if (tallaEntity == null) {
//                    _uiState.value = _uiState.value.copy(error = "Talla ${item.talla} no encontrada")
//                    return@launch
//                }
//
//                val inv = inventarioRepository.getByModeloYTalla(item.idModelo, tallaEntity.idTalla)
//                if (inv == null) {
//                    _uiState.value = _uiState.value.copy(error = "No hay inventario disponible para $nombreModelo talla ${item.talla}")
//                    return@launch
//                }
//
//                // Validar stock ANTES de permitir incrementar
//                if (inv.stockActual < nuevaCantidad) {
//                    _uiState.value = _uiState.value.copy(
//                        error = "Has alcanzado el stock máximo de $nombreModelo talla ${item.talla}. Solo hay ${inv.stockActual} unidades disponibles."
//                    )
//                    return@launch
//                }
//
//                // Si pasa las validaciones, actualizar
//                val updated = item.copy(cantidad = nuevaCantidad)
//                cartRepository.addOrUpdate(updated)
//                loadCart()
//
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(error = "Error al actualizar cantidad: ${e.message}")
//                loadCart() // Recargar para mantener consistencia
//            }
//        }
//    }
//
//    fun decrementQuantity(itemUi: CartItemUi) {
//        viewModelScope.launch {
//            // Limpiar error previo
//            _uiState.value = _uiState.value.copy(error = null)
//
//            try {
//                val item = itemUi.cartItem
//                if (item.cantidad <= 1) {
//                    cartRepository.delete(item)
//                } else {
//                    val updated = item.copy(cantidad = item.cantidad - 1)
//                    cartRepository.addOrUpdate(updated)
//                }
//                loadCart()
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(error = "Error al actualizar cantidad: ${e.message}")
//                loadCart() // Recargar para mantener consistencia
//            }
//        }
//    }
//
//    fun removeItem(itemUi: CartItemUi) {
//        viewModelScope.launch {
//            // Limpiar error previo
//            _uiState.value = _uiState.value.copy(error = null)
//
//            try {
//                cartRepository.delete(itemUi.cartItem)
//                loadCart()
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(error = "Error al eliminar producto: ${e.message}")
//                loadCart() // Recargar para mantener consistencia
//            }
//        }
//    }
//
//    fun clearCart() {
//        viewModelScope.launch {
//            // Limpiar error previo
//            _uiState.value = _uiState.value.copy(error = null)
//
//            try {
//                val current = authRepository.currentUser.value
//                if (current == null) {
//                    _uiState.value = _uiState.value.copy(error = "No hay sesión activa")
//                    return@launch
//                }
//                cartRepository.clear(current.idPersona)
//                loadCart()
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(error = "Error al vaciar carrito: ${e.message}")
//                loadCart() // Recargar para mantener consistencia
//            }
//        }
//    }
//
//    fun checkout() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isCheckingOut = true, checkoutMessage = null, error = null)
//            try {
//                val current = authRepository.currentUser.value
//                if (current == null) {
//                    _uiState.value = _uiState.value.copy(isCheckingOut = false, checkoutMessage = null, error = "No hay sesión activa")
//                    return@launch
//                }
//
//                val idCliente = current.idPersona
//
//                // Verificar que el cliente existe en la API remota
//                val clienteResult = clienteRemoteRepository.obtenerClientePorId(idCliente)
//                val personaResult = personaRemoteRepository.obtenerPersonaPorId(idCliente)
//
//                if (clienteResult.isFailure || personaResult.isFailure) {
//                    _uiState.value = _uiState.value.copy(
//                        isCheckingOut = false,
//                        error = "No se pudo verificar la información del cliente. Por favor, intente de nuevo."
//                    )
//                    return@launch
//                }
//
//                val cartItems = cartRepository.getCartForCliente(idCliente).first()
//                if (cartItems.isEmpty()) {
//                    _uiState.value = _uiState.value.copy(isCheckingOut = false, checkoutMessage = null, error = "El carrito está vacío")
//                    return@launch
//                }
//
//                // Pre-validar inventario y tallas para todos los items
//                val inventarios = mutableListOf<Pair<CartItemEntity, com.example.proyectoZapateria.data.local.inventario.InventarioEntity>>()
//                for (ci in cartItems) {
//                    val tallaEntity = tallaRepository.getByNumero(ci.talla)
//                    if (tallaEntity == null) {
//                        _uiState.value = _uiState.value.copy(
//                            isCheckingOut = false,
//                            error = "Talla ${ci.talla} no encontrada. Por favor, elimina este producto del carrito."
//                        )
//                        return@launch
//                    }
//                    val inv = inventarioRepository.getByModeloYTalla(ci.idModelo, tallaEntity.idTalla)
//                    if (inv == null) {
//                        val modelo = modeloRepository.getModeloById(ci.idModelo)
//                        val nombreModelo = modelo?.nombreModelo ?: "modelo ${ci.idModelo}"
//                        _uiState.value = _uiState.value.copy(
//                            isCheckingOut = false,
//                            error = "No hay inventario disponible para $nombreModelo talla ${ci.talla}. Por favor, elimina este producto del carrito."
//                        )
//                        return@launch
//                    }
//                    if (inv.stockActual < ci.cantidad) {
//                        val modelo = modeloRepository.getModeloById(ci.idModelo)
//                        val nombreModelo = modelo?.nombreModelo ?: "modelo ${ci.idModelo}"
//                        _uiState.value = _uiState.value.copy(
//                            isCheckingOut = false,
//                            error = "Stock insuficiente para $nombreModelo talla ${ci.talla}. Tienes ${ci.cantidad} en el carrito pero solo hay ${inv.stockActual} disponibles. Por favor, ajusta la cantidad."
//                        )
//                        return@launch
//                    }
//                    inventarios.add(ci to inv)
//                }
//
//                // Calcular total usando Long para evitar desbordamiento
//                var montoTotal = 0L
//                cartItems.forEach { ci ->
//                    val modelo = modeloRepository.getModeloById(ci.idModelo)
//                    val precio = modelo?.precioUnitario ?: ci.precioUnitario
//                    montoTotal += (precio.toLong() * ci.cantidad.toLong())
//                }
//
//                // Validar que el total no exceda el límite de Int para la base de datos
//                if (montoTotal > Int.MAX_VALUE) {
//                    _uiState.value = _uiState.value.copy(
//                        isCheckingOut = false,
//                        error = "El monto total excede el límite permitido"
//                    )
//                    return@launch
//                }
//
//                // Generar numero de boleta único (timestamp)
//                val numeroBoleta = "B-${System.currentTimeMillis()}"
//
//                // Obtener ID del admin para asignar como responsable de la venta (desde API)
//                var idAdminFinal: Int? = null
//                try {
//                    val adminsResult = usuarioRemoteRepository.obtenerUsuariosPorRol(1) // Rol 1 = Admin
//                    if (adminsResult.isSuccess) {
//                        val admins = adminsResult.getOrNull() ?: emptyList()
//                        idAdminFinal = admins.firstOrNull()?.idPersona
//                    }
//                } catch (_: Exception) {
//                    // Si algo falla al consultar, dejamos idAdminFinal en null
//                }
//
//                try {
//                    // Ejecutar todo dentro de una transacción para evitar constraint failures parciales
//                    appDatabase.withTransaction {
//                        // Ya no necesitamos crear ClienteEntity local porque el cliente existe en la API remota
//                        }
//                        // Insertar boleta
//                        val boleta = BoletaVentaEntity(
//                            idBoleta = 0,
//                            numeroBoleta = numeroBoleta,
//                            idVendedor = idAdminFinal,
//                            idCliente = idCliente,
//                            montoTotal = montoTotal.toInt(),
//                            fecha = System.currentTimeMillis()
//                        )
//
//                        val idBoletaLong = boletaVentaDao.insert(boleta)
//                        val idBoletaInt = idBoletaLong.toInt()
//
//                        // Insertar detalles y actualizar inventario
//                        for ((ci, inv) in inventarios) {
//                            val precioUnitario = modeloRepository.getModeloById(ci.idModelo)?.precioUnitario ?: ci.precioUnitario
//                            val subtotal = precioUnitario * ci.cantidad
//                            val detalle = DetalleBoletaEntity(
//                                idDetalle = 0,
//                                idBoleta = idBoletaInt,
//                                idInventario = inv.idInventario,
//                                cantidad = ci.cantidad,
//                                precioUnitario = precioUnitario,
//                                subtotal = subtotal
//                            )
//
//                            detalleBoletaRepository.insertDetalle(detalle)
//
//                            val actualizado = inv.copy(stockActual = inv.stockActual - ci.cantidad)
//                            inventarioRepository.updateInventario(actualizado)
//                        }
//
//                        // Crear entrega automáticamente
//                        // Obtener el primer transportista disponible (activo) desde API
//                        val transportistasResult = usuarioRemoteRepository.obtenerUsuariosPorRol(2) // Rol 2 = Transportista
//                        val idTransportista = if (transportistasResult.isSuccess) {
//                            val transportistas = transportistasResult.getOrNull() ?: emptyList()
//                            transportistas.firstOrNull()?.idPersona
//                        } else {
//                            null
//                        }
//
//                        // Obtener los datos de la persona cliente para la dirección de entrega desde API
//                        val personaClienteResult = personaRemoteRepository.obtenerPersonaPorId(idCliente)
//                        val direccionEntrega = if (personaClienteResult.isSuccess) {
//                            val personaCliente = personaClienteResult.getOrNull()
//                            if (personaCliente != null) {
//                                "${personaCliente.calle} ${personaCliente.numeroPuerta}"
//                            } else {
//                                "Dirección no disponible"
//                            }
//                        } else {
//                            "Dirección no disponible"
//                        }
//
//                        val nuevaEntrega = com.example.proyectoZapateria.data.local.entrega.EntregaEntity(
//                            idEntrega = 0,
//                            idBoleta = idBoletaInt,
//                            idTransportista = idTransportista, // Se asigna al primer transportista o null si no hay
//                            estadoEntrega = "pendiente",
//                            fechaAsignacion = System.currentTimeMillis(),
//                            fechaEntrega = null,
//                            observacion = "Entregar en: $direccionEntrega"
//                        )
//
//                        entregaRepository.insertEntrega(nuevaEntrega)
//                        android.util.Log.d("ClienteCartViewModel", "checkout: Entrega creada automáticamente para boleta $idBoletaInt, transportista=$idTransportista")
//                    }
//                } catch (sqle: SQLiteConstraintException) {
//                    // Diagnóstico: comprobar existencia de tablas padre para cada FK
//                    val diagnostics = StringBuilder()
//                    diagnostics.append("SQLiteConstraintException: ${sqle.message}\n")
//                    // Cliente (verificar en API)
//                    val cliResult = clienteRemoteRepository.obtenerClientePorId(idCliente)
//                    diagnostics.append("Cliente (id=$idCliente) exists in API: ${cliResult.isSuccess && cliResult.getOrNull() != null}\n")
//                    // Inventarios
//                    for ((ci, inv) in inventarios) {
//                        val invCheck = inventarioRepository.getById(inv.idInventario)
//                        diagnostics.append("Inventario id=${inv.idInventario} for modelo=${ci.idModelo} talla=${ci.talla} exists: ${invCheck != null}\n")
//                    }
//
//                    _uiState.value = _uiState.value.copy(
//                        isCheckingOut = false,
//                        checkoutMessage = null,
//                        error = "Compra fallida: ${diagnostics}",
//                        checkoutSuccess = false,
//                        shouldNavigateToHome = false
//                    )
//                    return@launch
//                }
//
//                // Limpiar carrito (fuera de la transacción ya que se modifican tablas independientes)
//                cartRepository.clear(idCliente)
//
//                // Refrescar vista
//                loadCart()
//
//                _uiState.value = _uiState.value.copy(
//                    isCheckingOut = false,
//                    checkoutMessage = "Compra finalizada correctamente",
//                    checkoutSuccess = true,
//                    shouldNavigateToHome = true,
//                    error = null
//                )
//
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isCheckingOut = false,
//                    checkoutMessage = null,
//                    error = "Compra fallida: ${e.message}",
//                    checkoutSuccess = false,
//                    shouldNavigateToHome = false
//                )
//            }
//        }
//    }
//
//    fun resetNavigationFlag() {
//        _uiState.value = _uiState.value.copy(shouldNavigateToHome = false)
//    }
//}
