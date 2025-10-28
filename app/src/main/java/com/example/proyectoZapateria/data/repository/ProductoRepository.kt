package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.inventario.InventarioDao
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.marca.MarcaDao
import com.example.proyectoZapateria.data.local.marca.MarcaEntity
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoDao
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.talla.TallaDao
import com.example.proyectoZapateria.data.local.talla.TallaEntity
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val modeloDao: ModeloZapatoDao,
    private val marcaDao: MarcaDao,
    private val tallaDao: TallaDao,
    private val inventarioDao: InventarioDao
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

    // ========== TALLAS ==========

    fun getAllTallas(): Flow<List<TallaEntity>> {
        return tallaDao.getAll()
    }

    suspend fun getTallaById(idTalla: Int): TallaEntity? {
        return tallaDao.getById(idTalla)
    }

    // ========== INVENTARIO ==========

    fun getInventarioByModelo(idModelo: Int): Flow<List<InventarioEntity>> {
        return inventarioDao.getByModelo(idModelo)
    }

    suspend fun getInventarioByModeloYTalla(idModelo: Int, idTalla: Int): InventarioEntity? {
        return inventarioDao.getByModeloYTalla(idModelo, idTalla)
    }

    suspend fun insertInventario(inventario: InventarioEntity): Result<Long> {
        return try {
            val id = inventarioDao.insert(inventario)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInventario(inventario: InventarioEntity): Result<Unit> {
        return try {
            inventarioDao.update(inventario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInventario(inventario: InventarioEntity): Result<Unit> {
        return try {
            inventarioDao.delete(inventario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

