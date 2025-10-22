package com.example.proyectoZapateria.data.local.inventario

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    // Insertar nuevo inventario, retorna el ID generado
    @Insert
    suspend fun insert(inventario: InventarioEntity): Long

    // Actualizar inventario existente
    @Update
    suspend fun update(inventario: InventarioEntity)

    // Eliminar inventario
    @Delete
    suspend fun delete(inventario: InventarioEntity)

    // Obtener inventario por ID
    @Query("SELECT * FROM inventario WHERE id_inventario = :id")
    suspend fun getById(id: Int): InventarioEntity?

    // Obtener todo el inventario
    @Query("SELECT * FROM inventario ORDER BY id_modelo ASC, id_talla ASC")
    fun getAll(): Flow<List<InventarioEntity>>

    // Obtener inventario por modelo
    @Query("SELECT * FROM inventario WHERE id_modelo = :idModelo ORDER BY id_talla ASC")
    fun getByModelo(idModelo: Int): Flow<List<InventarioEntity>>

    // Obtener inventario por talla
    @Query("SELECT * FROM inventario WHERE id_talla = :idTalla ORDER BY id_modelo ASC")
    fun getByTalla(idTalla: Int): Flow<List<InventarioEntity>>

    // Obtener inventario específico por modelo y talla
    @Query("SELECT * FROM inventario WHERE id_modelo = :idModelo AND id_talla = :idTalla")
    suspend fun getByModeloYTalla(idModelo: Int, idTalla: Int): InventarioEntity?

    // Obtener inventario con stock bajo (menor a un límite)
    @Query("SELECT * FROM inventario WHERE stock_actual < :limite ORDER BY stock_actual ASC")
    fun getStockBajo(limite: Int): Flow<List<InventarioEntity>>

    // Obtener inventario sin stock
    @Query("SELECT * FROM inventario WHERE stock_actual = 0")
    fun getSinStock(): Flow<List<InventarioEntity>>

    // Contar items de inventario
    @Query("SELECT COUNT(*) FROM inventario")
    suspend fun getCount(): Int

    // Contar items con stock
    @Query("SELECT COUNT(*) FROM inventario WHERE stock_actual > 0")
    suspend fun getCountConStock(): Int

    // Obtener stock total
    @Query("SELECT SUM(stock_actual) FROM inventario")
    suspend fun getStockTotal(): Int?
}

