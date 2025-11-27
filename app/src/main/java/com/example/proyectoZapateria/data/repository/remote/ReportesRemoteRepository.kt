package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.reportes.ReportesApiService
import com.example.proyectoZapateria.data.remote.reportes.dto.*
import com.example.proyectoZapateria.data.remote.ventas.ReportesVentasApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportesRemoteRepository @Inject constructor(
    private val api: ReportesApiService,
    private val ventasApi: ReportesVentasApiService
) {

    private suspend fun <T> safe(call: suspend () -> Response<T>): Result<T> {
        return try {
            val res = call()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                val errorMsg = when (res.code()) {
                    404 -> "Endpoint no disponible (404). Los reportes aún no están implementados en el backend."
                    500 -> "Error interno del servidor (500)"
                    else -> "Error ${res.code()}: ${res.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchEstadisticasGenerales(desde: String? = null, hasta: String? = null): Result<EstadisticasGeneralesDTO> {
        return safe { api.obtenerEstadisticasGenerales(desde, hasta) }
    }

    suspend fun fetchStockBajo(): Result<List<StockBajoItemDTO>> {
        return safe { api.obtenerStockBajo() }
    }

    suspend fun fetchMovimientosEstadisticas(): Result<MovimientosEstadisticasDTO> {
        return safe { api.obtenerMovimientosEstadisticas() }
    }

    suspend fun fetchEstadisticasProducto(productoId: Long): Result<ProductoEstadisticasDTO> {
        return safe { api.obtenerEstadisticasProducto(productoId) }
    }

    suspend fun fetchTopStock(limit: Int = 10): Result<List<TopProductoDTO>> {
        return safe { api.obtenerTopStock(limit) }
    }

    suspend fun generarReporte(filtro: FiltroReporteRequest): Result<ReporteVentasDTO> {
        return safe { ventasApi.generarReporte(filtro) }
    }

}
