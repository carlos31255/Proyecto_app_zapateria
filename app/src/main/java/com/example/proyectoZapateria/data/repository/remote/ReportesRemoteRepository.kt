package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.reportes.ReportesApiService
import com.example.proyectoZapateria.data.remote.reportes.dto.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportesRemoteRepository @Inject constructor(
    private val api: ReportesApiService
) {

    private suspend fun <T> safe(call: suspend () -> Response<T>): Result<T> {
        return try {
            val res = call()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                val err = try { res.errorBody()?.string() } catch (_: Exception) { null }
                Result.failure(Exception("Error ${res.code()}: ${res.message()} - body=${err ?: "<empty>"}"))
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

    suspend fun fetchReporteVentas(mes: Int? = null, anio: Int): Result<ReporteVentasDTO> {
        return safe { api.obtenerReporteVentas(mes, anio) }
    }
}
