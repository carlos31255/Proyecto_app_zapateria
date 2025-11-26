package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    val errorMsg: String? = null,
    val imagenUploaded: Boolean = false // indicar que la imagen se subió correctamente
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

    private val _productos = MutableStateFlow<List<ProductoDTO>>(emptyList())
    val productos: StateFlow<List<ProductoDTO>> = _productos

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // 1. Cargar Marcas
            inventarioRemoteRepository.getMarcas().onSuccess { dtos ->
                _marcas.value = dtos
            }
            // 2. Cargar Tallas
            inventarioRemoteRepository.getTallas().onSuccess { dtos ->
                _tallas.value = dtos
            }
            // 3. Cargar Productos
            cargarProductos()
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            inventarioRemoteRepository.getModelos().onSuccess { dtos ->
                _productos.value = dtos
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
            _formState.update { it.copy(isLoading = true, errorMsg = null, success = false, imagenUploaded = false) }

            try {
                // 1. Crear Modelo en API
                val nuevoProducto = ProductoDTO(
                    id = 0L,
                    nombre = s.nombreModelo.trim(),
                    marcaId = s.idMarcaSeleccionada!!,
                    descripcion = s.descripcion.trim().ifBlank { null },
                    precioUnitario = s.precio.toIntOrNull() ?: 0,
                    imagenUrl = null
                )

                val resultModelo = if (s.imagenFile != null) {
                    // preparar multipart: producto JSON + file
                    val gson = com.google.gson.Gson()
                    val json = gson.toJson(nuevoProducto)
                    val reqBody = json.toRequestBody("application/json".toMediaType())

                    // crear MultipartBody.Part desde el File
                    val bytes = s.imagenFile!!.readBytes()
                    val reqFile = bytes.toRequestBody("image/jpeg".toMediaType())
                    val part = MultipartBody.Part.createFormData("imagen", s.imagenFile!!.name, reqFile)

                    inventarioRemoteRepository.crearModeloConImagen(reqBody, part)
                } else {
                    inventarioRemoteRepository.crearModelo(nuevoProducto)
                }

                if (resultModelo.isSuccess) {
                    val modeloCreado = resultModelo.getOrNull()!! // Este objeto viene del Backend con el ID generado

                    // Asegurarse de que el backend retornó un id válido
                    val modeloId = modeloCreado.id ?: run {
                        _formState.update { it.copy(isLoading = false, errorMsg = "ID del modelo no fue retornado por el servidor") }
                        limpiarFormulario()
                        return@launch
                    }

                    // Si hubo imagen, marcar que la imagen fue subida correctamente (evento)
                    s.imagenFile?.let { file ->
                        _formState.update { it.copy(imagenUploaded = true) }
                    }

                    // 2. Crear Inventario (API) para cada talla
                    s.tallasSeleccionadas.forEach { (idTalla, tallaConStock) ->
                        val tallaObj = _tallas.value.find { it.id == idTalla }
                        val tallaString = tallaObj?.valor ?: ""

                        val inventarioDto = InventarioDTO(
                            id = null,
                            productoId = modeloId,
                            nombre = modeloCreado.nombre,
                            tallaId = idTalla,
                            talla = tallaString,
                            cantidad = tallaConStock.stock.toIntOrNull() ?: 0,
                            stockMinimo = 5
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
    // Limpiar indicador de imagen subida
    fun clearImagenUploaded() { _formState.update { it.copy(imagenUploaded = false) } }

    fun eliminarProducto(context: Context, producto: ProductoDTO) {
        viewModelScope.launch {
            producto.id?.let { id ->
                inventarioRemoteRepository.eliminarModelo(id).onSuccess { cargarProductos() }
            } ?: run {
                // id no presente — nada que eliminar en remoto
            }
        }
    }
}
