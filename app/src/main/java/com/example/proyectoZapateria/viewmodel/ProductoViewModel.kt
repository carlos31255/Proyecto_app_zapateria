package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.repository.ProductoRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProductoFormState(
    val nombreModelo: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stockInicial: String = "",
    val idMarcaSeleccionada: Int? = null,
    val imagenFile: File? = null,
    val imagenUri: Uri? = null,

    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val marcaError: String? = null,
    val imagenError: String? = null,

    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ProductoFormState())
    val formState: StateFlow<ProductoFormState> = _formState

    private val _marcas = MutableStateFlow<List<MarcaEntity>>(emptyList())
    val marcas: StateFlow<List<MarcaEntity>> = _marcas

    private val _productos = MutableStateFlow<List<ModeloZapatoEntity>>(emptyList())
    val productos: StateFlow<List<ModeloZapatoEntity>> = _productos

    init {
        cargarMarcas()
        cargarProductos()
    }

    private fun cargarMarcas() {
        viewModelScope.launch {
            productoRepository.getMarcasActivas().collect { lista ->
                _marcas.value = lista
            }
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            productoRepository.getModelosActivos().collect { lista ->
                _productos.value = lista
            }
        }
    }

    // ========== HANDLERS DEL FORMULARIO ==========

    fun onNombreChange(value: String) {
        _formState.update {
            it.copy(
                nombreModelo = value,
                nombreError = if (value.isBlank()) "El nombre es requerido" else null
            )
        }
        recomputeCanSubmit()
    }

    fun onDescripcionChange(value: String) {
        _formState.update { it.copy(descripcion = value) }
    }

    fun onPrecioChange(value: String) {
        // Validar que solo sean números y punto decimal
        val filtrado = value.filter { it.isDigit() || it == '.' }
        val error = when {
            filtrado.isBlank() -> "El precio es requerido"
            filtrado.toDoubleOrNull() == null -> "Precio inválido"
            filtrado.toDouble() <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        }

        _formState.update {
            it.copy(
                precio = filtrado,
                precioError = error
            )
        }
        recomputeCanSubmit()
    }

    fun onStockChange(value: String) {
        // Validar que solo sean números enteros
        val filtrado = value.filter { it.isDigit() }
        val error = when {
            filtrado.isBlank() -> "El stock es requerido"
            filtrado.toIntOrNull() == null -> "Stock inválido"
            filtrado.toInt() < 0 -> "El stock no puede ser negativo"
            else -> null
        }

        _formState.update {
            it.copy(
                stockInicial = filtrado,
                stockError = error
            )
        }
        recomputeCanSubmit()
    }

    fun onMarcaSelected(idMarca: Int?) {
        _formState.update {
            it.copy(
                idMarcaSeleccionada = idMarca,
                marcaError = if (idMarca == null) "Seleccione una marca" else null
            )
        }
        recomputeCanSubmit()
    }

    fun onImagenCapturada(context: Context, file: File) {
        val uri = ImageHelper.getUriForFile(context, file)
        _formState.update {
            it.copy(
                imagenFile = file,
                imagenUri = uri,
                imagenError = null
            )
        }
        recomputeCanSubmit()
    }

    fun onRemoveImagen() {
        _formState.update {
            it.copy(
                imagenFile = null,
                imagenUri = null,
                imagenError = "La imagen es requerida"
            )
        }
        recomputeCanSubmit()
    }

    private fun recomputeCanSubmit() {
        val s = _formState.value
        val can = s.nombreModelo.isNotBlank() &&
                s.nombreError == null &&
                s.precio.isNotBlank() &&
                s.precioError == null &&
                s.stockInicial.isNotBlank() &&
                s.stockError == null &&
                s.idMarcaSeleccionada != null &&
                s.marcaError == null &&
                s.imagenFile != null &&
                !s.isLoading

        _formState.update { it.copy(canSubmit = can) }
    }

    // ========== GUARDAR PRODUCTO ==========

    fun guardarProducto(context: Context) {
        val s = _formState.value
        if (!s.canSubmit || s.isLoading) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMsg = null, success = false) }

            try {
                // Verificar que no exista un producto con el mismo nombre en la marca
                val existe = productoRepository.existeModeloEnMarca(
                    s.idMarcaSeleccionada!!,
                    s.nombreModelo
                )

                if (existe) {
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Ya existe un modelo con ese nombre en la marca seleccionada"
                        )
                    }
                    return@launch
                }

                // Obtener la ruta relativa de la imagen
                val imagenPath = ImageHelper.getRelativePath(s.imagenFile!!, context)

                // Crear el modelo
                val nuevoModelo = ModeloZapatoEntity(
                    idMarca = s.idMarcaSeleccionada,
                    nombreModelo = s.nombreModelo.trim(),
                    descripcion = s.descripcion.trim().ifBlank { null },
                    precioUnitario = s.precio.toDouble(),
                    imagenUrl = imagenPath,
                    estado = "activo"
                )

                // Guardar en la base de datos
                val result = productoRepository.insertModelo(nuevoModelo)

                if (result.isSuccess) {
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            success = true,
                            errorMsg = null
                        )
                    }
                    limpiarFormulario()
                } else {
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            success = false,
                            errorMsg = "Error al guardar el producto: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMsg = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun limpiarFormulario() {
        _formState.value = ProductoFormState()
    }

    fun clearSuccess() {
        _formState.update { it.copy(success = false) }
    }

    // ========== GESTIÓN DE MARCAS ==========

    suspend fun agregarMarca(nombre: String, descripcion: String?): Result<Long> {
        return try {
            val existe = productoRepository.existeMarcaConNombre(nombre)
            if (existe) {
                return Result.failure(Exception("Ya existe una marca con ese nombre"))
            }

            val nuevaMarca = MarcaEntity(
                nombreMarca = nombre.trim(),
                descripcion = descripcion?.trim(),
                estado = "activa"
            )

            productoRepository.insertMarca(nuevaMarca)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

