package com.example.proyectoZapateria.data.remote.inventario

import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductoApiService {

    // --- MARCAS ---
    @GET("api/marcas")
    suspend fun obtenerTodasLasMarcas(): Response<List<MarcaDTO>>

    @GET("api/marcas/{id}") // FALTA ESTE
    suspend fun obtenerMarcaPorId(@Path("id") id: Long): Response<MarcaDTO>

    // --- TALLAS ---
    @GET("api/tallas")
    suspend fun obtenerTodasLasTallas(): Response<List<TallaDTO>>

    // --- PRODUCTOS ---
    @GET("/inventario/productos")
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoDTO>>

    @GET("/inventario/productos/{id}")
    suspend fun obtenerProductoPorId(@Path("id") id: Long): Response<ProductoDTO>

    @GET("/inventario/productos/marca/{marcaId}")
    suspend fun obtenerProductosPorMarca(@Path("marcaId") marcaId: Long): Response<List<ProductoDTO>>

    @GET("/inventario/productos/buscar")
    suspend fun buscarProductos(@Query("nombre") query: String): Response<List<ProductoDTO>>

    @POST("/inventario/productos/crear")
    suspend fun crearProducto(@Body producto: ProductoDTO): Response<ProductoDTO>

    @PUT("/inventario/productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Long, @Body producto: ProductoDTO): Response<ProductoDTO>

    @DELETE("/inventario/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Long): Response<Void>

}