package com.example.proyectoZapateria.data.local.comuna

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComunaDao {

    // Insertar nueva comuna, retorna el ID generado
    @Insert
    suspend fun insert(comuna: ComunaEntity): Long

    // Actualizar comuna existente
    @Update
    suspend fun update(comuna: ComunaEntity)

    // Eliminar comuna
    @Delete
    suspend fun delete(comuna: ComunaEntity)

    // Obtener comuna por ID
    @Query("SELECT * FROM comuna WHERE id_comuna = :id")
    suspend fun getById(id: Int): ComunaEntity?

    // Obtener todas las comunas ordenadas alfabéticamente
    @Query("SELECT * FROM comuna ORDER BY nombre_comuna ASC")
    fun getAll(): Flow<List<ComunaEntity>>

    // Obtener comunas de una región específica (para formularios en cascada)
    @Query("SELECT * FROM comuna WHERE id_region = :idRegion ORDER BY nombre_comuna ASC")
    fun getByRegion(idRegion: Int): Flow<List<ComunaEntity>>

    // Buscar comuna específica dentro de una región
    @Query("SELECT * FROM comuna WHERE nombre_comuna = :nombre AND id_region = :idRegion")
    suspend fun getByNombreAndRegion(nombre: String, idRegion: Int): ComunaEntity?

    // Buscar comunas por nombre (para autocomplete)
    @Query("SELECT * FROM comuna WHERE nombre_comuna LIKE '%' || :query || '%'")
    fun searchComunas(query: String): Flow<List<ComunaEntity>>

    // Contar total de comunas
    @Query("SELECT COUNT(*) FROM comuna")
    suspend fun getCount(): Int

    // Contar comunas de una región específica
    @Query("SELECT COUNT(*) FROM comuna WHERE id_region = :idRegion")
    suspend fun getCountByRegion(idRegion: Int): Int

    // Eliminar todas las comunas
    @Query("DELETE FROM comuna")
    suspend fun deleteAll()

    // Eliminar todas las comunas de una región
    @Query("DELETE FROM comuna WHERE id_region = :idRegion")
    suspend fun deleteByRegion(idRegion: Int)
}

