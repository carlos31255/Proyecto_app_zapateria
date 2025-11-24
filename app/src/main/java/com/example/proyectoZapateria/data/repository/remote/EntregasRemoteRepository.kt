package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        NetworkUtils.safeApiCall { api.obtenerTodasLasEntregas() }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerTodasLasEntregas exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregaPorId(id: Long): Result<EntregaDTO> = try {
        NetworkUtils.safeApiCall { api.obtenerEntregaPorId(id) }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregaPorId exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorTransportista(transportistaId: Long): Result<List<EntregaDTO>> = try {
        Log.d(TAG, "obtenerEntregasPorTransportista: transportistaId=$transportistaId")
        NetworkUtils.safeApiCall { api.obtenerEntregasPorTransportista(transportistaId) }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregasPorTransportista exception", e)
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorEstado(estado: String): Result<List<EntregaDTO>> = try {
        Log.d(TAG, "obtenerEntregasPorEstado: estado=$estado")
        NetworkUtils.safeApiCall { api.obtenerEntregasPorEstado(estado) }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerEntregasPorEstado exception", e)
        Result.failure(e)
    }

    suspend fun asignarTransportista(entregaId: Long, transportistaId: Long): Result<EntregaDTO> = try {
        Log.d(TAG, "asignarTransportista: entregaId=$entregaId, transportistaId=$transportistaId")
        NetworkUtils.safeApiCall { api.asignarTransportista(entregaId, transportistaId) }
    } catch (e: Exception) {
        Log.e(TAG, "asignarTransportista exception", e)
        Result.failure(e)
    }

    suspend fun completarEntrega(entregaId: Long, observacion: String?): Result<EntregaDTO> = try {
        Log.d(TAG, "completarEntrega: entregaId=$entregaId, observacion=$observacion")
        NetworkUtils.safeApiCall { api.completarEntrega(entregaId, observacion) }
    } catch (e: Exception) {
        Log.e(TAG, "completarEntrega exception", e)
        Result.failure(e)
    }

    suspend fun cambiarEstadoEntrega(entregaId: Long, nuevoEstado: String): Result<EntregaDTO> = try {
        Log.d(TAG, "cambiarEstadoEntrega: entregaId=$entregaId, nuevoEstado=$nuevoEstado")
        NetworkUtils.safeApiCall { api.cambiarEstadoEntrega(entregaId, nuevoEstado) }
    } catch (e: Exception) {
        Log.e(TAG, "cambiarEstadoEntrega exception", e)
        Result.failure(e)
    }

    suspend fun contarEntregasPendientes(transportistaId: Long): Result<Int> = try {
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

    suspend fun contarEntregasCompletadas(transportistaId: Long): Result<Int> = try {
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

    fun getEntregasPorTransportistaFlow(transportistaId: Long): Flow<List<EntregaDTO>> = flow {
        try {
            val res = NetworkUtils.safeApiCall { api.obtenerEntregasPorTransportista(transportistaId) }
            if (res.isSuccess) emit(res.getOrNull() ?: emptyList()) else emit(emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
