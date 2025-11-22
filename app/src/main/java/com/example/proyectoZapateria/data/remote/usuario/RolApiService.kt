package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import retrofit2.Response
import retrofit2.http.*

interface RolApiService {
    // ========== ENDPOINTS DE ROLES ==========
    // Obtener todos los roles
    @GET("api/roles")
    suspend fun obtenerTodosLosRoles(): List<RolDTO>

    // Obtener rol por ID
    @GET("api/roles/{id}")
    suspend fun obtenerRolPorId(@Path("id") id: Long): Response<RolDTO>

    // Obtener rol por nombre
    @GET("api/roles/nombre/{nombreRol}")
    suspend fun obtenerRolPorNombre(@Path("nombreRol") nombreRol: String): Response<RolDTO>

    // Crear nuevo rol
    @POST("api/roles")
    suspend fun crearRol(@Body rolDTO: RolDTO): Response<RolDTO>

    // Actualizar rol
    @PUT("api/roles/{id}")
    suspend fun actualizarRol(
        @Path("id") id: Long,
        @Body rolDTO: RolDTO
    ): Response<RolDTO>

    // Eliminar rol
    @DELETE("api/roles/{id}")
    suspend fun eliminarRol(@Path("id") id: Long): Response<Void>

}
