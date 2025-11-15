package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VentasRemoteRepository @Inject constructor(
    private val api: VentasApiService
) {

    companion object {
        private const val TAG = "VentasRemoteRepository"
    }

    // ==================== BOLETAS ====================

    suspend fun obtenerTodasLasBoletas(): Result<List<BoletaDTO>> = try {
        val response = api.obtenerTodasLasBoletas()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerTodasLasBoletas: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerTodasLasBoletas exception", e)
        Result.failure(e)
    }

    suspend fun obtenerBoletaPorId(id: Int): Result<BoletaDTO> = try {
        val response = api.obtenerBoletaPorId(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerBoletaPorId: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerBoletaPorId exception", e)
        Result.failure(e)
    }

    suspend fun obtenerBoletasPorCliente(clienteId: Int): Result<List<BoletaDTO>> = try {
        Log.d(TAG, "obtenerBoletasPorCliente: clienteId=$clienteId")
        val response = api.obtenerBoletasPorCliente(clienteId)
        if (response.isSuccessful && response.body() != null) {
            val boletas = response.body()!!
            Log.d(TAG, "obtenerBoletasPorCliente: encontradas ${boletas.size} boletas")
            Result.success(boletas)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "obtenerBoletasPorCliente: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "obtenerBoletasPorCliente exception", e)
        Result.failure(e)
    }

    suspend fun crearBoleta(request: CrearBoletaRequest): Result<BoletaDTO> = try {
        Log.d(TAG, "crearBoleta: clienteId=${request.clienteId}, total detalles=${request.detalles.size}")
        val response = api.crearBoleta(request)
        if (response.isSuccessful && response.body() != null) {
            val boleta = response.body()!!
            Log.d(TAG, "crearBoleta: boleta creada con ID=${boleta.id}")
            Result.success(boleta)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "crearBoleta: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "crearBoleta exception", e)
        Result.failure(e)
    }

    suspend fun cambiarEstadoBoleta(id: Int, nuevoEstado: String): Result<BoletaDTO> = try {
        Log.d(TAG, "cambiarEstadoBoleta: id=$id, nuevoEstado=$nuevoEstado")
        val request = CambiarEstadoRequest(nuevoEstado)
        val response = api.cambiarEstadoBoleta(id, request)
        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "cambiarEstadoBoleta: estado actualizado exitosamente")
            Result.success(response.body()!!)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "cambiarEstadoBoleta: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "cambiarEstadoBoleta exception", e)
        Result.failure(e)
    }

    suspend fun eliminarBoleta(id: Int): Result<Unit> = try {
        Log.d(TAG, "eliminarBoleta: id=$id")
        val response = api.eliminarBoleta(id)
        if (response.isSuccessful) {
            Log.d(TAG, "eliminarBoleta: boleta eliminada exitosamente")
            Result.success(Unit)
        } else {
            val error = "Error ${response.code()}: ${response.message()}"
            Log.e(TAG, "eliminarBoleta: $error")
            Result.failure(Exception(error))
        }
    } catch (e: Exception) {
        Log.e(TAG, "eliminarBoleta exception", e)
        Result.failure(e)
    }

    // ==================== MÉTODOS AUXILIARES ====================

    suspend fun obtenerDetallesDeBoleta(boletaId: Int): Result<List<DetalleBoletaDTO>> = try {
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
        Log.e(TAG, "obtenerDetallesDeBoleta exception", e)
        Result.failure(e)
    }

    suspend fun clienteTieneBoletasPendientes(clienteId: Int): Result<Boolean> = try {
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
        Log.e(TAG, "clienteTieneBoletasPendientes exception", e)
        Result.failure(e)
    }

    suspend fun calcularTotalComprasCliente(clienteId: Int): Result<Int> = try {
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
        Log.e(TAG, "calcularTotalComprasCliente exception", e)
        Result.failure(e)
    }

    suspend fun contarBoletasPorEstado(clienteId: Int, estado: String): Result<Int> = try {
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
        Log.e(TAG, "contarBoletasPorEstado exception", e)
        Result.failure(e)
    }
}

