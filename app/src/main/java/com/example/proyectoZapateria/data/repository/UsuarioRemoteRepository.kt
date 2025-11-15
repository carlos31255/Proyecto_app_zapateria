package com.example.proyectoZapateria.data.repository

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRemoteRepository @Inject constructor(
    private val usuarioApi: UsuarioApiService
) {
    // Obtener todos los usuarios
    suspend fun obtenerTodosLosUsuarios(): Result<List<UsuarioDTO>> {
        return try {
            val usuarios = usuarioApi.obtenerTodosLosUsuarios()
            Result.success(usuarios)
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuarios: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener usuario por ID de persona
    suspend fun obtenerUsuarioPorId(idPersona: Int): Result<UsuarioDTO?> {
        return try {
            val response = usuarioApi.obtenerUsuarioPorId(idPersona)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuario por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener usuarios por rol
    suspend fun obtenerUsuariosPorRol(idRol: Int): Result<List<UsuarioDTO>> {
        return try {
            val usuarios = usuarioApi.obtenerUsuariosPorRol(idRol)
            Result.success(usuarios)
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al obtener usuarios por rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nuevo usuario
    suspend fun crearUsuario(usuarioDTO: UsuarioDTO): Result<UsuarioDTO?> {
        return try {
            val response = usuarioApi.crearUsuario(usuarioDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al crear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar rol de usuario
    suspend fun actualizarRolUsuario(idPersona: Int, nuevoIdRol: Int): Result<UsuarioDTO?> {
        return try {
            val response = usuarioApi.actualizarRolUsuario(idPersona, nuevoIdRol)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al actualizar rol: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Desactivar usuario
    suspend fun eliminarUsuario(idPersona: Int): Result<Boolean> {
        return try {
            val response = usuarioApi.eliminarUsuario(idPersona)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("UsuarioRemoteRepo", "Error al eliminar usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
}

