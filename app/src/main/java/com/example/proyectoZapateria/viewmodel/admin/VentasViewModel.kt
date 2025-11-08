package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaConInfo
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentasViewModel @Inject constructor(
    private val boletaVentaDao: BoletaVentaDao
) : ViewModel() {

    private val _ventas = MutableStateFlow<List<BoletaVentaConInfo>>(emptyList())
    val ventas: StateFlow<List<BoletaVentaConInfo>> = _ventas.asStateFlow()

    private val _ventasFiltradas = MutableStateFlow<List<BoletaVentaConInfo>>(emptyList())
    val ventasFiltradas: StateFlow<List<BoletaVentaConInfo>> = _ventasFiltradas.asStateFlow()

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
                boletaVentaDao.getAllBoletasConInfo().collectLatest { ventas ->
                    _ventas.value = ventas
                    aplicarFiltros()
                    _isLoading.value = false
                }
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

        _ventasFiltradas.value = _ventas.value.filter { venta ->
            // Filtro por búsqueda de texto
            val coincideTexto = if (query.isEmpty()) {
                true
            } else {
                val nombreCompleto = "${venta.nombre_cliente} ${venta.apellido_cliente}".lowercase()
                nombreCompleto.contains(query) ||
                        venta.numero_boleta.lowercase().contains(query) ||
                        venta.estado.lowercase().contains(query)
            }

            // Filtro por fecha
            val coincideFecha = if (fechaSeleccionada != null) {
                esMismaFecha(venta.fecha, fechaSeleccionada)
            } else {
                true
            }

            coincideTexto && coincideFecha
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
                boletaVentaDao.cancelarBoleta(idBoleta)
                _successMessage.value = "Venta cancelada exitosamente"
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
}

