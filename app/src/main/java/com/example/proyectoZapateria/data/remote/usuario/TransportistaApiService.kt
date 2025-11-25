package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import retrofit2.Response
import retrofit2.http.*

interface TransportistaApiService {

    // Obtener todos los transportistas
    @GET("transportistas")
    suspend fun obtenerTodos(): Response<List<TransportistaDTO>>

    // Obtener transportista por ID
    @GET("transportistas/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): Response<TransportistaDTO>

    // Obtener transportista por personaId
    @GET("transportistas/persona/{personaId}")
    suspend fun obtenerPorPersona(@Path("personaId") personaId: Long): Response<TransportistaDTO>

    // Crear transportista
    @POST("transportistas")
    suspend fun crear(@Body dto: TransportistaDTO): Response<TransportistaDTO>

    // Actualizar transportista
    @PUT("transportistas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: TransportistaDTO): Response<TransportistaDTO>

    // Eliminar transportista
    @DELETE("transportistas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Void>
}
