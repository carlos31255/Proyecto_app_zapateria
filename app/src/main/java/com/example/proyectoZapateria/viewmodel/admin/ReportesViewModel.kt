package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.remote.ReportesRemoteRepository
import com.example.proyectoZapateria.data.remote.reportes.dto.ReporteVentasDTO
import com.example.proyectoZapateria.data.remote.reportes.dto.FiltroReporteRequest

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import java.util.Calendar
import javax.inject.Inject

sealed class ReportesUiState {
    object Initial : ReportesUiState()
    object Loading : ReportesUiState()
    data class Success(val reporte: ReporteVentasDTO) : ReportesUiState()
    data class Error(val message: String) : ReportesUiState()
}

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val reportesRepository: ReportesRemoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesUiState>(ReportesUiState.Initial)
    val uiState: StateFlow<ReportesUiState> = _uiState

    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado

    private val _mesSeleccionado = MutableStateFlow<Int?>(null)
    val mesSeleccionado: StateFlow<Int?> = _mesSeleccionado

    fun generarReporte() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState.Loading
            try {
                val filtro = FiltroReporteRequest(
                    anio = _anioSeleccionado.value,
                    mes = _mesSeleccionado.value
                )

                val resultado = reportesRepository.generarReporte(filtro)

                resultado.onSuccess { reporte ->
                    _uiState.value = ReportesUiState.Success(reporte)
                }.onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("404") == true -> "Funcionalidad no disponible. (Error 404)"
                        else -> error.message ?: "Error desconocido"
                    }
                    _uiState.value = ReportesUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun seleccionarAnio(anio: Int) {
        _anioSeleccionado.value = anio
    }

    fun seleccionarMes(mes: Int?) {
        _mesSeleccionado.value = mes
    }

    fun obtenerAniosDisponibles(): List<Int> {
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        return (anioActual downTo 2020).toList()
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
