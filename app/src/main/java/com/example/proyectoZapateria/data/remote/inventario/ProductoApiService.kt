package com.example.proyectoZapateria.data.remote.inventario

import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
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

    // --- MODELOS (PRODUCTOS) ---
    @GET("api/modelos")
    suspend fun obtenerTodosLosModelos(): Response<List<ModeloZapatoDTO>>

    @GET("api/modelos/{id}") // FALTA ESTE
    suspend fun obtenerModeloPorId(@Path("id") id: Long): Response<ModeloZapatoDTO>

    @GET("api/modelos/marca/{marcaId}") // FALTA ESTE
    suspend fun obtenerModelosPorMarca(@Path("marcaId") marcaId: Long): Response<List<ModeloZapatoDTO>>

    @GET("api/modelos/buscar") // FALTA ESTE (Query param: ?nombre=Nike)
    suspend fun buscarModelos(@Query("nombre") query: String): Response<List<ModeloZapatoDTO>>

    @POST("api/modelos")
    suspend fun crearModelo(@Body modelo: ModeloZapatoDTO): Response<ModeloZapatoDTO>

    @PUT("api/modelos/{id}")
    suspend fun actualizarModelo(@Path("id") id: Long, @Body modelo: ModeloZapatoDTO): Response<ModeloZapatoDTO>

    @DELETE("api/modelos/{id}")
    suspend fun eliminarModelo(@Path("id") id: Long): Response<Void>

}