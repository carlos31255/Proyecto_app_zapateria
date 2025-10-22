package com.example.proyectoZapateria.data.local.cliente

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    // Insertar nuevo cliente, retorna el ID generado
    @Insert
    suspend fun insert(cliente: ClienteEntity): Long

    // Actualizar cliente existente
    @Update
    suspend fun update(cliente: ClienteEntity)

    // Eliminar cliente
    @Delete
    suspend fun delete(cliente: ClienteEntity)

    // Obtener cliente por ID de persona
    @Query("SELECT * FROM cliente WHERE id_persona = :id")
    suspend fun getById(id: Int): ClienteEntity?

    // Obtener todos los clientes
    @Query("SELECT * FROM cliente")
    fun getAll(): Flow<List<ClienteEntity>>

    // Obtener clientes por categoría
    @Query("SELECT * FROM cliente WHERE categoria = :categoria")
    fun getByCategoria(categoria: String): Flow<List<ClienteEntity>>

    // Contar total de clientes
    @Query("SELECT COUNT(*) FROM cliente")
    suspend fun getCount(): Int

    // Obtener último cliente registrado
    @Query("SELECT * FROM cliente ORDER BY id_persona DESC LIMIT 1")
    suspend fun getLastCliente(): ClienteEntity?

    // === Consultas con JOIN a Persona ===

    // Buscar cliente por RUT con datos de persona
    @Query("""
        SELECT c.id_persona, p.nombre, p.apellido, p.rut, p.dv,
               p.telefono, p.email, c.categoria, p.username, 
               p.calle, p.numero_puerta as numeroPuerta, p.id_comuna as idComuna
        FROM cliente c
        INNER JOIN persona p ON c.id_persona = p.id_persona
        WHERE p.rut = :rut
    """)
    suspend fun getByRut(rut: String): ClienteConPersona?

    // Buscar clientes por nombre o apellido
    @Query("""
        SELECT c.id_persona, p.nombre, p.apellido, p.rut, p.dv,
               p.telefono, p.email, c.categoria, p.username, 
               p.calle, p.numero_puerta as numeroPuerta, p.id_comuna as idComuna
        FROM cliente c
        INNER JOIN persona p ON c.id_persona = p.id_persona
        WHERE p.nombre LIKE '%' || :query || '%' OR p.apellido LIKE '%' || :query || '%'
    """)
    fun searchClientes(query: String): Flow<List<ClienteConPersona>>

    // Obtener todos los clientes con datos de persona
    @Query("""
        SELECT c.id_persona, p.nombre, p.apellido, p.rut, p.dv,
               p.telefono, p.email, c.categoria, p.username, 
               p.calle, p.numero_puerta as numeroPuerta, p.id_comuna as idComuna
        FROM cliente c
        INNER JOIN persona p ON c.id_persona = p.id_persona
    """)
    fun getAllConPersona(): Flow<List<ClienteConPersona>>

    // Obtener cliente específico con datos de persona
    @Query("""
        SELECT c.id_persona, p.nombre, p.apellido, p.rut, p.dv,
               p.telefono, p.email, c.categoria, p.username, 
               p.calle, p.numero_puerta as numeroPuerta, p.id_comuna as idComuna
        FROM cliente c
        INNER JOIN persona p ON c.id_persona = p.id_persona
        WHERE c.id_persona = :id
    """)
    suspend fun getByIdConPersona(id: Int): ClienteConPersona?
}