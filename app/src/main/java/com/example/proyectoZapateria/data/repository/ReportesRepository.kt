package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.repository.remote.VentasRemoteRepository
import com.example.proyectoZapateria.domain.model.DetalleVentaReporte
import com.example.proyectoZapateria.domain.model.FiltroReporte
import com.example.proyectoZapateria.domain.model.ReporteVentas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Calendar
import javax.inject.Inject

class ReportesRepository @Inject constructor(
    private val ventasRepository: VentasRemoteRepository
) {
    suspend fun generarReporte(filtro: FiltroReporte): Result<ReporteVentas> = withContext(Dispatchers.IO) {
        try {
            val (fechaInicio, fechaFin) = calcularRangoFechas(filtro)

            val todasBoletasResult = ventasRepository.obtenerTodasLasBoletas()

            if (todasBoletasResult.isFailure) {
                return@withContext Result.failure(
                    todasBoletasResult.exceptionOrNull() ?: Exception("Error al obtener boletas")
                )
            }

            val todasBoletas = todasBoletasResult.getOrNull() ?: emptyList()

            val boletasFiltradas = todasBoletas.filter { boleta ->
                try {
                    val fechaBoleta = Instant.parse(boleta.fechaVenta).toEpochMilli()
                    fechaBoleta >= fechaInicio && fechaBoleta < fechaFin
                } catch (e: Exception) {
                    false
                }
            }

            val ventasRealizadas = boletasFiltradas.filter { it.estado.uppercase() != "CANCELADA" }
            val ventasCanceladas = boletasFiltradas.filter { it.estado.uppercase() == "CANCELADA" }

            val ingresosTotal = ventasRealizadas.sumOf { it.total }

            val detallesVentas = if (filtro.mes != null) {
                boletasFiltradas.map { boleta ->
                    val fechaTimestamp = try {
                        Instant.parse(boleta.fechaVenta).toEpochMilli()
                    } catch (e: Exception) {
                        0L
                    }

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

            Result.success(
                ReporteVentas(
                    numeroVentasRealizadas = ventasRealizadas.size,
                    numeroVentasCanceladas = ventasCanceladas.size,
                    ingresosTotal = ingresosTotal,
                    detallesVentas = detallesVentas
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
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
}

