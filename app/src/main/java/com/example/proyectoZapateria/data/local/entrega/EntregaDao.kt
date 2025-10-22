package com.example.proyectoZapateria.data.local.entrega

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {

    // Insertar nueva entrega, retorna el ID generado
    @Insert
    suspend fun insert(entrega: EntregaEntity): Long

    // Actualizar entrega existente
    @Update
    suspend fun update(entrega: EntregaEntity)

    // Eliminar entrega
    @Delete
    suspend fun delete(entrega: EntregaEntity)

    // Obtener entrega por ID
    @Query("SELECT * FROM entrega WHERE id_entrega = :id")
    suspend fun getById(id: Int): EntregaEntity?

    // Obtener todas las entregas ordenadas por fecha de asignación descendente
    @Query("SELECT * FROM entrega ORDER BY fecha_asignacion DESC")
    fun getAll(): Flow<List<EntregaEntity>>

    // Obtener entrega de una boleta específica
    @Query("SELECT * FROM entrega WHERE id_boleta = :idBoleta")
    suspend fun getByBoleta(idBoleta: Int): EntregaEntity?

    // Obtener entregas de un transportista específico
    @Query("SELECT * FROM entrega WHERE id_transportista = :idTransportista ORDER BY fecha_asignacion DESC")
    fun getByTransportista(idTransportista: Int): Flow<List<EntregaEntity>>

    // Obtener entregas por estado
    @Query("SELECT * FROM entrega WHERE estado_entrega = :estado ORDER BY fecha_asignacion DESC")
    fun getByEstado(estado: String): Flow<List<EntregaEntity>>

    // Obtener entregas pendientes (sin transportista asignado)
    @Query("SELECT * FROM entrega WHERE id_transportista IS NULL ORDER BY fecha_asignacion DESC")
    fun getEntregasPendientes(): Flow<List<EntregaEntity>>

    // Contar entregas por estado
    @Query("SELECT COUNT(*) FROM entrega WHERE estado_entrega = :estado")
    suspend fun getCountByEstado(estado: String): Int

    // Contar entregas de un transportista
    @Query("SELECT COUNT(*) FROM entrega WHERE id_transportista = :idTransportista")
    suspend fun getCountByTransportista(idTransportista: Int): Int
}

