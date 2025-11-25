package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.TransportistaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportistaRemoteRepository @Inject constructor(
    private val api: TransportistaApiService
) {
    companion object {
        private const val TAG = "TransportistaRemoteRepo"
    }

    // Recupera todos los transportistas desde el microservicio
    suspend fun obtenerTodos(): Result<List<TransportistaDTO>> {
        try {
            val resp: Response<List<TransportistaDTO>> = api.obtenerTodos()
            if (resp.isSuccessful) {
                val bodyList: List<TransportistaDTO> = resp.body() ?: emptyList()
                return Result.success(bodyList)
            } else {
                val err = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                val msg = "Error ${resp.code()}: ${resp.message()} - body=${err ?: "<empty>"}"
                Log.e(TAG, msg)
                return Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "obtenerTodos exception", e)
            return Result.failure(e)
        }
    }

    // Recupera un transportista por su ID
    suspend fun obtenerPorId(id: Long): kotlin.Result<TransportistaDTO> = try {
        val resp = api.obtenerPorId(id)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) Result.success(body) else kotlin.Result.failure(Exception("Empty body"))
        } else {
            Result.failure(Exception("Error ${resp.code()}: ${resp.message()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerPorId exception", e)
        Result.failure(e)
    }

    // Recupera un transportista por el ID de la persona asociada
    suspend fun obtenerPorPersona(personaId: Long): kotlin.Result<TransportistaDTO> = try {
        val resp = api.obtenerPorPersona(personaId)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) Result.success(body) else Result.failure(Exception("Empty body"))
        } else {
            Result.failure(Exception("Error ${resp.code()}: ${resp.message()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerPorPersona exception", e)
        Result.failure(e)
    }

    // Crea un nuevo transportista en el microservicio
    suspend fun crear(dto: TransportistaDTO): kotlin.Result<TransportistaDTO> = try {
        val resp = api.crear(dto)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) Result.success(body) else Result.failure(Exception("Empty body"))
        } else {
            Result.failure(Exception("Error ${resp.code()}: ${resp.message()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "crear exception", e)
        Result.failure(e)
    }

    // Actualiza un transportista existente por ID
    suspend fun actualizar(id: Long, dto: TransportistaDTO): kotlin.Result<TransportistaDTO> = try {
        val resp = api.actualizar(id, dto)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) Result.success(body) else Result.failure(Exception("Empty body"))
        } else {
            Result.failure(Exception("Error ${resp.code()}: ${resp.message()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "actualizar exception", e)
        Result.failure(e)
    }

    // Elimina un transportista por ID (retorna true si la operación fue exitosa)
    suspend fun eliminar(id: Long): Result<Boolean> = try {
        val resp = api.eliminar(id)
        if (resp.isSuccessful) Result.success(true) else Result.failure(Exception("Error ${resp.code()}: ${resp.message()}"))
    } catch (e: Exception) {
        Log.e(TAG, "eliminar exception", e)
        Result.failure(e)
    }
}
