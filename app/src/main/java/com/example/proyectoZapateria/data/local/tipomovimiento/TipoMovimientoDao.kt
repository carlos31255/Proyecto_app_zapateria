package com.example.proyectoZapateria.data.local.tipomovimiento

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoMovimientoDao {

    // Insertar nuevo tipo de movimiento, retorna el ID generado
    @Insert
    suspend fun insert(tipoMovimiento: TipoMovimientoEntity): Long

    // Actualizar tipo de movimiento existente
    @Update
    suspend fun update(tipoMovimiento: TipoMovimientoEntity)

    // Eliminar tipo de movimiento
    @Delete
    suspend fun delete(tipoMovimiento: TipoMovimientoEntity)

    // Obtener tipo de movimiento por ID
    @Query("SELECT * FROM tipo_movimiento WHERE id_tipo_movimiento = :id")
    suspend fun getById(id: Int): TipoMovimientoEntity?

    // Obtener todos los tipos de movimiento
    @Query("SELECT * FROM tipo_movimiento ORDER BY codigo ASC")
    fun getAll(): Flow<List<TipoMovimientoEntity>>

    // Buscar tipo de movimiento por código exacto
    @Query("SELECT * FROM tipo_movimiento WHERE codigo = :codigo")
    suspend fun getByCodigo(codigo: String): TipoMovimientoEntity?

    // Obtener tipos de movimiento por signo (1 para entradas, -1 para salidas)
    @Query("SELECT * FROM tipo_movimiento WHERE signo = :signo")
    fun getBySigno(signo: Int): Flow<List<TipoMovimientoEntity>>

    // Buscar tipos de movimiento por código o descripción
    @Query("SELECT * FROM tipo_movimiento WHERE codigo LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%'")
    fun searchTipoMovimientos(query: String): Flow<List<TipoMovimientoEntity>>

    // Contar total de tipos de movimiento
    @Query("SELECT COUNT(*) FROM tipo_movimiento")
    suspend fun getCount(): Int

    // Eliminar todos los tipos de movimiento
    @Query("DELETE FROM tipo_movimiento")
    suspend fun deleteAll()
}

