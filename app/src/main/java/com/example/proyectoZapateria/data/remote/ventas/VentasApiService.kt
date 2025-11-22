package com.example.proyectoZapateria.data.remote.ventas

import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import retrofit2.Response
import retrofit2.http.*

interface VentasApiService {

    @GET("api/ventas/boletas")
    suspend fun obtenerTodasLasBoletas(): Response<List<BoletaDTO>>

    @GET("api/ventas/boletas/{id}")
    suspend fun obtenerBoletaPorId(@Path("id") id: Long): Response<BoletaDTO>

    @GET("api/ventas/boletas/cliente/{clienteId}")
    suspend fun obtenerBoletasPorCliente(@Path("clienteId") clienteId: Long): Response<List<BoletaDTO>>

    // Alternativa: algunos backends usan query param en vez de path
    @GET("api/ventas/boletas")
    suspend fun obtenerBoletasPorClienteQuery(@Query("clienteId") clienteId: Long): Response<List<BoletaDTO>>

    @POST("api/ventas/boletas")
    suspend fun crearBoleta(@Body request: CrearBoletaRequest): Response<BoletaDTO>

    @PUT("api/ventas/boletas/{id}/estado")
    suspend fun cambiarEstadoBoleta(
        @Path("id") id: Long,
        @Body request: CambiarEstadoRequest
    ): Response<BoletaDTO>

    @DELETE("api/ventas/boletas/{id}")
    suspend fun eliminarBoleta(@Path("id") id: Long): Response<Void>
}
