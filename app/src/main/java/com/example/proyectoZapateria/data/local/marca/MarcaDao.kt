package com.example.proyectoZapateria.data.local.marca

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcaDao {

    // Insertar una nueva marca
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMarca(marca: MarcaEntity): Long

    // Actualizar marca existente
    @Update
    suspend fun updateMarca(marca: MarcaEntity)

    // Eliminar marca
    @Delete
    suspend fun deleteMarca(marca: MarcaEntity)

    // Obtener todas las marcas
    @Query("SELECT * FROM marca ORDER BY nombre_marca ASC")
    fun getAllMarcas(): Flow<List<MarcaEntity>>

    // Obtener marca por ID
    @Query("SELECT * FROM marca WHERE id_marca = :idMarca")
    suspend fun getMarcaById(idMarca: Int): MarcaEntity?

    // Obtener marca por nombre
    @Query("SELECT * FROM marca WHERE nombre_marca = :nombreMarca")
    suspend fun getMarcaByNombre(nombreMarca: String): MarcaEntity?

    // Obtener marcas activas
    @Query("SELECT * FROM marca WHERE estado = 'activa' ORDER BY nombre_marca ASC")
    fun getMarcasActivas(): Flow<List<MarcaEntity>>

    // Cambiar estado de marca
    @Query("UPDATE marca SET estado = :nuevoEstado WHERE id_marca = :idMarca")
    suspend fun cambiarEstadoMarca(idMarca: Int, nuevoEstado: String)

    // Verificar si existe marca con ese nombre
    @Query("SELECT COUNT(*) FROM marca WHERE nombre_marca = :nombreMarca")
    suspend fun existeMarcaConNombre(nombreMarca: String): Int
}

