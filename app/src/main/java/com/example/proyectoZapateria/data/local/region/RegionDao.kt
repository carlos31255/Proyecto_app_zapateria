package com.example.proyectoZapateria.data.local.region

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {

    // Insertar nueva región, retorna el ID generado
    @Insert
    suspend fun insert(region: RegionEntity): Long

    // Actualizar región existente
    @Update
    suspend fun update(region: RegionEntity)

    // Eliminar región
    @Delete
    suspend fun delete(region: RegionEntity)

    // Obtener región por ID
    @Query("SELECT * FROM region WHERE id_region = :id")
    suspend fun getById(id: Int): RegionEntity?

    // Obtener todas las regiones ordenadas alfabéticamente
    @Query("SELECT * FROM region ORDER BY nombre_region ASC")
    fun getAll(): Flow<List<RegionEntity>>

    // Buscar región por nombre exacto
    @Query("SELECT * FROM region WHERE nombre_region = :nombre")
    suspend fun getByNombre(nombre: String): RegionEntity?

    // Buscar región por abreviatura (ej: "RM", "V")
    @Query("SELECT * FROM region WHERE abreviatura = :abreviatura")
    suspend fun getByAbreviatura(abreviatura: String): RegionEntity?

    // Buscar regiones por nombre (para autocomplete)
    @Query("SELECT * FROM region WHERE nombre_region LIKE '%' || :query || '%'")
    fun searchRegiones(query: String): Flow<List<RegionEntity>>

    // Contar total de regiones
    @Query("SELECT COUNT(*) FROM region")
    suspend fun getCount(): Int

    // Eliminar todas las regiones
    @Query("DELETE FROM region")
    suspend fun deleteAll()
}

