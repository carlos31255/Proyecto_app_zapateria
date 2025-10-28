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

    // Cancelar una boleta (actualizar estado)
    @Query("UPDATE boletaventa SET estado = 'CANCELADA' WHERE id_boleta = :idBoleta")
    suspend fun cancelarBoleta(idBoleta: Int)

    // Obtener todas las boletas con información del cliente y vendedor
    @Query("""
        SELECT 
            b.id_boleta,
            b.numero_boleta,
            b.fecha,
            b.monto_total,
            b.estado,
            p_cliente.nombre as nombre_cliente,
            p_cliente.apellido as apellido_cliente,
            p_vendedor.nombre as nombre_vendedor,
            p_vendedor.apellido as apellido_vendedor
        FROM boletaventa b
        INNER JOIN cliente c ON b.id_cliente = c.id_persona
        INNER JOIN persona p_cliente ON c.id_persona = p_cliente.id_persona
        LEFT JOIN usuario u ON b.id_vendedor = u.id_persona
        LEFT JOIN persona p_vendedor ON u.id_persona = p_vendedor.id_persona
        ORDER BY b.fecha DESC
    """)
    fun getAllBoletasConInfo(): Flow<List<BoletaVentaConInfo>>

    // Obtener boletas por rango de fechas para reportes
    @Query("""
        SELECT 
            b.id_boleta,
            b.numero_boleta,
            b.fecha,
            b.monto_total,
            b.estado,
            p_cliente.nombre as nombre_cliente,
            p_cliente.apellido as apellido_cliente,
            p_vendedor.nombre as nombre_vendedor,
            p_vendedor.apellido as apellido_vendedor
        FROM boletaventa b
        INNER JOIN cliente c ON b.id_cliente = c.id_persona
        INNER JOIN persona p_cliente ON c.id_persona = p_cliente.id_persona
        LEFT JOIN usuario u ON b.id_vendedor = u.id_persona
        LEFT JOIN persona p_vendedor ON u.id_persona = p_vendedor.id_persona
        WHERE b.fecha >= :fechaInicio AND b.fecha < :fechaFin
        ORDER BY b.fecha DESC
    """)
    suspend fun getBoletasByRangoFechas(fechaInicio: Long, fechaFin: Long): List<BoletaVentaConInfo>

    // Eliminar boleta por ID (para cancelar)
    @Query("DELETE FROM boletaventa WHERE id_boleta = :id")
    suspend fun deleteSync(id: Int)
}

data class BoletaVentaConInfo(
    val id_boleta: Int,
    val numero_boleta: String,
    val fecha: Long,
    val monto_total: Int,
    val estado: String,
    val nombre_cliente: String,
    val apellido_cliente: String,
    val nombre_vendedor: String?,
    val apellido_vendedor: String?
)
