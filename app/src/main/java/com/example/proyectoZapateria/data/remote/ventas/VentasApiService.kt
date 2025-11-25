package com.example.proyectoZapateria.data.remote.ventas

import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import retrofit2.Response
import retrofit2.http.*

interface VentasApiService {

    @GET("ventas/boletas")
    suspend fun obtenerTodasLasBoletas(): Response<List<BoletaDTO>>

    @GET("ventas/boletas/{id}")
    suspend fun obtenerBoletaPorId(@Path("id") id: Long): Response<BoletaDTO>

    @GET("ventas/boletas/cliente/{clienteId}")
    suspend fun obtenerBoletasPorCliente(@Path("clienteId") clienteId: Long): Response<List<BoletaDTO>>

    // Alternativa: algunos backends usan query param en vez de path
    @GET("ventas/boletas")
    suspend fun obtenerBoletasPorClienteQuery(@Query("clienteId") clienteId: Long): Response<List<BoletaDTO>>

    @POST("ventas/boletas/crear")
    suspend fun crearBoleta(@Body request: CrearBoletaRequest): Response<BoletaDTO>

    @PUT("ventas/boletas/{id}/estado")
    suspend fun cambiarEstadoBoleta(
        @Path("id") id: Long,
        @Body request: CambiarEstadoRequest
    ): Response<BoletaDTO>

    @DELETE("ventas/boletas/{id}")
    suspend fun eliminarBoleta(@Path("id") id: Long): Response<Void>
}
