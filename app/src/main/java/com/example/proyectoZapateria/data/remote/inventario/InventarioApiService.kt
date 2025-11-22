package com.example.proyectoZapateria.data.remote.inventario

import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InventarioApiService {

    // --- GESTIÓN DE STOCK ---

    @GET("api/inventario/producto/{modeloId}")
    suspend fun obtenerInventarioPorModelo(@Path("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("api/inventario/{id}") // FALTA ESTE
    suspend fun obtenerInventarioPorId(@Path("id") id: Long): Response<InventarioDTO>

    @GET("api/inventario/stock-bajo") // FALTA ESTE (Para reportes)
    suspend fun obtenerStockBajo(): Response<List<InventarioDTO>>

    @POST("api/inventario/crear")
    suspend fun crearInventario(@Body inventario: InventarioDTO): Response<InventarioDTO>

    @PUT("api/inventario/{id}")
    suspend fun actualizarInventario(
        @Path("id") id: Long,
        @Body inventario: InventarioDTO
    ): Response<InventarioDTO>

    @DELETE("api/inventario/eliminar/{id}")
    suspend fun eliminarInventario(@Path("id") id: Long): Response<Void>



}