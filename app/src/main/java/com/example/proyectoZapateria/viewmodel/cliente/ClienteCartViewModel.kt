package com.example.proyectoZapateria.viewmodel.cliente

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.detalleboleta.DetalleBoletaEntity
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.ClienteRepository
import com.example.proyectoZapateria.data.repository.AuthRepository
import com.example.proyectoZapateria.data.repository.ModeloZapatoRepository
import com.example.proyectoZapateria.data.repository.DetalleBoletaRepository
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.TallaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.data.local.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import com.example.proyectoZapateria.data.repository.PersonaRepository

@HiltViewModel
class ClienteCartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val modeloRepository: ModeloZapatoRepository,
    private val detalleBoletaRepository: DetalleBoletaRepository,
    private val inventarioRepository: InventarioRepository,
    private val tallaRepository: TallaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val boletaVentaDao: BoletaVentaDao,
    private val clienteRepository: ClienteRepository,
    private val appDatabase: AppDatabase,
    private val personaRepository: PersonaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class CartItemUi(
        val cartItem: CartItemEntity,
        val modelo: ModeloZapatoEntity?
    )

    data class UiState(
        val isLoading: Boolean = true,
        val items: List<CartItemUi> = emptyList(),
        val total: Int = 0,
        val error: String? = null,
        val isCheckingOut: Boolean = false,
        val checkoutMessage: String? = null,
        val checkoutSuccess: Boolean = false,
        val shouldNavigateToHome: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = authRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }

                val idCliente = current.idPersona
                val flow = cartRepository.getCartForCliente(idCliente)
                val lista = flow.first()

                // Mapear cada cartItem a su modelo
                val enriched = lista.map { cartItem ->
                    val modelo = modeloRepository.getModeloById(cartItem.idModelo)
                    CartItemUi(cartItem = cartItem, modelo = modelo)
                }

                val total = enriched.sumOf { (it.modelo?.precioUnitario ?: it.cartItem.precioUnitario) * it.cartItem.cantidad }

                _uiState.value = _uiState.value.copy(isLoading = false, items = enriched, total = total)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun incrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            val item = itemUi.cartItem
            try {
                val nuevaCantidad = item.cantidad + 1
                // Obtener talla y validar inventario
                val tallaEntity = tallaRepository.getByNumero(item.talla)
                if (tallaEntity == null) {
                    _uiState.value = _uiState.value.copy(error = "Talla no encontrada")
                    return@launch
                }
                val inv = inventarioRepository.getByModeloYTalla(item.idModelo, tallaEntity.idTalla)
                if (inv == null) {
                    _uiState.value = _uiState.value.copy(error = "Inventario no encontrado para modelo ${item.idModelo} talla ${item.talla}")
                    return@launch
                }
                if (inv.stockActual < nuevaCantidad) {
                    _uiState.value = _uiState.value.copy(error = "Stock insuficiente: disponible=${inv.stockActual}, requerido=$nuevaCantidad")
                    return@launch
                }

                val updated = item.copy(cantidad = nuevaCantidad)
                cartRepository.addOrUpdate(updated)
                loadCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Error al actualizar cantidad")
            }
        }
    }

    fun decrementQuantity(itemUi: CartItemUi) {
        viewModelScope.launch {
            val item = itemUi.cartItem
            if (item.cantidad <= 1) {
                cartRepository.delete(item)
            } else {
                val updated = item.copy(cantidad = item.cantidad - 1)
                cartRepository.addOrUpdate(updated)
            }
            loadCart()
        }
    }

    fun removeItem(itemUi: CartItemUi) {
        viewModelScope.launch {
            cartRepository.delete(itemUi.cartItem)
            loadCart()
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val current = authRepository.currentUser.value ?: return@launch
            cartRepository.clear(current.idPersona)
            loadCart()
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingOut = true, checkoutMessage = null, error = null)
            try {
                val current = authRepository.currentUser.value
                if (current == null) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, checkoutMessage = null, error = "No hay sesión activa")
                    return@launch
                }

                val idCliente = current.idPersona

                // Verificar que el cliente existe
                val cliente = clienteRepository.getByIdConPersona(idCliente)
                var needsCreateCliente = false
                if (cliente == null) {
                    // En vez de fallar, marcamos que debemos crear la fila en cliente (si existe persona)
                    // Comprobar que la persona existe antes de crear cliente
                    val persona = personaRepository.getPersonaById(idCliente)
                    if (persona == null) {
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Persona (id=$idCliente) no existe; no se puede crear cliente para la boleta")
                        return@launch
                    }
                    needsCreateCliente = true
                }

                val cartItems = cartRepository.getCartForCliente(idCliente).first()
                if (cartItems.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, checkoutMessage = null, error = "El carrito está vacío")
                    return@launch
                }

                // Pre-validar inventario y tallas para todos los items
                val inventarios = mutableListOf<Pair<CartItemEntity, com.example.proyectoZapateria.data.local.inventario.InventarioEntity>>()
                for (ci in cartItems) {
                    val tallaEntity = tallaRepository.getByNumero(ci.talla)
                    if (tallaEntity == null) {
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Talla no encontrada: ${ci.talla}")
                        return@launch
                    }
                    val inv = inventarioRepository.getByModeloYTalla(ci.idModelo, tallaEntity.idTalla)
                    if (inv == null) {
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Inventario no encontrado para modelo ${ci.idModelo} talla ${ci.talla}")
                        return@launch
                    }
                    if (inv.stockActual < ci.cantidad) {
                        _uiState.value = _uiState.value.copy(isCheckingOut = false, error = "Stock insuficiente para modelo ${ci.idModelo} talla ${ci.talla}")
                        return@launch
                    }
                    inventarios.add(ci to inv)
                }

                // Calcular total
                var montoTotal = 0
                cartItems.forEach { ci ->
                    val modelo = modeloRepository.getModeloById(ci.idModelo)
                    val precio = modelo?.precioUnitario ?: ci.precioUnitario
                    montoTotal += precio * ci.cantidad
                }

                // Generar numero de boleta único (timestamp)
                val numeroBoleta = "B-${System.currentTimeMillis()}"

                // Asignar vendedor automáticamente: preferir primer usuario con rol Vendedor (2), si no existe usar Admin (1)
                var idVendedorFinal: Int?
                try {
                    val vendedores = usuarioRepository.getUsuariosByRol(2).first()
                    idVendedorFinal = vendedores.firstOrNull()?.idPersona
                    if (idVendedorFinal == null) {
                        val admins = usuarioRepository.getUsuariosByRol(1).first()
                        idVendedorFinal = admins.firstOrNull()?.idPersona
                    }
                } catch (_: Exception) {
                    // Si algo falla al consultar, dejamos idVendedorFinal en null
                    idVendedorFinal = null
                }

                try {
                    // Ejecutar todo dentro de una transacción para evitar constraint failures parciales
                    appDatabase.withTransaction {
                        // Si hace falta, crear la fila cliente para evitar FK fail
                        if (needsCreateCliente) {
                            val clienteEntity = ClienteEntity(idPersona = idCliente, categoria = null)
                            appDatabase.clienteDao().insert(clienteEntity)
                        }
                        // Insertar boleta
                        val boleta = BoletaVentaEntity(
                            idBoleta = 0,
                            numeroBoleta = numeroBoleta,
                            idVendedor = idVendedorFinal,
                            idCliente = idCliente,
                            montoTotal = montoTotal,
                            fecha = System.currentTimeMillis()
                        )

                        val idBoletaLong = boletaVentaDao.insert(boleta)
                        val idBoletaInt = idBoletaLong.toInt()

                        // Insertar detalles y actualizar inventario
                        for ((ci, inv) in inventarios) {
                            val precioUnitario = modeloRepository.getModeloById(ci.idModelo)?.precioUnitario ?: ci.precioUnitario
                            val subtotal = precioUnitario * ci.cantidad
                            val detalle = DetalleBoletaEntity(
                                idDetalle = 0,
                                idBoleta = idBoletaInt,
                                idInventario = inv.idInventario,
                                cantidad = ci.cantidad,
                                precioUnitario = precioUnitario,
                                subtotal = subtotal
                            )

                            detalleBoletaRepository.insertDetalle(detalle)

                            val actualizado = inv.copy(stockActual = inv.stockActual - ci.cantidad)
                            inventarioRepository.updateInventario(actualizado)
                        }
                    }
                } catch (sqle: SQLiteConstraintException) {
                    // Diagnóstico: comprobar existencia de tablas padre para cada FK
                    val diagnostics = StringBuilder()
                    diagnostics.append("SQLiteConstraintException: ${sqle.message}\n")
                    // Cliente
                    val cli = clienteRepository.getByIdConPersona(idCliente)
                    diagnostics.append("Cliente (id=$idCliente) exists: ${cli != null}\n")
                    // Inventarios
                    for ((ci, inv) in inventarios) {
                        val invCheck = inventarioRepository.getById(inv.idInventario)
                        diagnostics.append("Inventario id=${inv.idInventario} for modelo=${ci.idModelo} talla=${ci.talla} exists: ${invCheck != null}\n")
                    }

                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        checkoutMessage = null,
                        error = "Compra fallida: ${diagnostics}",
                        checkoutSuccess = false,
                        shouldNavigateToHome = false
                    )
                    return@launch
                }

                // Limpiar carrito (fuera de la transacción ya que se modifican tablas independientes)
                cartRepository.clear(idCliente)

                // Refrescar vista
                loadCart()

                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    checkoutMessage = "Compra finalizada correctamente",
                    checkoutSuccess = true,
                    shouldNavigateToHome = true,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    checkoutMessage = null,
                    error = "Compra fallida: ${e.message}",
                    checkoutSuccess = false,
                    shouldNavigateToHome = false
                )
            }
        }
    }

    fun resetNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToHome = false)
    }
}
