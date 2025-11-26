package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import com.example.proyectoZapateria.utils.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VentasRemoteRepository @Inject constructor(
    private val api: VentasApiService
) {

    // ==================== BOLETAS ====================

    suspend fun obtenerTodasLasBoletas(): Result<List<BoletaDTO>> = try {
        NetworkUtils.safeApiCall { api.obtenerTodasLasBoletas() }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerBoletaPorId(id: Long): Result<BoletaDTO> = try {
        NetworkUtils.safeApiCall { api.obtenerBoletaPorId(id) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerBoletasPorCliente(clienteId: Long): Result<List<BoletaDTO>> {
        return try {
            val res = NetworkUtils.safeApiCall { api.obtenerBoletasPorCliente(clienteId) }
            if (res.isSuccess) {
                val boletas = res.getOrNull() ?: emptyList()
                return Result.success(boletas)
            }
            val res2 = NetworkUtils.safeApiCall { api.obtenerBoletasPorClienteQuery(clienteId) }
            if (res2.isSuccess) return Result.success(res2.getOrNull() ?: emptyList())
            return res2
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearBoleta(request: CrearBoletaRequest): Result<BoletaDTO> = try {
        NetworkUtils.safeApiCall { api.crearBoleta(request) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cambiarEstadoBoleta(id: Long, nuevoEstado: String): Result<BoletaDTO> = try {
        NetworkUtils.safeApiCall { api.cambiarEstadoBoleta(id, CambiarEstadoRequest(nuevoEstado)) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun eliminarBoleta(id: Long): Result<Unit> = try {
        val res = NetworkUtils.safeApiCall { api.eliminarBoleta(id) }
        if (res.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Error eliminando boleta"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== MÉTODOS AUXILIARES ====================

    suspend fun obtenerDetallesDeBoleta(boletaId: Long): Result<List<DetalleBoletaDTO>> = try {
        val boletaResult = obtenerBoletaPorId(boletaId)
        if (boletaResult.isSuccess) {
            val boleta = boletaResult.getOrNull()
            val detalles = boleta?.detalles ?: emptyList()
            Result.success(detalles)
        } else {
            boletaResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido al obtener boleta"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun clienteTieneBoletasPendientes(clienteId: Long): Result<Boolean> = try {
        val boletasResult = obtenerBoletasPorCliente(clienteId)
        if (boletasResult.isSuccess) {
            val boletas = boletasResult.getOrNull() ?: emptyList()
            val tienePendientes = boletas.any { it.estado == "PENDIENTE" || it.estado == "EN_PROCESO" }
            Result.success(tienePendientes)
        } else {
            boletasResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun calcularTotalComprasCliente(clienteId: Long): Result<Int> = try {
        val boletasResult = obtenerBoletasPorCliente(clienteId)
        if (boletasResult.isSuccess) {
            val boletas = boletasResult.getOrNull() ?: emptyList()
            val total = boletas
                .filter { it.estado == "COMPLETADA" }
                .sumOf { it.total }
            Result.success(total)
        } else {
            boletasResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun contarBoletasPorEstado(clienteId: Long, estado: String): Result<Int> = try {
        val boletasResult = obtenerBoletasPorCliente(clienteId)
        if (boletasResult.isSuccess) {
            val boletas = boletasResult.getOrNull() ?: emptyList()
            val count = boletas.count { it.estado == estado }
            Result.success(count)
        } else {
            boletasResult.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
