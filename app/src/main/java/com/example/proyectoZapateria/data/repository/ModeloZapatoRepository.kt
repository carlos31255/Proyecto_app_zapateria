package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import kotlinx.coroutines.flow.Flow

class ModeloZapatoRepository(private val modeloDao: ModeloZapatoDao) {

    // Obtener todos los modelos
    fun getAllModelos(): Flow<List<ModeloZapatoEntity>> = modeloDao.getAllModelos()

    // Obtener modelos activos
    fun getModelosActivos(): Flow<List<ModeloZapatoEntity>> = modeloDao.getModelosActivos()

    // Obtener modelos por marca
    fun getModelosByMarca(idMarca: Int): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getModelosByMarca(idMarca)
    }

    // Obtener modelos activos por marca
    fun getModelosActivosByMarca(idMarca: Int): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getModelosActivosByMarca(idMarca)
    }

    // Obtener modelo por ID
    suspend fun getModeloById(idModelo: Int): ModeloZapatoEntity? {
        return modeloDao.getModeloById(idModelo)
    }

    // Buscar modelos por nombre
    fun searchModelosByNombre(query: String): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.searchModelosByNombre(query)
    }

    // Obtener modelos por rango de precio
    fun getModelosByRangoPrecio(precioMin: Double, precioMax: Double): Flow<List<ModeloZapatoEntity>> {
        return modeloDao.getModelosByRangoPrecio(precioMin, precioMax)
    }

    // Insertar nuevo modelo
    suspend fun insertModelo(modelo: ModeloZapatoEntity): Result<Long> {
        return try {
            // Verificar si ya existe ese modelo en la marca
            val existe = modeloDao.existeModeloEnMarca(modelo.idMarca, modelo.nombreModelo) > 0
            if (existe) {
                Result.failure(Exception("Ya existe un modelo con ese nombre en esta marca"))
            } else if (modelo.precioUnitario <= 0) {
                Result.failure(Exception("El precio debe ser mayor a 0"))
            } else {
                val id = modeloDao.insertModelo(modelo)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar modelo
    suspend fun updateModelo(modelo: ModeloZapatoEntity): Result<Unit> {
        return try {
            if (modelo.precioUnitario <= 0) {
                Result.failure(Exception("El precio debe ser mayor a 0"))
            } else {
                modeloDao.updateModelo(modelo)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar modelo
    suspend fun deleteModelo(modelo: ModeloZapatoEntity): Result<Unit> {
        return try {
            modeloDao.deleteModelo(modelo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cambiar estado de modelo
    suspend fun cambiarEstadoModelo(idModelo: Int, nuevoEstado: String): Result<Unit> {
        return try {
            modeloDao.cambiarEstadoModelo(idModelo, nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si existe modelo en marca
    suspend fun existeModeloEnMarca(idMarca: Int, nombreModelo: String): Boolean {
        return modeloDao.existeModeloEnMarca(idMarca, nombreModelo) > 0
    }
}

