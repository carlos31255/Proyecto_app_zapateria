package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.entregas.dto.ActualizarEstadoRequest
import com.example.proyectoZapateria.data.remote.entregas.dto.CompletarEntregaRequest
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntregasRemoteRepository @Inject constructor(
    private val api: EntregasApiService
) {


    // SharedFlow que emite cuando hay cambios en entregas (para que ViewModels se refresquen)
    private val _updates = MutableSharedFlow<Unit>(replay = 0)
    val updatesFlow = _updates.asSharedFlow()

    suspend fun obtenerTodasLasEntregas(): Result<List<EntregaDTO>> = try {
        NetworkUtils.safeApiCall { api.obtenerTodasLasEntregas() }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerEntregaPorId(id: Long): Result<EntregaDTO> = try {
        NetworkUtils.safeApiCall { api.obtenerEntregaPorId(id) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorTransportista(transportistaId: Long): Result<List<EntregaDTO>> = try {
        NetworkUtils.safeApiCall { api.obtenerEntregasPorTransportista(transportistaId) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerEntregasPorEstado(estado: String): Result<List<EntregaDTO>> = try {
        NetworkUtils.safeApiCall { api.obtenerEntregasPorEstado(estado) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun asignarTransportista(entregaId: Long, transportistaId: Long): Result<EntregaDTO> = try {
        val res = NetworkUtils.safeApiCall { api.asignarTransportista(entregaId, transportistaId) }
        if (res.isSuccess) {
            try { _updates.emit(Unit) } catch (_: Throwable) {}
        }
        res
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun completarEntrega(entregaId: Long, observacion: String?): Result<EntregaDTO> = try {
        val req = CompletarEntregaRequest(observacion ?: "")
        val res = NetworkUtils.safeApiCall { api.completarEntrega(entregaId, req) }
        if (res.isSuccess) {
            try { _updates.emit(Unit) } catch (_: Throwable) {}
        }
        res
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cambiarEstadoEntrega(entregaId: Long, nuevoEstado: String, observacion: String? = null): Result<EntregaDTO> = try {
        val req = ActualizarEstadoRequest(estadoEntrega = nuevoEstado, observacion = observacion)
        val res = NetworkUtils.safeApiCall { api.cambiarEstadoEntrega(entregaId, req) }
        if (res.isSuccess) {
            try { _updates.emit(Unit) } catch (_: Throwable) {}
        }
        res
    } catch (e: Exception) {
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
