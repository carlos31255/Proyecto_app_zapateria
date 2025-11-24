package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RolRemoteRepository @Inject constructor(
    private val rolApi: RolApiService
) {
    // Obtener todos los roles
    suspend fun obtenerTodosLosRoles(): Result<List<RolDTO>> {
        return try {
            val roles = try {
                rolApi.obtenerTodosLosRoles()
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(roles)
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener roles: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener rol por ID
    suspend fun obtenerRolPorId(id: Long): Result<RolDTO?> {
        return try {
            NetworkUtils.safeApiCall { rolApi.obtenerRolPorId(id) }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener rol por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener rol por nombre
    suspend fun obtenerRolPorNombre(nombreRol: String): Result<RolDTO?> {
        return try {
            NetworkUtils.safeApiCall { rolApi.obtenerRolPorNombre(nombreRol) }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener rol por nombre: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nuevo rol
    suspend fun crearRol(rolDTO: RolDTO): Result<RolDTO?> {
        return try {
            NetworkUtils.safeApiCall { rolApi.crearRol(rolDTO) }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al crear rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar rol
    suspend fun actualizarRol(id: Long, rolDTO: RolDTO): Result<RolDTO?> {
        return try {
            NetworkUtils.safeApiCall { rolApi.actualizarRol(id, rolDTO) }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al actualizar rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Eliminar rol
    suspend fun eliminarRol(id: Long): Result<Boolean> {
        return try {
            val response = rolApi.eliminarRol(id)
            if (response.isSuccessful) Result.success(true) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al eliminar rol: ${e.message}", e)
            Result.failure(e)
        }
    }
}