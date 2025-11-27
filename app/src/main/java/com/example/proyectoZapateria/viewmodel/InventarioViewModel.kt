package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.ui.model.InventarioUi
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoDTO>>(emptyList())
    val productos: StateFlow<List<ProductoDTO>> = _productos

    private val _marcas = MutableStateFlow<List<MarcaDTO>>(emptyList())
    val marcas: StateFlow<List<MarcaDTO>> = _marcas

    private val _tallas = MutableStateFlow<List<TallaDTO>>(emptyList())
    val tallas: StateFlow<List<TallaDTO>> = _tallas

    private val _inventarioPorModelo = MutableStateFlow<List<InventarioUi>>(emptyList())
    val inventarioPorModelo: StateFlow<List<InventarioUi>> = _inventarioPorModelo

    private val _imagenes = MutableStateFlow<Map<Long, ByteArray?>>(emptyMap())
    val imagenes: StateFlow<Map<Long, ByteArray?>> = _imagenes

    // Indica si la carga inicial de productos está en progreso
    private val _isLoadingProductos = MutableStateFlow(true)
    val isLoadingProductos: StateFlow<Boolean> = _isLoadingProductos

    // Indica qué modelo está cargando su inventario (idModelo) o null si no hay carga
    private val _isLoadingInventario = MutableStateFlow<Long?>(null)
    val isLoadingInventario: StateFlow<Long?> = _isLoadingInventario

    // cache local de productos remotos (InventarioDTO)
    private var cacheInventarioRemoto: List<InventarioDTO> = emptyList()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // 1. Cargar Marcas desde API
            inventarioRemoteRepository.getMarcas().onSuccess { dtos ->
                _marcas.value = dtos
            }

            // 2. Cargar Tallas desde API
            inventarioRemoteRepository.getTallas().onSuccess { dtos ->
                _tallas.value = dtos.sortedBy { it.valor.toDoubleOrNull() ?: 0.0 }
            }

            // 3. Cargar Productos desde API
            cargarProductos()
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            // marcar inicio de carga
            _isLoadingProductos.value = true

            inventarioRemoteRepository.getModelos().onSuccess { dtos ->
                _productos.value = dtos.sortedBy { it.nombre }
                _isLoadingProductos.value = false
            }.onFailure {
                // en fallo, dejar lista vacía y desactivar loader
                _productos.value = emptyList()
                _isLoadingProductos.value = false
            }
        }
    }

    // Carga el stock de un producto específico desde la NUBE
    fun cargarInventarioDeModelo(idModelo: Long) {
        viewModelScope.launch {
            // marcar que estamos cargando este modelo
            _isLoadingInventario.value = idModelo
             inventarioRemoteRepository.getInventarioPorModeloLogged(idModelo).onSuccess { dtos ->
                 cacheInventarioRemoto = dtos
                 val listaUi = dtos.map { dto ->
                     val tallaLocal = _tallas.value.find { it.valor == dto.talla }
                     InventarioUi(
                         idRemote = dto.id ?: 0L,
                         idModelo = idModelo,
                         talla = dto.talla,
                         tallaIdLocal = tallaLocal?.id,
                         stock = dto.cantidad
                     )
                 }
                 _inventarioPorModelo.value = listaUi
                // desactivar flag de carga
                _isLoadingInventario.value = null
             }.onFailure {
                 _inventarioPorModelo.value = emptyList()
                _isLoadingInventario.value = null
             }
         }
     }

    // Actualizar Producto en la API
    fun actualizarProducto(
        producto: ProductoDTO,
        nuevoNombre: String,
        nuevoPrecio: Int,
        nuevaDescripcion: String?,
        nuevoIdMarca: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val dto = ProductoDTO(
                id = producto.id,
                nombre = nuevoNombre.trim(),
                marcaId = nuevoIdMarca,
                descripcion = nuevaDescripcion?.trim()?.ifBlank { null },
                precioUnitario = nuevoPrecio,
                imagenUrl = producto.imagenUrl
            )

            val modeloIdNullable = producto.id
            if (modeloIdNullable == null) {
                inventarioRemoteRepository.crearModelo(dto)
                    .onSuccess { _ ->
                        cargarProductos()
                        onSuccess()
                    }
                    .onFailure { _ ->
                    }
            } else {
                inventarioRemoteRepository.actualizarModelo(modeloIdNullable, dto)
                    .onSuccess { _ ->
                        cargarProductos()
                        onSuccess()
                    }
                    .onFailure { _ ->
                    }
            }
        }
    }

    // Actualizar Inventario en la API (Sincronización inteligente)
    fun actualizarInventario(
        idModelo: Long,
        inventarioPorTalla: Map<Long, Int>, // Map<idTallaRemoto, Cantidad>
        context: Context,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("InventarioVM", "=== INICIO actualizarInventario para producto $idModelo ===")
                android.util.Log.d("InventarioVM", "Tallas a procesar: ${inventarioPorTalla.size}")

                val modelo = _productos.value.find { it.id == idModelo }
                val nombreModelo = modelo?.nombre ?: "Producto"
                android.util.Log.d("InventarioVM", "Nombre del producto: $nombreModelo")

                // Recargar cache antes de procesar para tener datos actualizados
                android.util.Log.d("InventarioVM", "Recargando cache de inventario desde servidor...")
                val resultInventario = inventarioRemoteRepository.getInventarioPorModeloLogged(idModelo)
                if (resultInventario.isSuccess) {
                    cacheInventarioRemoto = resultInventario.getOrNull() ?: emptyList()
                    android.util.Log.d("InventarioVM", "Cache cargado: ${cacheInventarioRemoto.size} registros")
                    cacheInventarioRemoto.forEach { inv ->
                        android.util.Log.d("InventarioVM", "  - Cache: id=${inv.id}, talla=${inv.talla}, cantidad=${inv.cantidad}, productoId=${inv.productoId}")
                    }
                } else {
                    android.util.Log.e("InventarioVM", "Error al cargar cache: ${resultInventario.exceptionOrNull()?.message}")
                }

                // Recorremos el mapa que viene de la UI
                inventarioPorTalla.forEach { (idTallaLocal, nuevoStock) ->
                    android.util.Log.d("InventarioVM", "--- Procesando talla: idTallaLocal=$idTallaLocal, nuevoStock=$nuevoStock ---")

                    // Buscamos el string de la talla (ej: "40")
                    val tallaObj = _tallas.value.find { it.id == idTallaLocal }
                    if (tallaObj == null) {
                        android.util.Log.e("InventarioVM", "ERROR: No se encontró talla con id=$idTallaLocal")
                        return@forEach
                    }
                    val tallaString = tallaObj.valor
                    android.util.Log.d("InventarioVM", "Talla encontrada: $tallaString (id=$idTallaLocal)")

                    // Verificamos si ya existe en el backend (por talla string y productoId)
                    val remoto = cacheInventarioRemoto.find {
                        it.talla == tallaString && it.productoId == idModelo
                    }
                    android.util.Log.d("InventarioVM", "Inventario remoto encontrado: ${remoto?.let { "id=${it.id}, cantidad=${it.cantidad}" } ?: "NO EXISTE"}")

                    if (nuevoStock <= 0) {
                        android.util.Log.d("InventarioVM", "Stock <= 0, intentando ELIMINAR...")
                        // Si stock es 0 y existe remoto, ELIMINAR
                        if (remoto != null && remoto.id != null) {
                            android.util.Log.d("InventarioVM", "Eliminando inventario id=${remoto.id}")
                            val deleteResult = inventarioRemoteRepository.eliminarStock(remoto.id)
                            if (deleteResult.isSuccess) {
                                android.util.Log.d("InventarioVM", "✅ Inventario eliminado correctamente")
                            } else {
                                android.util.Log.e("InventarioVM", "❌ Error al eliminar: ${deleteResult.exceptionOrNull()?.message}")
                            }
                        } else {
                            android.util.Log.d("InventarioVM", "No hay inventario remoto para eliminar")
                        }
                    } else {
                        val dto = InventarioDTO(
                            id = remoto?.id,
                            productoId = idModelo,
                            nombre = nombreModelo,
                            tallaId = idTallaLocal,
                            talla = tallaString,
                            cantidad = nuevoStock,
                            stockMinimo = 5
                        )
                        android.util.Log.d("InventarioVM", "DTO creado: id=${dto.id}, productoId=${dto.productoId}, tallaId=${dto.tallaId}, talla=${dto.talla}, cantidad=${dto.cantidad}")

                        if (remoto != null && remoto.id != null) {
                            // Si existe, siempre ACTUALIZAR (incluso si la cantidad es igual, por si otros campos cambiaron)
                            android.util.Log.d("InventarioVM", "Inventario existe, ACTUALIZANDO id=${remoto.id}...")
                            val updateResult = inventarioRemoteRepository.actualizarInventario(remoto.id, dto)
                            if (updateResult.isSuccess) {
                                android.util.Log.d("InventarioVM", "✅ Inventario actualizado correctamente")
                            } else {
                                android.util.Log.e("InventarioVM", "❌ Error al actualizar: ${updateResult.exceptionOrNull()?.message}")
                            }
                        } else {
                            // Si no existe, CREAR
                            android.util.Log.d("InventarioVM", "Inventario NO existe, CREANDO nuevo...")
                            val createResult = inventarioRemoteRepository.crearInventario(dto)
                            if (createResult.isSuccess) {
                                android.util.Log.d("InventarioVM", "✅ Inventario creado correctamente")
                            } else {
                                android.util.Log.e("InventarioVM", "❌ Error al crear: ${createResult.exceptionOrNull()?.message}")
                            }
                        }
                    }
                }

                android.util.Log.d("InventarioVM", "=== FIN actualizarInventario ===")
                Toast.makeText(context, "Inventario sincronizado", Toast.LENGTH_SHORT).show()
                cargarInventarioDeModelo(idModelo) // Recargar para ver cambios
                onSuccess()

            } catch (e: Exception) {
                android.util.Log.e("InventarioVM", "❌❌❌ EXCEPCIÓN en actualizarInventario: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun eliminarProducto(context: Context, producto: ProductoDTO) {
        viewModelScope.launch {
            // Eliminar imagen localmente si existe (esto se mantiene local)
            if (producto.imagenUrl != null) {
                ImageHelper.deleteImage(context, producto.imagenUrl)
            }

            // Eliminar producto en la API solo si tiene id
            val idToDelete = producto.id
            if (idToDelete != null) {
                inventarioRemoteRepository.eliminarModelo(idToDelete).onSuccess {
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
            } else {
                // Si no tiene id, solo refrescar UI
                Toast.makeText(context, "Producto no existe en remoto", Toast.LENGTH_SHORT).show()
                cargarProductos()
            }
        }
    }

    // Buscar productos
    @Suppress("unused")
    fun buscarProductos(query: String) {
        if (query.isBlank()) {
            cargarProductos()
        } else {
            viewModelScope.launch {
                inventarioRemoteRepository.searchModelos(query).onSuccess { dtos ->
                    _productos.value = dtos.sortedBy { it.nombre }
                }
            }
        }
    }

    fun loadImagenProducto(idProducto: Long) {
        // No volver a pedir si ya existe (aunque sea null para indicar no hay bytes)
        if (_imagenes.value.containsKey(idProducto)) return

        viewModelScope.launch {
            try {
                val res = inventarioRemoteRepository.obtenerImagenProducto(idProducto)
                if (res.isSuccess) {
                    val bytes = res.getOrNull()
                    _imagenes.update { map -> map + (idProducto to bytes) }
                } else {
                    // Indicar explícitamente que no hay imagen bytes (null se usa pero guardamos clave)
                    _imagenes.update { map -> map + (idProducto to null) }
                }
            } catch (e: Exception) {
                // loadImagenProducto error: ${e.message}
                // Guardar clave con null para evitar reintentos continuos
                _imagenes.update { map -> map + (idProducto to null) }
            }
        }
    }
}
