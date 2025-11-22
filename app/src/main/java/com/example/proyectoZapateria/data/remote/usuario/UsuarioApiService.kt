package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import retrofit2.Response
import retrofit2.http.*

interface UsuarioApiService {

    // ========== ENDPOINTS DE USUARIOS ==========

    // Obtener todos los usuarios
    @GET("api/usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioDTO>

    // Obtener usuario por ID de persona
    @GET("api/usuarios/{idPersona}")
    suspend fun obtenerUsuarioPorId(@Path("idPersona") idPersona: Long): Response<UsuarioDTO>

    // Obtener usuarios por rol
    @GET("api/usuarios/rol/{idRol}")
    suspend fun obtenerUsuariosPorRol(@Path("idRol") idRol: Long): List<UsuarioDTO>

    // Crear nuevo usuario
    @POST("api/usuarios")
    suspend fun crearUsuario(@Body usuarioDTO: UsuarioDTO): Response<UsuarioDTO>

    // Actualizar rol de usuario
    @PUT("api/usuarios/{idPersona}/rol")
    suspend fun actualizarRolUsuario(
        @Path("idPersona") idPersona: Long,
        @Query("nuevoIdRol") nuevoIdRol: Long
    ): Response<UsuarioDTO>

    // Desactivar usuario (borrado lógico - marca como inactivo)
    @DELETE("api/usuarios/{idPersona}")
    suspend fun eliminarUsuario(@Path("idPersona") idPersona: Long): Response<Void>


}
