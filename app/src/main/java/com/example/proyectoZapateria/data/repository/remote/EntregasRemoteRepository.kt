package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntregasRemoteRepository @Inject constructor(
    private val api: EntregasApiService
) {

    companion object {
        private const val TAG = "EntregasRemoteRepository"
    }

    suspend fun obtenerTodasLasEntregas(): Result<List<EntregaDTO>> = try {
        val response = api.obtenerTodasLasEntregas()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerTodasLasEntregas: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerTodasLasEntregas exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregaPorId(id: Int): Result<EntregaDTO> = try {
        val response = api.obtenerEntregaPorId(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerEntregaPorId: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregaPorId exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorTransportista(transportistaId: Int): Result<List<EntregaDTO>> = try {
        Log.d(TAG, "obtenerEntregasPorTransportista: transportistaId=$transportistaId")
        val response = api.obtenerEntregasPorTransportista(transportistaId)
        if (response.isSuccessful && response.body() != null) {
            val entregas = response.body()!!
            Log.d(TAG, "obtenerEntregasPorTransportista: encontradas ${entregas.size} entregas")
            Result.success(entregas)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerEntregasPorTransportista: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregasPorTransportista exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorEstado(estado: String): Result<List<EntregaDTO>> = try {
        Log.d(TAG, "obtenerEntregasPorEstado: estado=$estado")
        val response = api.obtenerEntregasPorEstado(estado)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerEntregasPorEstado: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregasPorEstado exception", e)
        Result.failure(e)
    }

    suspend fun asignarTransportista(entregaId: Int, transportistaId: Int): Result<EntregaDTO> = try {
        Log.d(TAG, "asignarTransportista: entregaId=$entregaId, transportistaId=$transportistaId")
        val response = api.asignarTransportista(entregaId, transportistaId)
        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "asignarTransportista: transportista asignado exitosamente")
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "asignarTransportista: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "asignarTransportista exception", e)
        Result.failure(e)
    }

    suspend fun completarEntrega(entregaId: Int, observacion: String?): Result<EntregaDTO> = try {
        Log.d(TAG, "completarEntrega: entregaId=$entregaId, observacion=$observacion")
        val response = api.completarEntrega(entregaId, observacion)
        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "completarEntrega: entrega completada exitosamente")
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "completarEntrega: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "completarEntrega exception", e)
        Result.failure(e)
    }

    suspend fun cambiarEstadoEntrega(entregaId: Int, nuevoEstado: String): Result<EntregaDTO> = try {
        Log.d(TAG, "cambiarEstadoEntrega: entregaId=$entregaId, nuevoEstado=$nuevoEstado")
        val response = api.cambiarEstadoEntrega(entregaId, nuevoEstado)
        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "cambiarEstadoEntrega: estado actualizado exitosamente")
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "cambiarEstadoEntrega: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "cambiarEstadoEntrega exception", e)
        Result.failure(e)
    }

    suspend fun contarEntregasPendientes(transportistaId: Int): Result<Int> = try {
        val entregasResult = obtenerEntregasPorTransportista(transportistaId)
        if (entregasResult.isSuccess) {
            val entregas = entregasResult.getOrNull() ?: emptyList()
            val pendientes = entregas.count { it.estadoEntrega == "pendiente" || it.estadoEntrega == "en_proceso" }
            Result.success(pendientes)
        } else {
            entregasResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "contarEntregasPendientes exception", e)
        Result.failure(e)
    }

    suspend fun contarEntregasCompletadas(transportistaId: Int): Result<Int> = try {
        val entregasResult = obtenerEntregasPorTransportista(transportistaId)
        if (entregasResult.isSuccess) {
            val entregas = entregasResult.getOrNull() ?: emptyList()
            val completadas = entregas.count { it.estadoEntrega == "completada" }
            Result.success(completadas)
        } else {
            entregasResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "contarEntregasCompletadas exception", e)
        Result.failure(e)
    }
}

