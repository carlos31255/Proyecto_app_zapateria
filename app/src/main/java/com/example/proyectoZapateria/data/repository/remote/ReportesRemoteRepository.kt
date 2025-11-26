package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.reportes.ReportesApiService
import com.example.proyectoZapateria.data.remote.reportes.dto.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportesRemoteRepository @Inject constructor(
    private val api: ReportesApiService
) {
    companion object { private const val TAG = "ReportesRemoteRepo" }

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
        Log.d(TAG, "fetchEstadisticasGenerales desde=$desde hasta=$hasta")
        return safe { api.obtenerEstadisticasGenerales(desde, hasta) }
    }

    suspend fun fetchStockBajo(): Result<List<StockBajoItemDTO>> {
        Log.d(TAG, "fetchStockBajo")
        return safe { api.obtenerStockBajo() }
    }

    suspend fun fetchMovimientosEstadisticas(): Result<MovimientosEstadisticasDTO> {
        Log.d(TAG, "fetchMovimientosEstadisticas")
        return safe { api.obtenerMovimientosEstadisticas() }
    }

    suspend fun fetchEstadisticasProducto(productoId: Long): Result<ProductoEstadisticasDTO> {
        Log.d(TAG, "fetchEstadisticasProducto productoId=$productoId")
        return safe { api.obtenerEstadisticasProducto(productoId) }
    }

    suspend fun fetchTopStock(limit: Int = 10): Result<List<TopProductoDTO>> {
        Log.d(TAG, "fetchTopStock limit=$limit")
        return safe { api.obtenerTopStock(limit) }
    }
}

