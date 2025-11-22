package com.example.proyectoZapateria.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.ui.model.InventarioUi
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteProductoDetailViewModel @Inject constructor(
    private val remoteRepository: InventarioRemoteRepository,
    private val cartRepository: CartRepository, // Carrito local
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val idModeloArg: Long = checkNotNull(savedStateHandle["idModelo"]) as Long

    private val _modelo = MutableStateFlow<ModeloZapatoEntity?>(null)
    val modelo: StateFlow<ModeloZapatoEntity?> = _modelo.asStateFlow()

    private val _inventario = MutableStateFlow<List<InventarioUi>>(emptyList())
    val inventario: StateFlow<List<InventarioUi>> = _inventario.asStateFlow()

    private val _comprando = MutableStateFlow(false)
    val comprando: StateFlow<Boolean> = _comprando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

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
            try {
                // 1. Cargar Detalle del Modelo (Producto)
                remoteRepository.getModeloById(idModeloArg).onSuccess { dto ->
                    _modelo.value = ModeloZapatoEntity(
                        idModelo = dto.id ?: 0L,
                        nombreModelo = dto.nombre,
                        idMarca = dto.marcaId ?: 0L,
                        descripcion = dto.descripcion,
                        precioUnitario = 0, // Pendiente en backend
                        imagenUrl = null,   // Pendiente en backend
                        estado = "activo"
                    )
                }

                // 2. Cargar Tallas (para poder mapear el inventario correctamente)
                val tallasResult = remoteRepository.getTallas()
                val listaTallas = if (tallasResult.isSuccess) tallasResult.getOrNull() ?: emptyList() else emptyList()

                // Actualizar mapa de tallas para la UI
                _tallasMap.value = listaTallas.associate { it.id to it.valor }

                // 3. Cargar Inventario del Modelo
                remoteRepository.getInventarioPorModelo(idModeloArg).onSuccess { dtos ->
                    val listaLocal = dtos.mapNotNull { dto ->
                        // Buscamos el ID de la talla correspondiente al string (ej: "40")
                        val tallaId = listaTallas.find { it.valor == dto.talla }?.id

                        if (tallaId != null) {
                            InventarioUi(
                                idRemote = dto.id ?: 0L,
                                idModelo = idModeloArg,
                                talla = dto.talla,
                                tallaIdLocal = tallaId,
                                stock = dto.cantidad
                            )
                        } else null
                    }
                    _inventario.value = listaLocal
                }
            } catch (e: Exception) {
                _mensaje.value = "Error cargando datos: ${e.message}"
            }
        }
    }


    // Refrescar recuento de carrito para cliente
    fun refreshCartCount(idCliente: Long) {
        viewModelScope.launch {
            try {
                val count = cartRepository.getCountByCliente(idCliente)
                _cartCount.value = count
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
                val modeloActual = _modelo.value
                if (modeloActual == null) {
                    _mensaje.value = "Modelo no cargado"
                    return@launch
                }

                // 1. Validar Stock Remoto en Tiempo Real
                val inventarioResult = remoteRepository.getInventarioPorModelo(idModeloArg)

                if (inventarioResult.isFailure) {
                    _mensaje.value = "No se pudo verificar el stock. Revise su conexión."
                    return@launch
                }

                // Buscamos el item específico en la respuesta remota usando el ID
                // (idInventario que viene de la UI corresponde al ID remoto mapeado en cargarDatos)
                val stockRemoto = inventarioResult.getOrNull()?.find { it.id == idInventario }

                if (stockRemoto == null) {
                    _mensaje.value = "Producto no disponible (Stock agotado o no existe)"
                    return@launch
                }

                // 2. Verificar cuánto ya tengo en el carrito (para no exceder el total)
                val itemEnCarrito = cartRepository.getItem(idCliente, idModeloArg, stockRemoto.talla)
                val cantidadEnCarrito = itemEnCarrito?.cantidad ?: 0
                val cantidadTotal = cantidadEnCarrito + cantidad

                if (stockRemoto.cantidad < cantidadTotal) {
                    val disponibleParaAgregar = maxOf(0, stockRemoto.cantidad - cantidadEnCarrito)
                    _mensaje.value = "Stock insuficiente. Ya tienes $cantidadEnCarrito. Solo puedes agregar $disponibleParaAgregar más."
                    return@launch
                }

                // 3. Agregar al carrito local
                val cartItem = CartItemEntity(
                    idCartItem = itemEnCarrito?.idCartItem ?: 0, // Si existe, actualizamos ese ID
                    idCliente = idCliente,
                    idModelo = idModeloArg,
                    talla = stockRemoto.talla,
                    cantidad = cantidadTotal, // Guardamos el nuevo total acumulado
                    precioUnitario = modeloActual.precioUnitario
                )

                cartRepository.addOrUpdate(cartItem)
                _mensaje.value = "Agregado al carrito"
                refreshCartCount(idCliente)

            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            } finally {
                _comprando.value = false
            }
        }
    }

}
