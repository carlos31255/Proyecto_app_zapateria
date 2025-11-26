package com.example.proyectoZapateria.data.remote.ventas

import com.example.proyectoZapateria.data.remote.reportes.dto.FiltroReporteRequest
import com.example.proyectoZapateria.data.remote.reportes.dto.ReporteVentasDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportesVentasApiService {
    @POST("ventas/estadisticas/generar")
    suspend fun generarReporte(@Body filtro: FiltroReporteRequest): Response<ReporteVentasDTO>
}

