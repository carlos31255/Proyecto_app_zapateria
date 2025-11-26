package com.example.proyectoZapateria.data.remote.reportes

import com.example.proyectoZapateria.data.remote.reportes.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportesApiService {
    @GET("reportes/estadisticas-generales")
    suspend fun obtenerEstadisticasGenerales(
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null
    ): Response<EstadisticasGeneralesDTO>

    @GET("reportes/stock-bajo")
    suspend fun obtenerStockBajo(): Response<List<StockBajoItemDTO>>

    @GET("reportes/movimientos/estadisticas")
    suspend fun obtenerMovimientosEstadisticas(): Response<MovimientosEstadisticasDTO>

    @GET("reportes/productos/{productoId}/estadisticas")
    suspend fun obtenerEstadisticasProducto(
        @Path("productoId") productoId: Long
    ): Response<ProductoEstadisticasDTO>

    @GET("reportes/productos/top-stock")
    suspend fun obtenerTopStock(
        @Query("limit") limit: Int = 10
    ): Response<List<TopProductoDTO>>
}

