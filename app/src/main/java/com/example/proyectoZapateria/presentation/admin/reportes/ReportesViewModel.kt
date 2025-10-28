package com.example.proyectoZapateria.presentation.admin.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.ReportesRepository
import com.example.proyectoZapateria.domain.model.FiltroReporte
import com.example.proyectoZapateria.domain.model.ReporteVentas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val reportesRepository: ReportesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesUiState>(ReportesUiState.Initial)
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()

    private val _mesSeleccionado = MutableStateFlow<Int?>(null)
    val mesSeleccionado: StateFlow<Int?> = _mesSeleccionado.asStateFlow()

    private val _reporteActual = MutableStateFlow<ReporteVentas?>(null)
    val reporteActual: StateFlow<ReporteVentas?> = _reporteActual.asStateFlow()

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

            val filtro = FiltroReporte(
                mes = _mesSeleccionado.value,
                anio = _anioSeleccionado.value
            )

            reportesRepository.generarReporte(filtro).fold(
                onSuccess = { reporte ->
                    _reporteActual.value = reporte
                    _uiState.value = ReportesUiState.Success(reporte)
                },
                onFailure = { error ->
                    _uiState.value = ReportesUiState.Error(
                        error.message ?: "Error al generar reporte"
                    )
                }
            )
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
    data class Success(val reporte: ReporteVentas) : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
}

