package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import retrofit2.Response
import retrofit2.http.*

interface PersonaApiService {

    // ========== ENDPOINTS DE PERSONAS ==========

    // Obtener todas las personas
    @GET("personas")
    suspend fun obtenerTodasLasPersonas(): Response<List<PersonaDTO>>

    // Obtener persona por ID
    @GET("personas/{id}")
    suspend fun obtenerPersonaPorId(@Path("id") id: Long): Response<PersonaDTO>

    // Obtener persona por RUT
    @GET("personas/rut/{rut}")
    suspend fun obtenerPersonaPorRut(@Path("rut") rut: String): Response<PersonaDTO>

    // Obtener persona por username
    @GET("personas/username/{username}")
    suspend fun obtenerPersonaPorUsername(@Path("username") username: String): Response<PersonaDTO>

    // Buscar personas por nombre (búsqueda parcial)
    @GET("personas/buscar")
    suspend fun buscarPersonasPorNombre(@Query("nombre") nombre: String): Response<List<PersonaDTO>>

    // Obtener personas por estado (activo/inactivo)
    @GET("personas/estado/{estado}")
    suspend fun obtenerPersonasPorEstado(@Path("estado") estado: String): Response<List<PersonaDTO>>

    // Crear nueva persona (registro público y admin - ahora ambos usan el mismo endpoint)
    @POST("personas/crear")
    suspend fun crearPersona(@Body personaDTO: PersonaDTO): Response<PersonaDTO>

    // Crear persona desde admin (ahora usa el mismo endpoint que el registro público)
    @POST("personas/crear")
    suspend fun crearPersonaAdmin(@Body personaDTO: PersonaDTO): Response<PersonaDTO>

    // Actualizar persona
    @PUT("personas/{id}")
    suspend fun actualizarPersona(
        @Path("id") id: Long,
        @Body personaDTO: PersonaDTO
    ): Response<PersonaDTO>

    // Desactivar persona (borrado lógico - cambia estado a inactivo)
    @DELETE("personas/{id}")
    suspend fun eliminarPersona(@Path("id") id: Long): Response<Void>

    // Verificar credenciales para autenticación
    @POST("personas/verificar-credenciales")
    suspend fun verificarCredenciales(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<PersonaDTO>

}
