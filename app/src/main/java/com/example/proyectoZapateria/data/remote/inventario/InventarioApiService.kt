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

    // --- PRIMARIAS (rutas que el backend expone en /inventario) ---
    @GET("inventario/producto/{modeloId}")
    suspend fun obtenerInventarioPorModelo(@Path("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("inventario/producto/{modeloId}/talla/{talla}")
    suspend fun obtenerInventarioPorModeloYtalla(
        @Path("modeloId") modeloId: Long,
        @Path("talla") talla: String
    ): Response<InventarioDTO>

    // Rutas alternativas que algunos backends podrían exponer (mantener por compatibilidad)
    @GET("api/inventario/producto/{modeloId}")
    suspend fun obtenerInventarioPorModelo_Api(@Path("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("inventario/modelo/{modeloId}")
    suspend fun obtenerInventarioPorModeloAlt_NoApi(@Path("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("api/inventario/modelo/{modeloId}")
    suspend fun obtenerInventarioPorModeloAlt(@Path("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("inventario")
    suspend fun obtenerInventarioPorQuery(@retrofit2.http.Query("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("api/inventario")
    suspend fun obtenerInventarioPorQuery_Api(@retrofit2.http.Query("modeloId") modeloId: Long): Response<List<InventarioDTO>>

    @GET("inventario/{id}")
    suspend fun obtenerInventarioPorId(@Path("id") id: Long): Response<InventarioDTO>

    @GET("api/inventario/{id}")
    suspend fun obtenerInventarioPorId_Api(@Path("id") id: Long): Response<InventarioDTO>

    @GET("inventario/stock-bajo")
    suspend fun obtenerStockBajo(): Response<List<InventarioDTO>>

    @POST("inventario/crear")
    suspend fun crearInventario(@Body inventario: InventarioDTO): Response<InventarioDTO>

    @POST("api/inventario/crear")
    suspend fun crearInventario_Api(@Body inventario: InventarioDTO): Response<InventarioDTO>

    @PUT("inventario/{id}")
    suspend fun actualizarInventario(
        @Path("id") id: Long,
        @Body inventario: InventarioDTO
    ): Response<InventarioDTO>

    @PUT("api/inventario/{id}")
    suspend fun actualizarInventario_Api(
        @Path("id") id: Long,
        @Body inventario: InventarioDTO
    ): Response<InventarioDTO>

    @DELETE("inventario/eliminar/{id}")
    suspend fun eliminarInventario(@Path("id") id: Long): Response<Void>

    @DELETE("api/inventario/eliminar/{id}")
    suspend fun eliminarInventario_Api(@Path("id") id: Long): Response<Void>

}