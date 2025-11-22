package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RolRemoteRepository @Inject constructor(
    private val rolApi: RolApiService
) {
    // Obtener todos los roles
    suspend fun obtenerTodosLosRoles(): Result<List<RolDTO>> {
        return try {
            val roles = rolApi.obtenerTodosLosRoles()
            Result.success(roles)
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener roles: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener rol por ID
    suspend fun obtenerRolPorId(id: Long): Result<RolDTO?> {
        return try {
            val response = rolApi.obtenerRolPorId(id)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener rol por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener rol por nombre
    suspend fun obtenerRolPorNombre(nombreRol: String): Result<RolDTO?> {
        return try {
            val response = rolApi.obtenerRolPorNombre(nombreRol)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al obtener rol por nombre: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nuevo rol
    suspend fun crearRol(rolDTO: RolDTO): Result<RolDTO?> {
        return try {
            val response = rolApi.crearRol(rolDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al crear rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar rol
    suspend fun actualizarRol(id: Long, rolDTO: RolDTO): Result<RolDTO?> {
        return try {
            val response = rolApi.actualizarRol(id, rolDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al actualizar rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Eliminar rol
    suspend fun eliminarRol(id: Long): Result<Boolean> {
        return try {
            val response = rolApi.eliminarRol(id)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("RolRemoteRepo", "Error al eliminar rol: ${e.message}", e)
            Result.failure(e)
        }
    }
}