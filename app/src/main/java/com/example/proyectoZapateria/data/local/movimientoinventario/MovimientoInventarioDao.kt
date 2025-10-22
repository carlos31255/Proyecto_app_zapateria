package com.example.proyectoZapateria.data.local.movimientoinventario

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoInventarioDao {

    // Insertar nuevo movimiento, retorna el ID generado
    @Insert
    suspend fun insert(movimiento: MovimientoInventarioEntity): Long

    // Actualizar movimiento existente
    @Update
    suspend fun update(movimiento: MovimientoInventarioEntity)

    // Eliminar movimiento
    @Delete
    suspend fun delete(movimiento: MovimientoInventarioEntity)

    // Obtener movimiento por ID
    @Query("SELECT * FROM movimientoinventario WHERE id_movimiento = :id")
    suspend fun getById(id: Int): MovimientoInventarioEntity?

    // Obtener todos los movimientos ordenados por fecha descendente
    @Query("SELECT * FROM movimientoinventario ORDER BY fecha_movimiento DESC")
    fun getAll(): Flow<List<MovimientoInventarioEntity>>

    // Obtener movimientos de un inventario específico
    @Query("SELECT * FROM movimientoinventario WHERE id_inventario = :idInventario ORDER BY fecha_movimiento DESC")
    fun getByInventario(idInventario: Int): Flow<List<MovimientoInventarioEntity>>

    // Obtener movimientos por tipo
    @Query("SELECT * FROM movimientoinventario WHERE id_tipo_movimiento = :idTipo ORDER BY fecha_movimiento DESC")
    fun getByTipo(idTipo: Int): Flow<List<MovimientoInventarioEntity>>

    // Calcular total de movimientos de un inventario por tipo
    @Query("SELECT SUM(cantidad) FROM movimientoinventario WHERE id_inventario = :idInventario AND id_tipo_movimiento = :idTipo")
    suspend fun getTotalByInventarioYTipo(idInventario: Int, idTipo: Int): Int?

    // Contar movimientos de un inventario
    @Query("SELECT COUNT(*) FROM movimientoinventario WHERE id_inventario = :idInventario")
    suspend fun getCountByInventario(idInventario: Int): Int

    // Obtener últimos N movimientos
    @Query("SELECT * FROM movimientoinventario ORDER BY fecha_movimiento DESC LIMIT :limit")
    fun getLastMovimientos(limit: Int): Flow<List<MovimientoInventarioEntity>>
}

