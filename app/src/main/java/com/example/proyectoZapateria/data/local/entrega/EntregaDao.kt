package com.example.proyectoZapateria.data.local.entrega

import androidx.room.*
import kotlinx.coroutines.flow.Flow



@Dao
interface EntregaDao {

        // Insertar nueva entrega, retorna el ID generado
        @Insert
        suspend fun insert(entrega: EntregaEntity): Long

        // Actualizar entrega existente (ej: cambiar estado)
        @Update
        suspend fun updateEntrega(entrega: EntregaEntity)

        // Eliminar entrega
        @Delete
        suspend fun delete(entrega: EntregaEntity)

        // Obtener todas las entregas
        @Query("SELECT * FROM entrega ORDER BY fecha_asignacion DESC")
        fun getAll(): Flow<List<EntregaEntity>>

        // Obtener entrega por ID
        @Query("SELECT * FROM entrega WHERE id_entrega = :id")
        suspend fun getEntregaById(id: Int): EntregaEntity?

        // Obtener entrega de una boleta específica
        @Query("SELECT * FROM entrega WHERE id_boleta = :idBoleta")
        suspend fun getByBoleta(idBoleta: Int): EntregaEntity?

        // Obtener entregas (simples) de un transportista específico
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
        // Cuenta entregas para un transportista específico con un estado específico.

        @Query("SELECT COUNT(*) FROM entrega WHERE id_transportista = :transportistaId AND estado_entrega = :estado")
        suspend fun getCountByEstadoParaTransportista(transportistaId: Int, estado: String): Int
        // Obtener la lista de entregas de un transportista, usando la boleta (para el id) y la persona (para el nombre y direccion)
        @Query("""
        SELECT 
            e.id_entrega as idEntrega,
            e.estado_entrega as estadoEntrega,
            e.fecha_asignacion as fechaAsignacion,
            b.id_boleta as numeroBoleta,
            (p.nombre || ' ' || p.apellido) as clienteNombre,
            p.calle as calle,
            p.numero_puerta as numeroPuerta
        FROM entrega e
        INNER JOIN boletaventa b ON e.id_boleta = b.id_boleta
        INNER JOIN cliente c ON b.id_cliente = c.id_persona
        INNER JOIN persona p ON c.id_persona = p.id_persona
        WHERE e.id_transportista = :transportistaId
        ORDER BY 
            CASE e.estado_entrega
                WHEN 'pendiente' THEN 1
                WHEN 'completada' THEN 2
                ELSE 3
            END, 
            e.fecha_asignacion DESC
    """)
        fun getEntregasConDetalles(transportistaId: Int): Flow<List<EntregaConDetalles>>


        // Obtener los detalles (con JOIN) de una sola entrega por su ID
        @Query("""
        SELECT 
            e.id_entrega as idEntrega,
            e.estado_entrega as estadoEntrega,
            e.fecha_asignacion as fechaAsignacion,
            b.id_boleta as numeroBoleta,
            (p.nombre || ' ' || p.apellido) as clienteNombre,
            p.calle as calle,
            p.numero_puerta as numeroPuerta
        FROM entrega e
        INNER JOIN boletaventa b ON e.id_boleta = b.id_boleta
        INNER JOIN cliente c ON b.id_cliente = c.id_persona
        INNER JOIN persona p ON c.id_persona = p.id_persona
        WHERE e.id_entrega = :idEntrega
    """)
        // Usamos Flow para que la UI se actualice si el estado cambia
        fun getDetallesPorId(idEntrega: Int): Flow<EntregaConDetalles>

    }


