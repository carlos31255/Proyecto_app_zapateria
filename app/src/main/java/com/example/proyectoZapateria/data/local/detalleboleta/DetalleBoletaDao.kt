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

    // Obtener lista de productos (modelo, talla, cantidad, marca) de una boleta específica
    @Query("""
        SELECT 
            mz.nombre_modelo as nombreZapato, 
            t.numero_talla as talla, 
            d.cantidad as cantidad, 
            m.nombre_marca as marca
        FROM DetalleBoleta d
        INNER JOIN Inventario i ON d.id_inventario = i.id_inventario
        INNER JOIN ModeloZapato mz ON i.id_modelo = mz.id_modelo
        INNER JOIN Talla t ON i.id_talla = t.id_talla
        INNER JOIN Marca m ON mz.id_marca = m.id_marca
        WHERE d.id_boleta = :idBoleta
    """)
    fun getProductosDeBoleta(idBoleta: Int): Flow<List<ProductoDetalle>>
}

