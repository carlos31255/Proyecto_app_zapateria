package com.example.proyectoZapateria.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import com.example.proyectoZapateria.data.repository.CartRepository
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.data.repository.TallaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteProductoDetailViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val inventarioRepository: InventarioRepository,
    private val tallaRepository: TallaRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val idModeloArg: Int = checkNotNull(savedStateHandle["idModelo"])

    private val _modelo = MutableStateFlow<ModeloZapatoEntity?>(null)
    val modelo: StateFlow<ModeloZapatoEntity?> = _modelo.asStateFlow()

    private val _inventario = MutableStateFlow<List<InventarioEntity>>(emptyList())
    val inventario: StateFlow<List<InventarioEntity>> = _inventario.asStateFlow()

    private val _comprando = MutableStateFlow(false)
    val comprando: StateFlow<Boolean> = _comprando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Map de idTalla -> numero (por ejemplo "40", "41.5") para mostrar en UI
    private val _tallasMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val tallasMap: StateFlow<Map<Int, String>> = _tallasMap.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount = _cartCount.asStateFlow()

    // Refrescar recuento de carrito para cliente
    fun refreshCartCount(idCliente: Int) {
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
    fun addToCart(idInventario: Int, cantidad: Int, idCliente: Int) {
        viewModelScope.launch {
            try {
                _comprando.value = true

                val invent = inventarioRepository.getById(idInventario)
                if (invent == null) {
                    _mensaje.value = "Inventario no encontrado"
                    return@launch
                }

                val tallaEntity = tallaRepository.getById(invent.idTalla)
                val tallaNumero = tallaEntity?.numeroTalla ?: run {
                    _mensaje.value = "Talla no encontrada"
                    return@launch
                }

                val modeloActual = _modelo.value
                if (modeloActual == null) {
                    _mensaje.value = "Modelo no disponible"
                    return@launch
                }

                val cartItem = CartItemEntity(
                    idCartItem = 0,
                    idCliente = idCliente,
                    idModelo = idModeloArg,
                    talla = tallaNumero,
                    cantidad = cantidad,
                    precioUnitario = modeloActual.precioUnitario
                )

                val res = cartRepository.addValidated(cartItem)
                if (res.isSuccess) {
                    _mensaje.value = "Agregado al carrito"
                    // actualizar contador
                    refreshCartCount(idCliente)
                } else {
                    _mensaje.value = res.exceptionOrNull()?.message ?: "Error al agregar al carrito"
                }
            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Error al agregar al carrito"
            } finally {
                _comprando.value = false
            }
        }
    }

    init {
        cargarModelo()
        cargarInventario()
    }

    private fun cargarModelo() {
        viewModelScope.launch {
            _modelo.value = productoRepository.getModeloById(idModeloArg)
        }
    }

    private fun cargarInventario() {
        viewModelScope.launch {
            inventarioRepository.getByModelo(idModeloArg).collect { lista ->
                _inventario.value = lista
                // Cargar tallas asociadas
                cargarTallasParaInventario(lista)
            }
        }
    }

    private suspend fun cargarTallasParaInventario(lista: List<InventarioEntity>) {
        val map = mutableMapOf<Int, String>()
        lista.map { it.idTalla }.distinct().forEach { idTalla ->
            val talla = tallaRepository.getById(idTalla)
            if (talla != null) map[idTalla] = talla.numeroTalla
        }
        _tallasMap.value = map
    }
}
