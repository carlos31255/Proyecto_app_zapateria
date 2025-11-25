package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonaRemoteRepository @Inject constructor(
    private val personaApi: PersonaApiService
) {
    // Obtener todas las personas
    suspend fun obtenerTodasLasPersonas(): Result<List<PersonaDTO>> {
        return try {
            val response = personaApi.obtenerTodasLasPersonas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                val msg = "Error ${response.code()}: ${response.message()} - body=${err ?: "<empty>"}"
                Log.e("PersonaRemoteRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener personas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por ID
    suspend fun obtenerPersonaPorId(id: Long): Result<PersonaDTO?> {
        return try {
            val response = personaApi.obtenerPersonaPorId(id)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por RUT
    suspend fun obtenerPersonaPorRut(rut: String): Result<PersonaDTO?> {
        return try {
            val response = personaApi.obtenerPersonaPorRut(rut)
            if (response.isSuccessful) Result.success(response.body()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por RUT: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por username
    suspend fun obtenerPersonaPorUsername(username: String): Result<PersonaDTO?> {
        return try {
            val response = personaApi.obtenerPersonaPorUsername(username)
            if (response.isSuccessful) Result.success(response.body()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por username: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Buscar personas por nombre
    suspend fun buscarPersonasPorNombre(nombre: String): Result<List<PersonaDTO>> {
        return try {
            val response = personaApi.buscarPersonasPorNombre(nombre)
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al buscar personas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener personas por estado
    suspend fun obtenerPersonasPorEstado(estado: String): Result<List<PersonaDTO>> {
        return try {
            val response = personaApi.obtenerPersonasPorEstado(estado)
            if (response.isSuccessful) Result.success(response.body() ?: emptyList()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener personas por estado: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nueva persona
    suspend fun crearPersona(personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            val response = personaApi.crearPersona(personaDTO)
            if (response.isSuccessful) Result.success(response.body()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al crear persona: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar persona
    suspend fun actualizarPersona(id: Long, personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            val response = personaApi.actualizarPersona(id, personaDTO)
            if (response.isSuccessful) Result.success(response.body()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al actualizar persona: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Desactivar persona
    suspend fun eliminarPersona(id: Long): Result<Boolean> {
        return try {
            val response = personaApi.eliminarPersona(id)
            if (response.isSuccessful) Result.success(true) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al eliminar persona: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Verificar credenciales
    suspend fun verificarCredenciales(username: String, password: String): Result<PersonaDTO?> {
        return try {
            val response = personaApi.verificarCredenciales(username, password)
            if (response.isSuccessful) Result.success(response.body()) else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al verificar credenciales: ${e.message}", e)
            Result.failure(e)
        }
    }
}