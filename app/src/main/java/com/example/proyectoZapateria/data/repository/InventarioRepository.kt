package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.inventario.InventarioDao
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventarioRepository @Inject constructor(
    private val inventarioDao: InventarioDao
) {
    fun getAll(): Flow<List<InventarioEntity>> = inventarioDao.getAll()

    fun getByModelo(idModelo: Int): Flow<List<InventarioEntity>> = inventarioDao.getByModelo(idModelo)

    suspend fun getByModeloYTalla(idModelo: Int, idTalla: Int): InventarioEntity? = inventarioDao.getByModeloYTalla(idModelo, idTalla)

    fun getStockBajo(limite: Int): Flow<List<InventarioEntity>> = inventarioDao.getStockBajo(limite)

    suspend fun getById(idInventario: Int): InventarioEntity? = inventarioDao.getById(idInventario)

    suspend fun insertInventario(inventario: InventarioEntity): Long {
        return inventarioDao.insert(inventario)
    }

    suspend fun updateInventario(inventario: InventarioEntity) {
        inventarioDao.update(inventario)
    }
}
