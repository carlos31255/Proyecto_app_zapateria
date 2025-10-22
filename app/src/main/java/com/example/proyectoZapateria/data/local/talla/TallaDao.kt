package com.example.proyectoZapateria.data.local.talla

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TallaDao {

    // Insertar nueva talla, retorna el ID generado
    @Insert
    suspend fun insert(talla: TallaEntity): Long

    // Actualizar talla existente
    @Update
    suspend fun update(talla: TallaEntity)

    // Eliminar talla
    @Delete
    suspend fun delete(talla: TallaEntity)

    // Obtener talla por ID
    @Query("SELECT * FROM talla WHERE id_talla = :id")
    suspend fun getById(id: Int): TallaEntity?

    // Obtener todas las tallas ordenadas por número
    @Query("SELECT * FROM talla ORDER BY CAST(numero_talla AS INTEGER) ASC")
    fun getAll(): Flow<List<TallaEntity>>

    // Buscar talla por número exacto
    @Query("SELECT * FROM talla WHERE numero_talla = :numero")
    suspend fun getByNumero(numero: String): TallaEntity?

    // Buscar tallas por número (para autocomplete)
    @Query("SELECT * FROM talla WHERE numero_talla LIKE '%' || :query || '%'")
    fun searchTallas(query: String): Flow<List<TallaEntity>>

    // Contar total de tallas
    @Query("SELECT COUNT(*) FROM talla")
    suspend fun getCount(): Int

    // Eliminar todas las tallas
    @Query("DELETE FROM talla")
    suspend fun deleteAll()
}

