package com.example.proyectoZapateria.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.domain.model.FiltroReporte
import com.example.proyectoZapateria.domain.model.ReporteVentas
import com.example.proyectoZapateria.domain.model.DetalleVentaReporte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val ventasRemoteRepository: VentasRemoteRepository
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

            try {
                // Obtener todas las boletas desde el microservicio de ventas
                val todasBoletasResult = ventasRemoteRepository.obtenerTodasLasBoletas()

                if (todasBoletasResult.isFailure) {
                    _uiState.value = ReportesUiState.Error(
                        todasBoletasResult.exceptionOrNull()?.message ?: "Error al obtener boletas"
                    )
                    return@launch
                }

                val todasBoletas = todasBoletasResult.getOrNull() ?: emptyList()

                val (fechaInicio, fechaFin) = calcularRangoFechas(filtro)

                // Función auxiliar para parsear fechas tolerante a varios formatos
                fun parseFechaMillis(fecha: String?): Long {
                    if (fecha.isNullOrBlank()) return 0L

                    val attempts = listOf<() -> Long>(
                        {
                            Instant.parse(fecha).toEpochMilli()
                        },
                        {
                            OffsetDateTime.parse(fecha).toInstant().toEpochMilli()
                        },
                        {
                            LocalDateTime.parse(fecha).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        },
                        {
                            try {
                                val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]")
                                val ldt = LocalDateTime.parse(fecha, fmt)
                                return@listOf ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            } catch (ex: Exception) {
                                throw ex
                            }
                        },
                        {
                            fecha.toLong()
                        }
                    )

                    for (attempt in attempts) {
                        try {
                            val v = attempt()
                            return v
                        } catch (_: Exception) {
                        }
                    }
                    return 0L
                }

                val boletasFiltradas = todasBoletas.filter { boleta ->
                    val fechaBoleta = parseFechaMillis(boleta.fechaVenta)
                    fechaBoleta in fechaInicio until fechaFin
                }

                // Si no encontramos boletas por parseo, intentar una coincidencia por texto (year-month)
                val boletasFiltradasFinal = if (boletasFiltradas.isEmpty() && todasBoletas.isNotEmpty()) {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = fechaInicio
                    val targetYear = cal.get(Calendar.YEAR)
                    val targetMonth = cal.get(Calendar.MONTH) + 1 // 1-based

                    fun extractYearMonth(fechaStr: String?): Pair<Int, Int>? {
                        if (fechaStr.isNullOrBlank()) return null
                        val regex = Regex("(\\d{4})[-/](\\d{2})")
                        val m = regex.find(fechaStr)
                        return if (m != null) {
                            val y = m.groupValues[1].toIntOrNull()
                            val mo = m.groupValues[2].toIntOrNull()
                            if (y != null && mo != null) Pair(y, mo) else null
                        } else null
                    }

                    val matched = todasBoletas.filter { b ->
                        val ym = extractYearMonth(b.fechaVenta)
                        ym != null && ym.first == targetYear && ym.second == targetMonth
                    }
                    matched
                } else boletasFiltradas

                val ventasRealizadas = boletasFiltradasFinal.filter { it.estado.uppercase() != "CANCELADA" }
                val ventasCanceladas = boletasFiltradasFinal.filter { it.estado.uppercase() == "CANCELADA" }

                val ingresosTotal = ventasRealizadas.sumOf { it.total }

                val detallesVentas = if (filtro.mes != null) {
                    boletasFiltradasFinal.map { boleta ->
                        val fechaTimestamp = parseFechaMillis(boleta.fechaVenta)

                        DetalleVentaReporte(
                            numeroBoleta = "B-${boleta.id}",
                            fecha = fechaTimestamp,
                            nombreCliente = "Cliente #${boleta.clienteId}",
                            montoTotal = boleta.total,
                            estado = if (boleta.estado.uppercase() == "CANCELADA") "cancelada" else "realizada"
                        )
                    }
                } else {
                    emptyList()
                }

                val reporte = ReporteVentas(
                    numeroVentasRealizadas = ventasRealizadas.size,
                    numeroVentasCanceladas = ventasCanceladas.size,
                    ingresosTotal = ingresosTotal,
                    detallesVentas = detallesVentas
                )

                _reporteActual.value = reporte
                _uiState.value = ReportesUiState.Success(reporte)

            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error(e.message ?: "Error al generar reporte")
            }
        }
    }

    private fun calcularRangoFechas(filtro: FiltroReporte): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        if (filtro.mes != null) {
            calendar.set(Calendar.YEAR, filtro.anio)
            calendar.set(Calendar.MONTH, filtro.mes - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val fechaInicio = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            val fechaFin = calendar.timeInMillis

            return Pair(fechaInicio, fechaFin)
        } else {
            calendar.set(Calendar.YEAR, filtro.anio)
            calendar.set(Calendar.MONTH, 0)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val fechaInicio = calendar.timeInMillis

            calendar.set(Calendar.YEAR, filtro.anio + 1)
            val fechaFin = calendar.timeInMillis

            return Pair(fechaInicio, fechaFin)
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
