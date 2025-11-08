package com.example.proyectoZapateria.data.local.rol

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RolDao {

    // Insertar nuevo rol, retorna el ID generado
    @Insert
    suspend fun insert(rol: RolEntity): Long

    // Actualizar rol existente
    @Update
    suspend fun update(rol: RolEntity)

    // Eliminar rol
    @Delete
    suspend fun delete(rol: RolEntity)

    // Obtener rol por ID
    @Query("SELECT * FROM rol WHERE id_rol = :id")
    suspend fun getById(id: Int): RolEntity?

    // Obtener todos los roles ordenados alfabéticamente
    @Query("SELECT * FROM rol ORDER BY nombre_rol ASC")
    fun getAll(): Flow<List<RolEntity>>

    // Buscar rol por nombre exacto (ej: "administrador", "cliente")
    @Query("SELECT * FROM rol WHERE nombre_rol = :nombre")
    suspend fun getByNombre(nombre: String): RolEntity?

    // Buscar roles por nombre (para autocomplete)
    @Query("SELECT * FROM rol WHERE nombre_rol LIKE '%' || :query || '%'")
    fun searchRoles(query: String): Flow<List<RolEntity>>

    // Contar total de roles
    @Query("SELECT COUNT(*) FROM rol")
    suspend fun getCount(): Int

    // Eliminar todos los roles
    @Query("DELETE FROM rol")
    suspend fun deleteAll()
}

