package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentasViewModel @Inject constructor(
    private val ventasRepository: VentasRemoteRepository
) : ViewModel() {

    private val _ventas = MutableStateFlow<List<BoletaDTO>>(emptyList())
    val ventas: StateFlow<List<BoletaDTO>> = _ventas.asStateFlow()

    private val _ventasFiltradas = MutableStateFlow<List<BoletaDTO>>(emptyList())
    val ventasFiltradas: StateFlow<List<BoletaDTO>> = _ventasFiltradas.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fechaFiltro = MutableStateFlow<Long?>(null)
    val fechaFiltro: StateFlow<Long?> = _fechaFiltro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        cargarVentas()
    }

    private fun cargarVentas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ventasRepository.obtenerTodasLasBoletas()

                if (result.isSuccess) {
                    _ventas.value = result.getOrNull() ?: emptyList()
                    aplicarFiltros()
                } else {
                    _errorMessage.value = "Error al cargar ventas: ${result.exceptionOrNull()?.message}"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar ventas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
        aplicarFiltros()
    }

    fun actualizarFechaFiltro(fecha: Long?) {
        _fechaFiltro.value = fecha
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val query = _searchQuery.value.lowercase().trim()
        val fechaSeleccionada = _fechaFiltro.value

        _ventasFiltradas.value = _ventas.value.filter { boleta ->
            val coincideTexto = if (query.isEmpty()) {
                true
            } else {
                boleta.id.toString().contains(query) ||
                boleta.estado.lowercase().contains(query) ||
                boleta.metodoPago.lowercase().contains(query)
            }

            val coincideFecha = if (fechaSeleccionada != null) {
                esMismaFecha(parseFecha(boleta.fechaVenta), fechaSeleccionada)
            } else {
                true
            }

            coincideTexto && coincideFecha
        }
    }

    private fun parseFecha(fechaStr: String): Long {
        return try {
            java.time.Instant.parse(fechaStr).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    private fun esMismaFecha(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp1
        }
        val cal2 = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp2
        }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH)
    }

    fun cancelarVenta(idBoleta: Int) {
        viewModelScope.launch {
            try {
                val result = ventasRepository.cambiarEstadoBoleta(idBoleta, "CANCELADA")

                if (result.isSuccess) {
                    _successMessage.value = "Venta cancelada exitosamente"
                    cargarVentas()
                } else {
                    _errorMessage.value = "Error al cancelar venta: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cancelar venta: ${e.message}"
            }
        }
    }

    fun limpiarMensajes() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun limpiarFiltros() {
        _searchQuery.value = ""
        _fechaFiltro.value = null
        aplicarFiltros()
    }

    fun recargarVentas() {
        cargarVentas()
    }
}

