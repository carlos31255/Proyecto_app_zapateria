package com.example.proyectoZapateria.data.remote.entregas

import com.example.proyectoZapateria.data.remote.entregas.dto.ActualizarEstadoRequest
import com.example.proyectoZapateria.data.remote.entregas.dto.CompletarEntregaRequest
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import retrofit2.Response
import retrofit2.http.*

interface EntregasApiService {

    @GET("entregas")
    suspend fun obtenerTodasLasEntregas(): Response<List<EntregaDTO>>

    @GET("entregas/{id}")
    suspend fun obtenerEntregaPorId(@Path("id") id: Long): Response<EntregaDTO>

    @GET("entregas/transportista/{transportistaId}")
    suspend fun obtenerEntregasPorTransportista(@Path("transportistaId") transportistaId: Long): Response<List<EntregaDTO>>

    @GET("entregas/estado/{estado}")
    suspend fun obtenerEntregasPorEstado(@Path("estado") estado: String): Response<List<EntregaDTO>>

    @PUT("entregas/{id}/asignar")
    suspend fun asignarTransportista(
        @Path("id") id: Long,
        @Query("transportistaId") transportistaId: Long
    ): Response<EntregaDTO>

    @PUT("entregas/{id}/completar")
    suspend fun completarEntrega(
        @Path("id") id: Long,
        @Body body: CompletarEntregaRequest
    ): Response<EntregaDTO>

    @PUT("entregas/{id}/estado")
    suspend fun cambiarEstadoEntrega(
        @Path("id") id: Long,
        @Body body: ActualizarEstadoRequest
    ): Response<EntregaDTO>
}
