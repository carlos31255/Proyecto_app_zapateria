package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonaRemoteRepository @Inject constructor(
    private val personaApi: PersonaApiService
) {
    /**
     * Función auxiliar para extraer el mensaje de error del errorBody JSON
     * El backend devuelve errores en formato: {"error": "mensaje descriptivo"}
     */
    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val jsonError = JSONObject(errorBody)
                jsonError.optString("error", response.message())
            } else {
                response.message()
            }
        } catch (e: Exception) {
            response.message()
        }
    }
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
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al obtener persona por ID: ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
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
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al obtener persona por RUT: ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al obtener persona por RUT: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtener persona por username
    suspend fun obtenerPersonaPorUsername(username: String): Result<PersonaDTO?> {
        return try {
            val response = personaApi.obtenerPersonaPorUsername(username)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al obtener persona por username: ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
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

    // Crear nueva persona (registro público - RUT opcional)
    suspend fun crearPersona(personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            val response = personaApi.crearPersona(personaDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al crear persona: ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al crear persona: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Crear persona desde admin (requiere RUT - para trabajadores)
    suspend fun crearPersonaAdmin(personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            val response = personaApi.crearPersonaAdmin(personaDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al crear persona (admin): ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al crear persona (admin): ${e.message}", e)
            Result.failure(e)
        }
    }

    // Actualizar persona
    suspend fun actualizarPersona(id: Long, personaDTO: PersonaDTO): Result<PersonaDTO?> {
        return try {
            val response = personaApi.actualizarPersona(id, personaDTO)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorMessage = parseErrorMessage(response)
                Log.e("PersonaRemoteRepo", "Error al actualizar persona: ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
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
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                // Manejo de errores específicos con mensajes amigables
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales inválidas. Verifique su email y contraseña"
                    404 -> "Usuario no encontrado"
                    500 -> "Error en el servidor. Intente más tarde"
                    else -> "Error al iniciar sesión. Código: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("PersonaRemoteRepo", "Error al verificar credenciales: ${e.message}", e)
            Result.failure(e)
        }
    }
}