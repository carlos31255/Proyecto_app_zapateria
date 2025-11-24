package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ProductoRemoteRepository @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) {

    // Obtener todos los productos (remoto) y emitir los DTOs tal como vienen del servicio
    fun getAllModelos(): Flow<List<ProductoDTO>> = flow {
        val res = inventarioRemoteRepository.getModelos()
        val list = if (res.isSuccess) {
            res.getOrNull() ?: emptyList()
        } else emptyList()
        emit(list)
    }

    // Alias con el mismo comportamiento
    fun getModelosActivos(): Flow<List<ProductoDTO>> = getAllModelos()

    // Obtener modelos por marca devolviendo DTOs
    fun getModelosByMarca(idMarca: Long): Flow<List<ProductoDTO>> = flow {
        val res = inventarioRemoteRepository.getModelosByMarca(idMarca)
        val list = if (res.isSuccess) res.getOrNull() ?: emptyList() else emptyList()
        emit(list)
    }

    fun getModelosActivosByMarca(idMarca: Long): Flow<List<ProductoDTO>> = getModelosByMarca(idMarca)

    suspend fun getModeloById(idModelo: Long): ProductoDTO? {
        val res = inventarioRemoteRepository.getModeloById(idModelo)
        return res.getOrNull()
    }
}