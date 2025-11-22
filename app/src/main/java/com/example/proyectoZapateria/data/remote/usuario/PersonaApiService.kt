package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import retrofit2.Response
import retrofit2.http.*

interface PersonaApiService {

    // ========== ENDPOINTS DE PERSONAS ==========

    // Obtener todas las personas
    @GET("api/personas")
    suspend fun obtenerTodasLasPersonas(): List<PersonaDTO>

    // Obtener persona por ID
    @GET("api/personas/{id}")
    suspend fun obtenerPersonaPorId(@Path("id") id: Long): Response<PersonaDTO>

    // Obtener persona por RUT
    @GET("api/personas/rut/{rut}")
    suspend fun obtenerPersonaPorRut(@Path("rut") rut: String): Response<PersonaDTO>

    // Obtener persona por username
    @GET("api/personas/username/{username}")
    suspend fun obtenerPersonaPorUsername(@Path("username") username: String): Response<PersonaDTO>

    // Buscar personas por nombre (búsqueda parcial)
    @GET("api/personas/buscar")
    suspend fun buscarPersonasPorNombre(@Query("nombre") nombre: String): List<PersonaDTO>

    // Obtener personas por estado (activo/inactivo)
    @GET("api/personas/estado/{estado}")
    suspend fun obtenerPersonasPorEstado(@Path("estado") estado: String): List<PersonaDTO>

    // Crear nueva persona
    @POST("api/personas")
    suspend fun crearPersona(@Body personaDTO: PersonaDTO): Response<PersonaDTO>

    // Actualizar persona
    @PUT("api/personas/{id}")
    suspend fun actualizarPersona(
        @Path("id") id: Long,
        @Body personaDTO: PersonaDTO
    ): Response<PersonaDTO>

    // Desactivar persona (borrado lógico - cambia estado a inactivo)
    @DELETE("api/personas/{id}")
    suspend fun eliminarPersona(@Path("id") id: Long): Response<Void>

    // Verificar credenciales para autenticación
    @POST("api/personas/verificar-credenciales")
    suspend fun verificarCredenciales(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<PersonaDTO>

}
