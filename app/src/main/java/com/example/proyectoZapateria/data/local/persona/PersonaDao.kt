package com.example.proyectoZapateria.data.local.persona

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {

    // Insertar nueva persona, retorna el ID generado
    @Insert
    suspend fun insert(persona: PersonaEntity): Long

    // Actualizar persona existente
    @Update
    suspend fun update(persona: PersonaEntity)

    // Eliminar persona
    @Delete
    suspend fun delete(persona: PersonaEntity)

    // Obtener persona por ID
    @Query("SELECT * FROM persona WHERE id_persona = :id")
    suspend fun getById(id: Int): PersonaEntity?

    // Obtener todas las personas
    @Query("SELECT * FROM persona")
    fun getAll(): Flow<List<PersonaEntity>>

    // Buscar persona por RUT
    @Query("SELECT * FROM persona WHERE rut = :rut")
    suspend fun getByRut(rut: String): PersonaEntity?

    // Buscar persona por username (para login)
    @Query("SELECT * FROM persona WHERE username = :username")
    suspend fun getByUsername(username: String): PersonaEntity?

    // Buscar persona por email
    @Query("SELECT * FROM persona WHERE email = :email")
    suspend fun getByEmail(email: String): PersonaEntity?

    // Buscar personas por nombre o apellido
    @Query("SELECT * FROM persona WHERE nombre LIKE '%' || :query || '%' OR apellido LIKE '%' || :query || '%'")
    fun searchPersonas(query: String): Flow<List<PersonaEntity>>

    // Obtener personas por estado (activo/inactivo)
    @Query("SELECT * FROM persona WHERE estado = :estado")
    fun getByEstado(estado: String): Flow<List<PersonaEntity>>

    // Contar total de personas
    @Query("SELECT COUNT(*) FROM persona")
    suspend fun getCount(): Int

    // Obtener última persona registrada
    @Query("SELECT * FROM persona ORDER BY id_persona DESC LIMIT 1")
    suspend fun getLastPersona(): PersonaEntity?
}

