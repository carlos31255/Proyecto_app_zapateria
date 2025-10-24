package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val modeloDao: ModeloZapatoDao,
    private val marcaDao: MarcaDao
) {

    // ========== MODELOS DE ZAPATOS ==========

    suspend fun insertModelo(modelo: ModeloZapatoEntity): Result<Long> {
        return try {
            val id = modeloDao.insertModelo(modelo)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateModelo(modelo: ModeloZapatoEntity): Result<Unit> {
        return try {
            modeloDao.updateModelo(modelo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteModelo(modelo: ModeloZapatoEntity): Result<Unit> {
        return try {
            modeloDao.deleteModelo(modelo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllModelos(): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getAllModelos()
    }

    fun getModelosActivos(): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getModelosActivos()
    }

    suspend fun getModeloById(idModelo: Int): ModeloZapatoEntity? {
        return modeloDao.getModeloById(idModelo)
    }

    fun getModelosByMarca(idMarca: Int): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getModelosByMarca(idMarca)
    }

    suspend fun cambiarEstadoModelo(idModelo: Int, nuevoEstado: String): Result<Unit> {
        return try {
            modeloDao.cambiarEstadoModelo(idModelo, nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun existeModeloEnMarca(idMarca: Int, nombreModelo: String): Boolean {
        return modeloDao.existeModeloEnMarca(idMarca, nombreModelo) > 0
    }

    fun searchModelosByNombre(query: String): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.searchModelosByNombre(query)
    }

    // ========== MARCAS ==========

    suspend fun insertMarca(marca: MarcaEntity): Result<Long> {
        return try {
            val id = marcaDao.insertMarca(marca)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllMarcas(): Flow<List<MarcaEntity>> {
        return marcaDao.getAllMarcas()
    }

    fun getMarcasActivas(): Flow<List<MarcaEntity>> {
        return marcaDao.getMarcasActivas()
    }

    suspend fun getMarcaById(idMarca: Int): MarcaEntity? {
        return marcaDao.getMarcaById(idMarca)
    }

    suspend fun existeMarcaConNombre(nombreMarca: String): Boolean {
        return marcaDao.existeMarcaConNombre(nombreMarca) > 0
    }
}

