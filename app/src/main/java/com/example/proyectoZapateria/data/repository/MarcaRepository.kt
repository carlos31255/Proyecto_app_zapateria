package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import kotlinx.coroutines.flow.Flow

class MarcaRepository(private val marcaDao: MarcaDao) {

    // Obtener todas las marcas (Flow para observar cambios)
    fun getAllMarcas(): Flow<List<MarcaEntity>> = marcaDao.getAllMarcas()

    // Obtener marcas activas
    fun getMarcasActivas(): Flow<List<MarcaEntity>> = marcaDao.getMarcasActivas()

    // Obtener marca por ID
    suspend fun getMarcaById(idMarca: Int): MarcaEntity? {
        return marcaDao.getMarcaById(idMarca)
    }

    // Obtener marca por nombre
    suspend fun getMarcaByNombre(nombreMarca: String): MarcaEntity? {
        return marcaDao.getMarcaByNombre(nombreMarca)
    }

    // Insertar nueva marca
    suspend fun insertMarca(marca: MarcaEntity): Result<Long> {
        return try {
            // Verificar si ya existe una marca con ese nombre
            val existe = marcaDao.existeMarcaConNombre(marca.nombreMarca) > 0
            if (existe) {
                Result.failure(Exception("Ya existe una marca con ese nombre"))
            } else {
                val id = marcaDao.insertMarca(marca)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar marca
    suspend fun updateMarca(marca: MarcaEntity): Result<Unit> {
        return try {
            marcaDao.updateMarca(marca)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar marca
    suspend fun deleteMarca(marca: MarcaEntity): Result<Unit> {
        return try {
            marcaDao.deleteMarca(marca)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cambiar estado de marca (activar/desactivar)
    suspend fun cambiarEstadoMarca(idMarca: Int, nuevoEstado: String): Result<Unit> {
        return try {
            marcaDao.cambiarEstadoMarca(idMarca, nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si existe marca
    suspend fun existeMarca(nombreMarca: String): Boolean {
        return marcaDao.existeMarcaConNombre(nombreMarca) > 0
    }
}

