package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Remote repository que expone los DTOs de modelos desde el microservicio de inventario.
 */
class ModeloZapatoRemoteRepository @Inject constructor(
    private val inventarioRemoteRepository: InventarioRemoteRepository
) {

    fun getAllModelos(): Flow<List<ModeloZapatoEntity>> = flow {
        val res = inventarioRemoteRepository.getModelos()
        val list = if (res.isSuccess) {
            res.getOrNull()?.map { dto -> mapDtoToEntity(dto) } ?: emptyList()
        } else emptyList()
        emit(list)
    }

    fun getModelosActivos(): Flow<List<ModeloZapatoEntity>> = getAllModelos()

    fun getModelosByMarca(idMarca: Long): Flow<List<ModeloZapatoEntity>> = flow {
        val res = inventarioRemoteRepository.getModelosByMarca(idMarca)
        val list = if (res.isSuccess) res.getOrNull()?.map { mapDtoToEntity(it) }
            ?: emptyList() else emptyList()
        emit(list)
    }

    fun getModelosActivosByMarca(idMarca: Long): Flow<List<ModeloZapatoEntity>> = getModelosByMarca(idMarca)

    suspend fun getModeloById(idModelo: Long): ModeloZapatoEntity? {
        val res = inventarioRemoteRepository.getModeloById(idModelo)
        return res.getOrNull()?.let { mapDtoToEntity(it) }
    }

    private fun mapDtoToEntity(dto: ModeloZapatoDTO): ModeloZapatoEntity {
        return ModeloZapatoEntity(
            idModelo = dto.id ?: 0L,
            nombreModelo = dto.nombre,
            idMarca = dto.marcaId ?: 0L,
            descripcion = dto.descripcion ?: "",
            precioUnitario = 0,
            imagenUrl = null,
            estado = "activo"
        )
    }
}