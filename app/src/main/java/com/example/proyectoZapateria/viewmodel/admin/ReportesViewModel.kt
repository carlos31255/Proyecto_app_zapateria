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

    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()

    private val _mesSeleccionado = MutableStateFlow<Int?>(null)
    val mesSeleccionado: StateFlow<Int?> = _mesSeleccionado.asStateFlow()

    private val _reporteActual = MutableStateFlow<ReporteVentasDTO?>(null)
    val reporteActual: StateFlow<ReporteVentasDTO?> = _reporteActual.asStateFlow()

    fun seleccionarAnio(anio: Int) {
        _anioSeleccionado.value = anio
        // Al cambiar el año, resetear el mes si estaba seleccionado
        if (_mesSeleccionado.value != null) {
            generarReporte()
        }
    }

    fun seleccionarMes(mes: Int?) {
        _mesSeleccionado.value = mes
        generarReporte()
    }

    fun generarReporte() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState.Loading

            try {
                val result = reportesRemoteRepository.fetchReporteVentas(
                    mes = _mesSeleccionado.value,
                    anio = _anioSeleccionado.value
                )

                if (result.isFailure) {
                    _uiState.value = ReportesUiState.Error(
                        result.exceptionOrNull()?.message ?: "Error al obtener reporte"
                    )
                    return@launch
                }

                val reporte = result.getOrNull()
                if (reporte != null) {
                    _reporteActual.value = reporte
                    _uiState.value = ReportesUiState.Success(reporte)
                } else {
                    _uiState.value = ReportesUiState.Error("No se pudo obtener el reporte")
                }

            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error(e.message ?: "Error al generar reporte")
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
    data class Success(val reporte: ReporteVentasDTO) : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
}
