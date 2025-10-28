package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.talla.TallaEntity
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ModeloZapatoEntity>>(emptyList())
    val productos: StateFlow<List<ModeloZapatoEntity>> = _productos

    private val _marcas = MutableStateFlow<List<MarcaEntity>>(emptyList())
    val marcas: StateFlow<List<MarcaEntity>> = _marcas

    private val _tallas = MutableStateFlow<List<TallaEntity>>(emptyList())
    val tallas: StateFlow<List<TallaEntity>> = _tallas

    private val _inventarioPorModelo = MutableStateFlow<List<InventarioEntity>>(emptyList())
    val inventarioPorModelo: StateFlow<List<InventarioEntity>> = _inventarioPorModelo

    init {
        cargarProductos()
        cargarMarcas()
        cargarTallas()
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            productoRepository.getAllModelos().collect { lista ->
                _productos.value = lista.sortedBy { it.nombreModelo }
            }
        }
    }

    private fun cargarMarcas() {
        viewModelScope.launch {
            productoRepository.getAllMarcas().collect { lista ->
                _marcas.value = lista
            }
        }
    }

    private fun cargarTallas() {
        viewModelScope.launch {
            productoRepository.getAllTallas().collect { lista ->
                _tallas.value = lista.sortedBy { it.numeroTalla.toDoubleOrNull() ?: 0.0 }
            }
        }
    }

    fun cargarInventarioDeModelo(idModelo: Int) {
        viewModelScope.launch {
            productoRepository.getInventarioByModelo(idModelo).collect { lista ->
                _inventarioPorModelo.value = lista
            }
        }
    }

    fun actualizarProducto(
        producto: ModeloZapatoEntity,
        nuevoNombre: String,
        nuevoPrecio: Int,
        nuevaDescripcion: String?,
        nuevoIdMarca: Int
    ) {
        viewModelScope.launch {
            try {
                // Verificar si el nuevo nombre ya existe en otra marca (si cambió)
                if (nuevoNombre != producto.nombreModelo || nuevoIdMarca != producto.idMarca) {
                    val existe = productoRepository.existeModeloEnMarca(nuevoIdMarca, nuevoNombre)
                    if (existe) {
                        // Podríamos emitir un error, pero por simplicidad lo ignoramos
                        return@launch
                    }
                }

                val productoActualizado = producto.copy(
                    nombreModelo = nuevoNombre,
                    precioUnitario = nuevoPrecio,
                    descripcion = nuevaDescripcion,
                    idMarca = nuevoIdMarca
                )

                productoRepository.updateModelo(productoActualizado)
            } catch (_: Exception) {
                // Manejar error
            }
        }
    }

    fun actualizarInventario(
        idModelo: Int,
        inventarioPorTalla: Map<Int, Int>, // Map de idTalla a stock
        context: Context,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // Para cada talla, actualizar o insertar el inventario
                inventarioPorTalla.forEach { (idTalla, stock) ->
                    val inventarioExistente = productoRepository.getInventarioByModeloYTalla(idModelo, idTalla)

                    if (stock <= 0 && inventarioExistente != null) {
                        // Si el stock es 0 o negativo, eliminar el registro
                        productoRepository.deleteInventario(inventarioExistente)
                    } else if (stock > 0) {
                        if (inventarioExistente != null) {
                            // Actualizar existente
                            productoRepository.updateInventario(
                                inventarioExistente.copy(stockActual = stock)
                            )
                        } else {
                            // Insertar nuevo
                            productoRepository.insertInventario(
                                InventarioEntity(
                                    idModelo = idModelo,
                                    idTalla = idTalla,
                                    stockActual = stock
                                )
                            )
                        }
                    }
                }

                Toast.makeText(
                    context,
                    "Inventario actualizado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al actualizar inventario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun eliminarProducto(context: Context, producto: ModeloZapatoEntity) {
        viewModelScope.launch {
            try {
                // Eliminar la imagen del producto si existe
                if (producto.imagenUrl != null) {
                    ImageHelper.deleteImage(context, producto.imagenUrl)
                }

                // Eliminar el producto de la base de datos
                productoRepository.deleteModelo(producto)

                Toast.makeText(
                    context,
                    "Producto eliminado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "Error al eliminar el producto: ocurrió un error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun buscarProductos(query: String) {
        if (query.isBlank()) {
            cargarProductos()
        } else {
            viewModelScope.launch {
                productoRepository.searchModelosByNombre(query).collect { lista ->
                    _productos.value = lista
                }
            }
        }
    }
}
