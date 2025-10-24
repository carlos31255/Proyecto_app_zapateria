package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.utils.ImageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ModeloZapatoEntity>>(emptyList())
    val productos: StateFlow<List<ModeloZapatoEntity>> = _productos

    private val _marcas = MutableStateFlow<List<MarcaEntity>>(emptyList())
    val marcas: StateFlow<List<MarcaEntity>> = _marcas

    init {
        cargarProductos()
        cargarMarcas()
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

    fun actualizarProducto(
        producto: ModeloZapatoEntity,
        nuevoNombre: String,
        nuevoPrecio: Double,
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
            } catch (e: Exception) {
                // Manejar error
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
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al eliminar el producto: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun buscarProductos(query: String) {
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

