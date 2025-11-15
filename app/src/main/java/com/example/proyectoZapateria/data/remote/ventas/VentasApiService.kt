package com.example.proyectoZapateria.data.remote.ventas

import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import retrofit2.Response
import retrofit2.http.*

interface VentasApiService {

    @GET("api/boletas")
    suspend fun obtenerTodasLasBoletas(): Response<List<BoletaDTO>>

    @GET("api/boletas/{id}")
    suspend fun obtenerBoletaPorId(@Path("id") id: Int): Response<BoletaDTO>

    @GET("api/boletas/cliente/{clienteId}")
    suspend fun obtenerBoletasPorCliente(@Path("clienteId") clienteId: Int): Response<List<BoletaDTO>>

    @POST("api/boletas")
    suspend fun crearBoleta(@Body request: CrearBoletaRequest): Response<BoletaDTO>

    @PUT("api/boletas/{id}/estado")
    suspend fun cambiarEstadoBoleta(
        @Path("id") id: Int,
        @Body request: CambiarEstadoRequest
    ): Response<BoletaDTO>

    @DELETE("api/boletas/{id}")
    suspend fun eliminarBoleta(@Path("id") id: Int): Response<Void>
}

