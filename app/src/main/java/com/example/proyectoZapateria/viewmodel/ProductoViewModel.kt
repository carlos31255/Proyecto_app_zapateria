package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class TallaConStock(
    val talla: TallaDTO,
    val stock: String = "",
    val stockError: String? = null
)

data class ProductoFormState(
    val nombreModelo: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val idMarcaSeleccionada: Long? = null,
    val imagenFile: File? = null,
    val imagenUri: Uri? = null,
    val tallasSeleccionadas: Map<Long, TallaConStock> = emptyMap(),
    val nombreError: String? = null,
    val precioError: String? = null,
    val marcaError: String? = null,
    val imagenError: String? = null,
    val tallasError: String? = null,
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ProductoFormState())
    val formState: StateFlow<ProductoFormState> = _formState

    private val _marcas = MutableStateFlow<List<MarcaDTO>>(emptyList())
    val marcas: StateFlow<List<MarcaDTO>> = _marcas

    private val _tallas = MutableStateFlow<List<TallaDTO>>(emptyList())
    val tallas: StateFlow<List<TallaDTO>> = _tallas

    private val _productos = MutableStateFlow<List<ModeloZapatoDTO>>(emptyList())
    val productos: StateFlow<List<ModeloZapatoDTO>> = _productos

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // 1. Cargar Marcas
            inventarioRemoteRepository.getMarcas().onSuccess { dtos ->
                _marcas.value = dtos ?: emptyList()
            }
            // 2. Cargar Tallas
            inventarioRemoteRepository.getTallas().onSuccess { dtos ->
                _tallas.value = dtos ?: emptyList()
            }
            // 3. Cargar Productos
            cargarProductos()
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            inventarioRemoteRepository.getModelos().onSuccess { dtos ->
                _productos.value = dtos ?: emptyList()
            }
        }
    }

    // Handlers de formulario
    fun onNombreChange(value: String) {
        _formState.update { it.copy(nombreModelo = value, nombreError = if (value.isBlank()) "Requerido" else null) }
        recomputeCanSubmit()
    }
    fun onDescripcionChange(value: String) { _formState.update { it.copy(descripcion = value) } }
    fun onPrecioChange(value: String) {
        val error = if (value.isBlank()) "Requerido" else null
        _formState.update { it.copy(precio = value, precioError = error) }
        recomputeCanSubmit()
    }
    fun onMarcaSelected(id: Long?) {
        _formState.update { it.copy(idMarcaSeleccionada = id, marcaError = if (id == null) "Requerido" else null) }
        recomputeCanSubmit()
    }
    fun onTallaToggle(talla: TallaDTO) {
        _formState.update { s ->
            val map = s.tallasSeleccionadas.toMutableMap()
            if (map.containsKey(talla.id)) map.remove(talla.id)
            else map[talla.id] = TallaConStock(talla)
            s.copy(tallasSeleccionadas = map)
        }
        recomputeCanSubmit()
    }
    fun onStockTallaChange(idTalla: Long, stock: String) {
        _formState.update { s ->
            val map = s.tallasSeleccionadas.toMutableMap()
            val item = map[idTalla] ?: return@update s
            map[idTalla] = item.copy(stock = stock)
            s.copy(tallasSeleccionadas = map)
        }
        recomputeCanSubmit()
    }
    fun onImagenCapturada(ctx: Context, file: File) {
        val uri = ImageHelper.getUriForFile(ctx, file)
        _formState.update { it.copy(imagenFile = file, imagenUri = uri, imagenError = null) }
        recomputeCanSubmit()
    }
    fun onRemoveImagen() {
        _formState.update { it.copy(imagenFile = null, imagenUri = null) }
    }
    private fun recomputeCanSubmit() {
        val s = _formState.value
        val can = s.nombreModelo.isNotBlank() && s.idMarcaSeleccionada != null && s.tallasSeleccionadas.isNotEmpty()
        _formState.update { it.copy(canSubmit = can) }
    }

    // ========== GUARDAR PRODUCTO  ==========

    fun guardarProducto(context: Context) {
        val s = _formState.value
        if (!s.canSubmit || s.isLoading) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMsg = null, success = false) }

            try {
                // 1. Crear Modelo en API
                val nuevoModelo = ModeloZapatoDTO(
                    id = 0,
                    nombre = s.nombreModelo.trim(),
                    marcaId = s.idMarcaSeleccionada!!,
                    descripcion = s.descripcion.trim().ifBlank { null },
                    precioUnitario = s.precio.toIntOrNull() ?: 0,
                    imagenUrl = null
                )

                val resultModelo = inventarioRemoteRepository.crearModelo(nuevoModelo)

                if (resultModelo.isSuccess) {
                    val modeloCreado = resultModelo.getOrNull()!! // Este objeto viene del Backend con el ID generado

                    // 2. Crear Inventario (API) para cada talla
                    s.tallasSeleccionadas.forEach { (idTalla, tallaConStock) ->
                        val tallaObj = _tallas.value.find { it.id == idTalla }
                        val tallaString = tallaObj?.valor ?: ""

                        val inventarioDto = InventarioDTO(
                            id = null,
                            productoId = modeloCreado.id,
                            nombre = modeloCreado.nombre,
                            talla = tallaString,
                            cantidad = tallaConStock.stock.toIntOrNull() ?: 0,
                            stockMinimo = 5,
                            modeloId = modeloCreado.id,
                            tallaId = idTalla
                        )
                        inventarioRemoteRepository.crearInventario(inventarioDto)
                    }

                    _formState.update { it.copy(isLoading = false, success = true) }
                    limpiarFormulario()
                } else {
                    val msg = resultModelo.exceptionOrNull()?.message ?: "Error al crear modelo"
                    _formState.update { it.copy(isLoading = false, errorMsg = msg) }
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
        }
    }

    fun limpiarFormulario() { _formState.value = ProductoFormState() }
    fun clearSuccess() { _formState.update { it.copy(success = false) } }

    fun eliminarProducto(context: Context, producto: ModeloZapatoDTO) {
        viewModelScope.launch {
            inventarioRemoteRepository.eliminarModelo(producto.id).onSuccess { cargarProductos() }
        }
    }
}
