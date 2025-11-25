package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.ui.model.InventarioUi
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import com.example.proyectoZapateria.data.repository.remote.CartRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteProductoDetailViewModel @Inject constructor(
    private val remoteRepository: InventarioRemoteRepository,
    private val cartRepository: CartRemoteRepository, // Remote wrapper over local for migration
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "ClienteProductoDetailVM"

    // El id viene como String en los argumentos de navegación; convertir a Long correctamente
    private val idModeloArg: Long = run {
        val raw = savedStateHandle.get<Any>("idModelo")
        when (raw) {
            is Long -> raw
            is Int -> raw.toLong()
            is String -> raw.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    // Ahora usamos ProductoDTO (remoto) en lugar de ModeloZapatoEntity local
    private val _producto = MutableStateFlow<ProductoDTO?>(null)
    val producto: StateFlow<ProductoDTO?> = _producto.asStateFlow()

    private val _inventario = MutableStateFlow<List<InventarioUi>>(emptyList())
    val inventario: StateFlow<List<InventarioUi>> = _inventario.asStateFlow()

    private val _comprando = MutableStateFlow(false)
    val comprando: StateFlow<Boolean> = _comprando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Map de idTalla -> numero  para mostrar en UI
    private val _tallasMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val tallasMap: StateFlow<Map<Long, String>> = _tallasMap.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount = _cartCount.asStateFlow()

    init {
        cargarDatos()
    }
    private fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "cargarDatos: iniciando para idModelo=$idModeloArg")
                if (idModeloArg <= 0L) {
                    _mensaje.value = "ID de producto inválido"
                    Log.w(TAG, "idModelo inválido: $idModeloArg")
                    return@launch
                }
                // 1. Cargar Detalle del Modelo (Producto)
                val modeloRes = remoteRepository.getModeloById(idModeloArg)
                if (modeloRes.isSuccess) {
                    val dto = modeloRes.getOrNull()
                    if (dto != null) {
                        _producto.value = dto
                    } else {
                        _mensaje.value = "Producto no encontrado"
                    }
                } else {
                    _mensaje.value = "No se pudo cargar el producto: ${modeloRes.exceptionOrNull()?.message}"
                }

                // 2. Cargar Tallas (para poder mapear el inventario correctamente)
                // Preferir las tallas asociadas al producto si el backend las expone
                val tallasResult = try {
                    remoteRepository.getTallasPorProducto(idModeloArg)
                } catch (e: Exception) {
                    Log.w(TAG, "getTallasPorProducto falló: ${e.message}; intentando global...")
                    remoteRepository.getTallasLogged()
                }
                Log.d(TAG, "TallasResult success=${tallasResult.isSuccess} exception=${tallasResult.exceptionOrNull()?.message}")
                if (tallasResult.isFailure) {
                    Log.w(TAG, "No se pudieron cargar tallas: ${tallasResult.exceptionOrNull()?.message}")
                }
                var listaTallas = if (tallasResult.isSuccess) tallasResult.getOrNull() ?: emptyList() else emptyList()

                // Si las tallas del producto vienen vacías, intentar obtener las tallas globales
                if (listaTallas.isEmpty()) {
                    try {
                        val global = remoteRepository.getTallasLogged()
                        if (global.isSuccess) listaTallas = global.getOrNull() ?: emptyList()
                    } catch (_: Exception) { /* ignore */ }
                }
                Log.d(TAG, "Tallas recibidas: count=${listaTallas.size} items=${listaTallas.take(5)}")

                // Actualizar mapa de tallas para la UI
                _tallasMap.value = listaTallas.associate { it.id to it.valor }

                // 3. Cargar Inventario del Modelo
                val invRes = remoteRepository.getInventarioPorModeloLogged(idModeloArg)
                Log.d(TAG, "InventarioResult success=${invRes.isSuccess} exception=${invRes.exceptionOrNull()?.message}")
                if (invRes.isSuccess) {
                    val dtos = invRes.getOrNull() ?: emptyList()
                    Log.d(TAG, "Inventario recibido: count=${dtos.size} sample=${dtos.take(5)}")
                    try {
                        // No filtrar por falta de mapping de talla: mostrar todas las tallas remotas
                        val listaLocal = dtos.map { dto ->
                            val tallaId = listaTallas.find { it.valor == dto.talla }?.id
                            InventarioUi(
                                idRemote = dto.id ?: 0L,
                                idModelo = idModeloArg,
                                talla = dto.talla,
                                tallaIdLocal = tallaId,
                                stock = dto.cantidad
                            )
                        }
                        _inventario.value = listaLocal
                        Log.d(TAG, "Inventario mapeado localmente: size=${listaLocal.size}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando inventario: ${e.message}", e)
                        _mensaje.value = "Error procesando inventario"
                    }
                } else {
                    Log.e(TAG, "Error al obtener inventario: ${invRes.exceptionOrNull()?.message}")
                    _mensaje.value = "No se pudo cargar inventario: ${invRes.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción general cargando datos", e)
                _mensaje.value = "Error cargando datos"
            } finally {
                _isLoading.value = false

            }
        }
    }


    // Refrescar recuento de carrito para cliente
    fun refreshCartCount(idCliente: Long) {
        viewModelScope.launch {
            try {
                // Preferir snapshot local inmediato
                val snap = cartRepository.getCacheSnapshot(idCliente)
                if (snap.isNotEmpty()) {
                    _cartCount.value = snap.size
                }

                // Intentar obtener conteo remoto; solo aplicar si no hay cambios locales recientes/pending
                try {
                    val remoteCount = cartRepository.getCountByCliente(idCliente)
                    val hadRecentLocal = cartRepository.hasRecentLocalUpdate(idCliente, 8000)
                    val hasPending = cartRepository.hasPendingLocalChanges(idCliente)
                    if (!hadRecentLocal && !hasPending) {
                        // Si tenemos un snapshot local no vacío, evitamos bajar el conteo si el remote devuelve 0
                        if (snap.isNotEmpty()) {
                            _cartCount.value = maxOf(snap.size, remoteCount)
                        } else {
                            _cartCount.value = remoteCount
                        }
                    } else {
                         // si hay cambios locales, preferir snapshot
                         Log.w(TAG, "refreshCartCount: skipping remote update due to recent local change or pending (hadRecentLocal=$hadRecentLocal hasPending=$hasPending)")
                     }
                 } catch (e: Exception) {
                     Log.w(TAG, "refreshCartCount: fallo al obtener count remoto: ${e.message}")
                 }

             } catch (_: Exception) {
                 _cartCount.value = 0
             }
         }
     }

    // Agregar al carrito con validaciones
    fun addToCart(idInventario: Long, cantidad: Int, idCliente: Long) {
        viewModelScope.launch {
            _comprando.value = true
            _mensaje.value = null

            try {
                val productoActual = _producto.value
                if (productoActual == null) {
                    _mensaje.value = "Producto no cargado"
                    return@launch
                }

                // 1. Intentar usar el inventario ya cargado en la pantalla (coherente con la UI)
                val invLocal = _inventario.value.find { (it.idRemote ?: 0L) == idInventario }

                if (invLocal == null) {
                    // Si el usuario seleccionó una talla que no existe en el inventario mapeado, rechazamos
                    Log.w(TAG, "addToCart: inventario local no encontrado para idInventario=$idInventario")
                    _mensaje.value = "Talla inválida o no cargada. Intente refrescar la página."
                    return@launch
                }

                // 2. Antes de llamar al remoto, usar el stock que se muestra en la UI como fuente primaria
                val stockUi = invLocal.stock
                if (stockUi <= 0) {
                    _mensaje.value = "Stock agotado"
                    return@launch
                }

                // 3. Comprobar cuántas unidades ya existen en el carrito (remoto)
                // Normalizar talla desde el inventario UI
                val tallaRaw = invLocal.talla
                val tallaNorm = tallaRaw.trim()
                if (tallaNorm.isBlank() || tallaNorm.equals("null", ignoreCase = true)) {
                    Log.w(TAG, "addToCart: talla inválida (blank/null) para idInventario=$idInventario")
                    _mensaje.value = "Seleccione talla antes de agregar al carrito"
                    return@launch
                }

                val itemEnCarrito: CartItemResponse? = try {
                    // Refrescar lista remota para evitar datos stale en inMemoryCache
                    val currentList = cartRepository.getCartForCliente(idCliente).first()
                    currentList.firstOrNull { it.modeloId == idModeloArg && it.talla.trim().equals(tallaNorm, ignoreCase = true) }
                } catch (e: Exception) {
                    Log.w(TAG, "addToCart: fallo al obtener item en carrito (fallback getItem): ${e.message}")
                    try {
                        cartRepository.getItem(idCliente, idModeloArg, tallaNorm)
                    } catch (e2: Exception) {
                        Log.w(TAG, "addToCart: fallback getItem también falló: ${e2.message}")
                        null
                    }
                }

                val cantidadEnCarrito = itemEnCarrito?.cantidad ?: 0
                val cantidadTotal = cantidadEnCarrito + cantidad

                // 4. Autoridad final: preferir stock mostrado en UI (stockUi). Si el remoto está disponible, lo usamos para reconfirmar
                var disponibleAutoritativo = stockUi

                try {
                    val inventarioResult = remoteRepository.getInventarioPorModeloLogged(idModeloArg)
                    if (inventarioResult.isSuccess) {
                        val listaRemota = inventarioResult.getOrNull() ?: emptyList()
                        val remotoMatch = listaRemota.find { (it.id ?: -1L) == idInventario || (it.talla.trim().equals(invLocal.talla.trim(), ignoreCase = true)) }
                        if (remotoMatch != null) {
                            // Use remoto como verificación adicional solo si reporta stock positivo
                            if (remotoMatch.cantidad > 0) {
                                disponibleAutoritativo = remotoMatch.cantidad
                                Log.d(TAG, "addToCart: remote verification found qty=${remotoMatch.cantidad} for idInventario=$idInventario")
                            } else {
                                Log.w(TAG, "addToCart: remote verification returned non-positive qty=${remotoMatch.cantidad} — keeping UI stock=$stockUi")
                            }
                        } else {
                            Log.w(TAG, "addToCart: remote verification no match for idInventario=$idInventario, falling back to UI stock")
                        }
                    } else {
                        Log.w(TAG, "addToCart: fallo al obtener inventario remoto para verificación: ${inventarioResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "addToCart: excepción al verificar inventario remoto: ${e.message}", e)
                }

                // 5. Validaciones finales antes de agregar al carrito
                if (cantidadTotal <= 0) {
                    _mensaje.value = "Cantidad inválida"
                    return@launch
                }

                if (cantidadTotal > disponibleAutoritativo) {
                    _mensaje.value = "Cantidad solicitada excede el stock disponible"
                    return@launch
                }

                // 6. Todo en orden, proceder a agregar al carrito
                val req = CartItemRequest(
                    id = itemEnCarrito?.id,
                    clienteId = idCliente,
                    modeloId = idModeloArg,
                    talla = tallaNorm,
                    cantidad = cantidadTotal,
                    precioUnitario = productoActual.precioUnitario,
                    nombreProducto = productoActual.nombre
                )

                try {
                    if (itemEnCarrito?.id != null && itemEnCarrito.id != 0L) {
                        val idRes = cartRepository.addOrUpdate(req)
                        Log.d(TAG, "addToCart: addOrUpdate returned id=$idRes")
                        // actualizar contador inmediatamente desde snapshot
                        try {
                            val snap = cartRepository.getCacheSnapshot(idCliente)
                            _cartCount.value = snap.size
                        } catch (e: Exception) {
                            Log.w(TAG, "addToCart: failed to update cart count after update: ${e.message}")
                        }
                    } else {
                        // Crear item y obtener carrito consolidado
                        val newList = cartRepository.addAndReturnCart(req, idCliente)
                        Log.d(TAG, "addToCart: addAndReturnCart returned listSize=${newList.size}")
                        // actualizar contador inmediato para que el icono aparezca
                        try {
                            _cartCount.value = newList.size
                        } catch (e: Exception) {
                            Log.w(TAG, "addToCart: failed to set cart count from newList: ${e.message}")
                        }
                    }
                    _mensaje.value = "Producto agregado al carrito"

                    // REFRESH: actualizar contador del carrito para que la UI muestre el icono flotante / badge
                    try {
                        refreshCartCount(idCliente)
                    } catch (e: Exception) {
                        Log.w(TAG, "addToCart: failed to refresh cart count: ${e.message}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "addToCart: error al agregar al carrito: ${e.message}", e)
                    _mensaje.value = "Error al agregar al carrito: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en addToCart: ${e.message}", e)
                _mensaje.value = "Error al agregar al carrito: ${e.message}"
            } finally {
                _comprando.value = false
            }
        }
    }
}
