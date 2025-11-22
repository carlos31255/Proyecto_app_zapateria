package com.example.proyectoZapateria.data.remote.entregas

import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import retrofit2.Response
import retrofit2.http.*

interface EntregasApiService {

    @GET("api/entregas")
    suspend fun obtenerTodasLasEntregas(): Response<List<EntregaDTO>>

    @GET("api/entregas/{id}")
    suspend fun obtenerEntregaPorId(@Path("id") id: Long): Response<EntregaDTO>

    @GET("api/entregas/transportista/{transportistaId}")
    suspend fun obtenerEntregasPorTransportista(@Path("transportistaId") transportistaId: Long): Response<List<EntregaDTO>>

    @GET("api/entregas/estado/{estado}")
    suspend fun obtenerEntregasPorEstado(@Path("estado") estado: String): Response<List<EntregaDTO>>

    @PUT("api/entregas/{id}/asignar")
    suspend fun asignarTransportista(
        @Path("id") id: Long,
        @Query("transportistaId") transportistaId: Long
    ): Response<EntregaDTO>

    @PUT("api/entregas/{id}/completar")
    suspend fun completarEntrega(
        @Path("id") id: Long,
        @Body observacion: String?
    ): Response<EntregaDTO>

    @PUT("api/entregas/{id}/estado")
    suspend fun cambiarEstadoEntrega(
        @Path("id") id: Long,
        @Query("nuevoEstado") nuevoEstado: String
    ): Response<EntregaDTO>
}
