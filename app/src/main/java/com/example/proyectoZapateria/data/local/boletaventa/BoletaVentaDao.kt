package com.example.proyectoZapateria.data.local.boletaventa

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BoletaVentaDao {
    // Insertar nueva boleta, retorna el ID generado
    @Insert
    suspend fun insert(boleta: BoletaVentaEntity): Long

    // Actualizar boleta existente
    @Update
    suspend fun update(boleta: BoletaVentaEntity)

    // Eliminar boleta
    @Delete
    suspend fun delete(boleta: BoletaVentaEntity)

    // Obtener boleta por ID
    @Query("SELECT * FROM boletaventa WHERE id_boleta = :id")
    suspend fun getById(id: Int): BoletaVentaEntity?

    // Obtener todas las boletas ordenadas por fecha descendente
    @Query("SELECT * FROM boletaventa ORDER BY fecha DESC")
    fun getAll(): Flow<List<BoletaVentaEntity>>

    // Obtener boletas de un cliente específico
    @Query("SELECT * FROM boletaventa WHERE id_cliente = :idCliente")
    fun getByCliente(idCliente: Int): Flow<List<BoletaVentaEntity>>

    // Obtener boletas realizadas por un vendedor específico
    @Query("SELECT * FROM boletaventa WHERE id_vendedor = :idVendedor")
    fun getByVendedor(idVendedor: Int): Flow<List<BoletaVentaEntity>>
}