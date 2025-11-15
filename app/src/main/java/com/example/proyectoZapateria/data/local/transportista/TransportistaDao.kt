package com.example.proyectoZapateria.data.local.transportista

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportistaDao {

    // Insertar nuevo transportista, retorna el ID generado
    @Insert
    suspend fun insert(transportista: TransportistaEntity): Long

    // Actualizar transportista existente
    @Update
    suspend fun update(transportista: TransportistaEntity)

    // Eliminar transportista
    @Delete
    suspend fun delete(transportista: TransportistaEntity)

    // Obtener transportista por ID de persona
    @Query("SELECT * FROM transportista WHERE id_persona = :id")
    suspend fun getById(id: Int): TransportistaEntity?

    // Obtener todos los transportistas
    @Query("SELECT * FROM transportista")
    fun getAll(): Flow<List<TransportistaEntity>>

    // Buscar transportista por licencia
    @Query("SELECT * FROM transportista WHERE licencia = :licencia")
    suspend fun getByLicencia(licencia: String): TransportistaEntity?

    // Contar total de transportistas
    @Query("SELECT COUNT(*) FROM transportista")
    suspend fun getCount(): Int

    // Eliminar todos los transportistas
    @Query("DELETE FROM transportista")
    suspend fun deleteAll()

    // === Consultas con JOIN a Persona ===
    // TODO: Queries comentadas - usan JOIN con tabla persona que ya no existe localmente
    // Cuando se implemente el microservicio de transportistas, obtener estos datos desde la API

    /*
    // Obtener transportista específico con datos de persona
    @Query("""
        SELECT t.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               t.licencia, t.vehiculo
        FROM transportista t
        INNER JOIN persona p ON t.id_persona = p.id_persona
        WHERE t.id_persona = :id
    """)
    suspend fun getByIdConPersona(id: Int): TransportistaConPersona?

    // Obtener todos los transportistas con datos de persona
    @Query("""
        SELECT t.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               t.licencia, t.vehiculo
        FROM transportista t
        INNER JOIN persona p ON t.id_persona = p.id_persona
        ORDER BY p.nombre ASC, p.apellido ASC
    """)
    fun getAllConPersona(): Flow<List<TransportistaConPersona>>

    // Buscar transportistas por nombre, apellido o username
    @Query("""
        SELECT t.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               t.licencia, t.vehiculo
        FROM transportista t
        INNER JOIN persona p ON t.id_persona = p.id_persona
        WHERE p.nombre LIKE '%' || :query || '%' 
           OR p.apellido LIKE '%' || :query || '%'
           OR p.username LIKE '%' || :query || '%'
    """)
    fun searchTransportistas(query: String): Flow<List<TransportistaConPersona>>

    // Obtener transportistas por estado (activo/inactivo)
    @Query("""
        SELECT t.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               t.licencia, t.vehiculo
        FROM transportista t
        INNER JOIN persona p ON t.id_persona = p.id_persona
        WHERE p.estado = :estado
        ORDER BY p.nombre ASC, p.apellido ASC
    """)
    fun getByEstadoConPersona(estado: String): Flow<List<TransportistaConPersona>>
    */
}

