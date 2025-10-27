package com.example.proyectoZapateria.data.local.modelo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeloZapatoDao {

    // Insertar un nuevo modelo
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertModelo(modelo: ModeloZapatoEntity): Long

    // Actualizar modelo existente
    @Update
    suspend fun updateModelo(modelo: ModeloZapatoEntity)

    // Eliminar modelo
    @Delete
    suspend fun deleteModelo(modelo: ModeloZapatoEntity)

    // Obtener todos los modelos
    @Query("SELECT * FROM modelozapato ORDER BY nombre_modelo ASC")
    fun getAllModelos(): Flow<List<ModeloZapatoEntity>>

    // Obtener modelo por ID
    @Query("SELECT * FROM modelozapato WHERE id_modelo = :idModelo")
    suspend fun getModeloById(idModelo: Int): ModeloZapatoEntity?

    // Obtener modelos por marca
    @Query("SELECT * FROM modelozapato WHERE id_marca = :idMarca ORDER BY nombre_modelo ASC")
    fun getModelosByMarca(idMarca: Int): Flow<List<ModeloZapatoEntity>>

    // Obtener modelos activos
    @Query("SELECT * FROM modelozapato WHERE estado = 'activo' ORDER BY nombre_modelo ASC")
    fun getModelosActivos(): Flow<List<ModeloZapatoEntity>>

    // Obtener modelos activos por marca
    @Query("SELECT * FROM modelozapato WHERE id_marca = :idMarca AND estado = 'activo' ORDER BY nombre_modelo ASC")
    fun getModelosActivosByMarca(idMarca: Int): Flow<List<ModeloZapatoEntity>>

    // Cambiar estado de modelo
    @Query("UPDATE modelozapato SET estado = :nuevoEstado WHERE id_modelo = :idModelo")
    suspend fun cambiarEstadoModelo(idModelo: Int, nuevoEstado: String)

    // Verificar si existe modelo con ese nombre en una marca
    @Query("SELECT COUNT(*) FROM modelozapato WHERE id_marca = :idMarca AND nombre_modelo = :nombreModelo")
    suspend fun existeModeloEnMarca(idMarca: Int, nombreModelo: String): Int

    // Buscar modelos por nombre (búsqueda parcial)
    @Query("SELECT * FROM modelozapato WHERE nombre_modelo LIKE '%' || :query || '%' ORDER BY nombre_modelo ASC")
    fun searchModelosByNombre(query: String): Flow<List<ModeloZapatoEntity>>

    // Obtener modelos por rango de precio
    @Query("SELECT * FROM modelozapato WHERE precio_unitario BETWEEN :precioMin AND :precioMax ORDER BY precio_unitario ASC")
    fun getModelosByRangoPrecio(precioMin: Int, precioMax: Int): Flow<List<ModeloZapatoEntity>>
}
