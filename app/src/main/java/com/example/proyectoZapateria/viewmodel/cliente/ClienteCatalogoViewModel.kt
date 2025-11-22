package com.example.proyectoZapateria.viewmodel.cliente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import com.example.proyectoZapateria.ui.model.InventarioUi
import com.example.proyectoZapateria.data.repository.remote.InventarioRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClienteCatalogoVM"

@HiltViewModel
class ClienteCatalogoViewModel @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) : ViewModel() {

    private val _modelos = MutableStateFlow<List<ModeloZapatoDTO>>(emptyList())
    val modelos: StateFlow<List<ModeloZapatoDTO>> = _modelos.asStateFlow()

    private val _marcas = MutableStateFlow<List<MarcaDTO>>(emptyList())
    val marcas: StateFlow<List<MarcaDTO>> = _marcas.asStateFlow()

    private val _inventarioPorModelo = MutableStateFlow<Map<Long, List<InventarioUi>>>(emptyMap())
    val inventarioPorModelo: StateFlow<Map<Long, List<InventarioUi>>> = _inventarioPorModelo.asStateFlow()

    private val _tallas = MutableStateFlow<List<TallaDTO>>(emptyList())
    val tallas: StateFlow<List<TallaDTO>> = _tallas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar tallas
                val tallasRes = inventarioRemoteRepository.getTallas()
                if (tallasRes.isSuccess) {
                    _tallas.value = tallasRes.getOrNull() ?: emptyList()
                    Log.d(TAG, "Tallas cargadas: ${_tallas.value.size}")
                } else {
                    Log.e(TAG, "Error cargando tallas: ${tallasRes.exceptionOrNull()?.message}")
                }

                // Cargar marcas
                val marcasRes = inventarioRemoteRepository.getMarcas()
                if (marcasRes.isSuccess) {
                    _marcas.value = marcasRes.getOrNull() ?: emptyList()
                    Log.d(TAG, "Marcas cargadas: ${_marcas.value.size}")
                } else {
                    Log.e(TAG, "Error cargando marcas: ${marcasRes.exceptionOrNull()?.message}")
                }

                // Cargar modelos
                val modelosRes = inventarioRemoteRepository.getModelos()
                if (modelosRes.isSuccess) {
                    _modelos.value = modelosRes.getOrNull() ?: emptyList()
                    Log.d(TAG, "Modelos cargados: ${_modelos.value.size}")
                } else {
                    Log.e(TAG, "Error cargando modelos: ${modelosRes.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción cargando catálogo: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarInventarioParaModelo(idModelo: Long) {
        viewModelScope.launch {
            val res = inventarioRemoteRepository.getInventarioPorModelo(idModelo)
            res.onSuccess { dtos ->
                val listaLocal = dtos?.mapNotNull { dto ->
                    val tallaLocal = _tallas.value.find { it.valor == dto.talla }
                    InventarioUi(
                        idRemote = dto.id ?: 0L,
                        idModelo = idModelo,
                        talla = dto.talla,
                        tallaIdLocal = tallaLocal?.id,
                        stock = dto.cantidad
                    )
                } ?: emptyList()
                _inventarioPorModelo.value = _inventarioPorModelo.value + (idModelo to listaLocal)
                Log.d(TAG, "Inventario para modelo $idModelo cargado: ${listaLocal.size} items")
            }.onFailure { ex ->
                Log.e(TAG, "Error cargando inventario para modelo $idModelo: ${ex.message}")
            }
        }
    }
}
