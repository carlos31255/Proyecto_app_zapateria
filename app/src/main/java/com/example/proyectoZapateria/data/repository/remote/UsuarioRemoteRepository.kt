package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRemoteRepository @Inject constructor(
    private val usuarioApi: UsuarioApiService
) {
    // Obtener todos los usuarios
    suspend fun obtenerTodosLosUsuarios(): Result<List<UsuarioDTO>> {
        return try {
            val usuarios = try {
                usuarioApi.obtenerTodosLosUsuarios()
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(usuarios)
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuarios: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener usuario por ID de persona
    suspend fun obtenerUsuarioPorId(idPersona: Long): Result<UsuarioDTO?> {
        return try {
            NetworkUtils.safeApiCall { usuarioApi.obtenerUsuarioPorId(idPersona) }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuario por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener usuarios por rol
    suspend fun obtenerUsuariosPorRol(idRol: Long): Result<List<UsuarioDTO>> {
        return try {
            val usuarios = try {
                usuarioApi.obtenerUsuariosPorRol(idRol)
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(usuarios)
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuarios por rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nuevo usuario
    suspend fun crearUsuario(usuarioDTO: UsuarioDTO): Result<UsuarioDTO?> {
        return try {
            NetworkUtils.safeApiCall { usuarioApi.crearUsuario(usuarioDTO) }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al crear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar rol de usuario
    suspend fun actualizarRolUsuario(idPersona: Long, nuevoIdRol: Long): Result<UsuarioDTO?> {
        return try {
            NetworkUtils.safeApiCall { usuarioApi.actualizarRolUsuario(idPersona, nuevoIdRol) }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al actualizar rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Desactivar usuario
    suspend fun eliminarUsuario(idPersona: Long): Result<Boolean> {
        return try {
            val response = usuarioApi.eliminarUsuario(idPersona)
            if (response.isSuccessful) Result.success(true) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al eliminar usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
}