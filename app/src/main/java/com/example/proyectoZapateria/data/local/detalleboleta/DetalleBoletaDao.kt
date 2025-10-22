package com.example.proyectoZapateria.data.local.detalleboleta

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleBoletaDao {

    // Insertar nuevo detalle de boleta, retorna el ID generado
    @Insert
    suspend fun insert(detalle: DetalleBoletaEntity): Long

    // Insertar múltiples detalles de boleta
    @Insert
    suspend fun insertAll(detalles: List<DetalleBoletaEntity>): List<Long>

    // Actualizar detalle de boleta existente
    @Update
    suspend fun update(detalle: DetalleBoletaEntity)

    // Eliminar detalle de boleta
    @Delete
    suspend fun delete(detalle: DetalleBoletaEntity)

    // Obtener detalle por ID
    @Query("SELECT * FROM detalleboleta WHERE id_detalle = :id")
    suspend fun getById(id: Int): DetalleBoletaEntity?

    // Obtener todos los detalles de una boleta específica
    @Query("SELECT * FROM detalleboleta WHERE id_boleta = :idBoleta")
    fun getByBoleta(idBoleta: Int): Flow<List<DetalleBoletaEntity>>

    // Obtener detalles por inventario (para ver ventas de un modelo/talla específico)
    @Query("SELECT * FROM detalleboleta WHERE id_inventario = :idInventario")
    fun getByInventario(idInventario: Int): Flow<List<DetalleBoletaEntity>>

    // Calcular total de unidades vendidas de un inventario
    @Query("SELECT SUM(cantidad) FROM detalleboleta WHERE id_inventario = :idInventario")
    suspend fun getTotalVendidoByInventario(idInventario: Int): Int?

    // Contar detalles de una boleta
    @Query("SELECT COUNT(*) FROM detalleboleta WHERE id_boleta = :idBoleta")
    suspend fun getCountByBoleta(idBoleta: Int): Int

    // Eliminar todos los detalles de una boleta
    @Query("DELETE FROM detalleboleta WHERE id_boleta = :idBoleta")
    suspend fun deleteByBoleta(idBoleta: Int)
}

