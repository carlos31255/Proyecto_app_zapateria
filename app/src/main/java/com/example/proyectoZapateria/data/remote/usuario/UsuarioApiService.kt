package com.example.proyectoZapateria.data.remote.usuario

import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import retrofit2.Response
import retrofit2.http.*

interface UsuarioApiService {

    // ========== ENDPOINTS DE USUARIOS ==========

    // Obtener todos los usuarios
    @GET("usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioDTO>

    // Obtener usuario por ID de persona
    @GET("usuarios/{idPersona}")
    suspend fun obtenerUsuarioPorId(@Path("idPersona") idPersona: Long): Response<UsuarioDTO>

    // Obtener usuarios por rol
    @GET("usuarios/rol/{idRol}")
    suspend fun obtenerUsuariosPorRol(@Path("idRol") idRol: Long): List<UsuarioDTO>

    // Crear nuevo usuario
    @POST("usuarios")
    suspend fun crearUsuario(@Body usuarioDTO: UsuarioDTO): Response<UsuarioDTO>

    // Actualizar rol de usuario
    @PUT("usuarios/{idPersona}/rol")
    suspend fun actualizarRolUsuario(
        @Path("idPersona") idPersona: Long,
        @Query("nuevoIdRol") nuevoIdRol: Long
    ): Response<UsuarioDTO>

    // Desactivar usuario (borrado lógico - marca como inactivo)
    @DELETE("usuarios/{idPersona}")
    suspend fun eliminarUsuario(@Path("idPersona") idPersona: Long): Response<Void>

    // Subir foto de perfil
    @Multipart
    @PUT("usuarios/{idPersona}/foto")
    suspend fun actualizarFotoUsuario(
        @Path("idPersona") idPersona: Long,
        @Part foto: okhttp3.MultipartBody.Part
    ): Response<UsuarioDTO>

    // Obtener foto de perfil (usar ResponseBody para datos binarios)
    @GET("usuarios/{idPersona}/foto-blob")
    @Streaming
    suspend fun obtenerFotoUsuario(@Path("idPersona") idPersona: Long): Response<okhttp3.ResponseBody>
}
