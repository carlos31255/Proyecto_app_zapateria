package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import retrofit2.Response
import retrofit2.http.*

interface RolApiService {
    // ========== ENDPOINTS DE ROLES ==========
    // Obtener todos los roles
    @GET("roles")
    suspend fun obtenerTodosLosRoles(): Response<List<RolDTO>>

    // Obtener rol por ID
    @GET("roles/{id}")
    suspend fun obtenerRolPorId(@Path("id") id: Long): Response<RolDTO>

    // Obtener rol por nombre
    @GET("roles/nombre/{nombreRol}")
    suspend fun obtenerRolPorNombre(@Path("nombreRol") nombreRol: String): Response<RolDTO>

    // Crear nuevo rol
    @POST("roles")
    suspend fun crearRol(@Body rolDTO: RolDTO): Response<RolDTO>

    // Actualizar rol
    @PUT("roles/{id}")
    suspend fun actualizarRol(
        @Path("id") id: Long,
        @Body rolDTO: RolDTO
    ): Response<RolDTO>

    // Eliminar rol
    @DELETE("roles/{id}")
    suspend fun eliminarRol(@Path("id") id: Long): Response<Void>

}
