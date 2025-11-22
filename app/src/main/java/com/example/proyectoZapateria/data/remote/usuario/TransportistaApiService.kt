package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import retrofit2.Response
import retrofit2.http.*

interface TransportistaApiService {

    // Obtener todos los transportistas
    @GET("api/transportistas")
    suspend fun obtenerTodos(): List<TransportistaDTO>

    // Obtener transportista por ID
    @GET("api/transportistas/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): Response<TransportistaDTO>

    // Obtener transportista por personaId
    @GET("api/transportistas/persona/{personaId}")
    suspend fun obtenerPorPersona(@Path("personaId") personaId: Long): Response<TransportistaDTO>

    // Crear transportista
    @POST("api/transportistas")
    suspend fun crear(@Body dto: TransportistaDTO): Response<TransportistaDTO>

    // Actualizar transportista
    @PUT("api/transportistas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: TransportistaDTO): Response<TransportistaDTO>

    // Eliminar transportista
    @DELETE("api/transportistas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Void>
}
