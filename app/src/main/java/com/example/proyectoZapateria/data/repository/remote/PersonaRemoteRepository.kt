package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonaRemoteRepository @Inject constructor(
    private val personaApi: PersonaApiService
) {
    // Obtener todas las personas
    suspend fun obtenerTodasLasPersonas(): Result<List<PersonaDTO>> {
        return try {
            val personas = try {
                personaApi.obtenerTodasLasPersonas()
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(personas)
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener personas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por ID
    suspend fun obtenerPersonaPorId(id: Long): Result<PersonaDTO?> {
        return try {
            val res = NetworkUtils.safeApiCall { personaApi.obtenerPersonaPorId(id) }
            res
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por RUT
    suspend fun obtenerPersonaPorRut(rut: String): Result<PersonaDTO?> {
        return try {
            NetworkUtils.safeApiCall { personaApi.obtenerPersonaPorRut(rut) }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por RUT: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por username
    suspend fun obtenerPersonaPorUsername(username: String): Result<PersonaDTO?> {
        return try {
            NetworkUtils.safeApiCall { personaApi.obtenerPersonaPorUsername(username) }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por username: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Buscar personas por nombre
    suspend fun buscarPersonasPorNombre(nombre: String): Result<List<PersonaDTO>> {
        return try {
            val personas = try {
                personaApi.buscarPersonasPorNombre(nombre)
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(personas)
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al buscar personas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener personas por estado
    suspend fun obtenerPersonasPorEstado(estado: String): Result<List<PersonaDTO>> {
        return try {
            val personas = try {
                personaApi.obtenerPersonasPorEstado(estado)
            } catch (e: Exception) {
                return when (e) {
                    is java.net.UnknownHostException -> Result.failure(Exception("Sin conexión: ${e.message}"))
                    is java.net.SocketTimeoutException -> Result.failure(Exception("Timeout de conexión"))
                    is java.io.IOException -> Result.failure(Exception("Error de red: ${e.message}"))
                    else -> Result.failure(e)
                }
            }
            Result.success(personas)
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener personas por estado: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear nueva persona
    suspend fun crearPersona(personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            NetworkUtils.safeApiCall { personaApi.crearPersona(personaDTO) }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al crear persona: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar persona
    suspend fun actualizarPersona(id: Long, personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            NetworkUtils.safeApiCall { personaApi.actualizarPersona(id, personaDTO) }
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
            NetworkUtils.safeApiCall { personaApi.verificarCredenciales(username, password) }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al verificar credenciales: ${e.message}", e)
            Result.failure(e)
        }
    }
}