package com.example.proyectoZapateria.viewmodel.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteCatalogoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _modelos = MutableStateFlow<List<ModeloZapatoEntity>>(emptyList())
    val modelos: StateFlow<List<ModeloZapatoEntity>> = _modelos.asStateFlow()

    private val _marcas = MutableStateFlow<List<MarcaEntity>>(emptyList())
    val marcas: StateFlow<List<MarcaEntity>> = _marcas.asStateFlow()

    private val _inventarioPorModelo = MutableStateFlow<Map<Int, List<InventarioEntity>>>(emptyMap())
    val inventarioPorModelo: StateFlow<Map<Int, List<InventarioEntity>>> = _inventarioPorModelo.asStateFlow()

    init {
        cargarModelos()
        cargarMarcas()
    }

    private fun cargarModelos() {
        viewModelScope.launch {
            productoRepository.getModelosActivos().collect { lista ->
                _modelos.value = lista
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

    fun cargarInventarioParaModelo(idModelo: Int) {
        viewModelScope.launch {
            inventarioRepository.getByModelo(idModelo).collect { lista ->
                _inventarioPorModelo.value = _inventarioPorModelo.value + (idModelo to lista)
            }
        }
    }
}

