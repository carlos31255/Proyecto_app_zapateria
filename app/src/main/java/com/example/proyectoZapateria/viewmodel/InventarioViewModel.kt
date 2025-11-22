package com.example.proyectoZapateria.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.ui.model.InventarioUi
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import com.example.proyectoZapateria.utils.ImageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ModeloZapatoDTO>>(emptyList())
    val productos: StateFlow<List<ModeloZapatoDTO>> = _productos

    private val _marcas = MutableStateFlow<List<MarcaDTO>>(emptyList())
    val marcas: StateFlow<List<MarcaDTO>> = _marcas

    private val _tallas = MutableStateFlow<List<TallaDTO>>(emptyList())
    val tallas: StateFlow<List<TallaDTO>> = _tallas

    private val _inventarioPorModelo = MutableStateFlow<List<InventarioUi>>(emptyList())
    val inventarioPorModelo: StateFlow<List<InventarioUi>> = _inventarioPorModelo

    // cache local de productos remotos (InventarioDTO)
    private var cacheInventarioRemoto: List<InventarioDTO> = emptyList()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // 1. Cargar Marcas desde API
            inventarioRemoteRepository.getMarcas().onSuccess { dtos ->
                _marcas.value = dtos ?: emptyList()
            }

            // 2. Cargar Tallas desde API
            inventarioRemoteRepository.getTallas().onSuccess { dtos ->
                _tallas.value = dtos?.sortedBy { it.valor.toDoubleOrNull() ?: 0.0 } ?: emptyList()
            }

            // 3. Cargar Productos desde API
            cargarProductos()
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            inventarioRemoteRepository.getModelos().onSuccess { dtos ->
                _productos.value = dtos?.sortedBy { it.nombre } ?: emptyList()
            }
        }
    }

    // Carga el stock de un producto específico desde la NUBE
    fun cargarInventarioDeModelo(idModelo: Long) {
        viewModelScope.launch {
            inventarioRemoteRepository.getInventarioPorModelo(idModelo).onSuccess { dtos ->
                cacheInventarioRemoto = dtos ?: emptyList()

                // Mapear DTO remoto a modelo UI (InventarioUi)
                val listaUi = (dtos ?: emptyList()).mapNotNull { dto ->
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
            }.onFailure {
                _inventarioPorModelo.value = emptyList()
            }
        }
    }

    // Actualizar Producto en la API
    fun actualizarProducto(
        producto: ModeloZapatoDTO,
        nuevoNombre: String,
        nuevoPrecio: Int,
        nuevaDescripcion: String?,
        nuevoIdMarca: Long
    ) {
        viewModelScope.launch {
            val dto = ModeloZapatoDTO(
                id = producto.id,
                nombre = nuevoNombre,
                marcaId = nuevoIdMarca,
                descripcion = nuevaDescripcion,
                precioUnitario = nuevoPrecio,
                imagenUrl = producto.imagenUrl
            )

            inventarioRemoteRepository.actualizarModelo(producto.id, dto)
                .onSuccess { cargarProductos() }
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
                val modelo = _productos.value.find { it.id == idModelo }
                val nombreModelo = modelo?.nombre ?: "Producto"

                // Recorremos el mapa que viene de la UI
                inventarioPorTalla.forEach { (idTallaLocal, nuevoStock) ->
                    // Buscamos el string de la talla (ej: "40")
                    val tallaObj = _tallas.value.find { it.id == idTallaLocal } ?: return@forEach
                    val tallaString = tallaObj.valor

                    // Verificamos si ya existe en el backend (por talla)
                    val remoto = cacheInventarioRemoto.find { it.talla == tallaString }

                    if (nuevoStock <= 0) {
                        // Si stock es 0 y existe remoto, ELIMINAR
                        if (remoto != null && remoto.id != null) {
                            inventarioRemoteRepository.eliminarStock(remoto.id)
                        }
                    } else {
                        val dto = InventarioDTO(
                            id = remoto?.id,
                            productoId = idModelo,
                            nombre = nombreModelo,
                            talla = tallaString,
                            cantidad = nuevoStock,
                            stockMinimo = 5,
                            modeloId = idModelo,
                            tallaId = idTallaLocal
                        )

                        if (remoto != null && remoto.id != null) {
                            // Si existe y cambió cantidad, ACTUALIZAR
                            if (remoto.cantidad != nuevoStock) {
                                inventarioRemoteRepository.actualizarInventario(remoto.id, dto)
                            }
                        } else {
                            // Si no existe, CREAR
                            inventarioRemoteRepository.crearInventario(dto)
                        }
                    }
                }

                Toast.makeText(context, "Inventario sincronizado", Toast.LENGTH_SHORT).show()
                cargarInventarioDeModelo(idModelo) // Recargar para ver cambios
                onSuccess()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun eliminarProducto(context: Context, producto: ModeloZapatoDTO) {
        viewModelScope.launch {
            // Eliminar imagen localmente si existe (esto se mantiene local)
            if (producto.imagenUrl != null) {
                ImageHelper.deleteImage(context, producto.imagenUrl)
            }

            // Eliminar producto en la API
            inventarioRemoteRepository.eliminarModelo(producto.id).onSuccess {
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                cargarProductos()
            }
        }
    }

    fun buscarProductos(query: String) {
        if (query.isBlank()) {
            cargarProductos()
        } else {
            viewModelScope.launch {
                inventarioRemoteRepository.searchModelos(query).onSuccess { dtos ->
                    _productos.value = dtos?.sortedBy { it.nombre } ?: emptyList()
                }
            }
        }
    }
}
