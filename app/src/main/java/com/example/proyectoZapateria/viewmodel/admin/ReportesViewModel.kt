package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.remote.ReportesRemoteRepository
import com.example.proyectoZapateria.data.remote.reportes.dto.ReporteVentasDTO

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val reportesRemoteRepository: ReportesRemoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesUiState>(ReportesUiState.Initial)
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    private val _estadisticasGenerales = MutableStateFlow<com.example.proyectoZapateria.data.remote.reportes.dto.EstadisticasGeneralesDTO?>(null)
    val estadisticasGenerales: StateFlow<com.example.proyectoZapateria.data.remote.reportes.dto.EstadisticasGeneralesDTO?> = _estadisticasGenerales.asStateFlow()

    private val _stockBajo = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.reportes.dto.StockBajoItemDTO>>(emptyList())
    val stockBajo: StateFlow<List<com.example.proyectoZapateria.data.remote.reportes.dto.StockBajoItemDTO>> = _stockBajo.asStateFlow()

    private val _topStock = MutableStateFlow<List<com.example.proyectoZapateria.data.remote.reportes.dto.TopProductoDTO>>(emptyList())
    val topStock: StateFlow<List<com.example.proyectoZapateria.data.remote.reportes.dto.TopProductoDTO>> = _topStock.asStateFlow()

    private val _movimientosEstadisticas = MutableStateFlow<com.example.proyectoZapateria.data.remote.reportes.dto.MovimientosEstadisticasDTO?>(null)
    val movimientosEstadisticas: StateFlow<com.example.proyectoZapateria.data.remote.reportes.dto.MovimientosEstadisticasDTO?> = _movimientosEstadisticas.asStateFlow()

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState.Loading

            try {
                // Cargar estadísticas generales
                val estadisticasResult = reportesRemoteRepository.fetchEstadisticasGenerales()
                if (estadisticasResult.isSuccess) {
                    _estadisticasGenerales.value = estadisticasResult.getOrNull()
                }

                // Cargar stock bajo
                val stockBajoResult = reportesRemoteRepository.fetchStockBajo()
                if (stockBajoResult.isSuccess) {
                    _stockBajo.value = stockBajoResult.getOrNull() ?: emptyList()
                }

                // Cargar top stock
                val topStockResult = reportesRemoteRepository.fetchTopStock(10)
                if (topStockResult.isSuccess) {
                    _topStock.value = topStockResult.getOrNull() ?: emptyList()
                }

                // Cargar estadísticas de movimientos
                val movimientosResult = reportesRemoteRepository.fetchMovimientosEstadisticas()
                if (movimientosResult.isSuccess) {
                    _movimientosEstadisticas.value = movimientosResult.getOrNull()
                }

                // Si todo fue exitoso
                if (estadisticasResult.isSuccess) {
                    _uiState.value = ReportesUiState.Success
                } else {
                    _uiState.value = ReportesUiState.Error(
                        estadisticasResult.exceptionOrNull()?.message ?: "Error al cargar reportes"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error(e.message ?: "Error al cargar reportes")
            }
        }
    }


    fun obtenerAniosDisponibles(): List<Int> {
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        // Mostrar desde 2020 hasta el año actual
        return (2020..anioActual).toList().reversed()
    }

    fun obtenerNombreMes(mes: Int): String {
        return when (mes) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
        }
    }
}

sealed class ReportesUiState {
    object Initial : ReportesUiState()
    object Loading : ReportesUiState()
    object Success : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
}
