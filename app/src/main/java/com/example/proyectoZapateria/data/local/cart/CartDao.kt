package com.example.proyectoZapateria.data.local.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    // Devuelve un Flow con todos los items del carrito para un cliente
    @Query("SELECT * FROM cart_item WHERE id_cliente = :idCliente")
    fun getByCliente(idCliente: Long): Flow<List<CartItemEntity>>

    // Obtiene un item por su id (o null si no existe)
    @Query("SELECT * FROM cart_item WHERE id_cart_item = :id")
    suspend fun getById(id: Long): CartItemEntity?

    // Busca un item por cliente+modelo+talla (evita duplicados)
    @Query("SELECT * FROM cart_item WHERE id_cliente = :idCliente AND id_modelo = :idModelo AND talla = :talla LIMIT 1")
    suspend fun getByClienteModeloTalla(idCliente: Long, idModelo: Long, talla: String): CartItemEntity?

    // Inserta o reemplaza un item del carrito y retorna el id generado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity): Long

    // Inserta varios items (batch) y retorna los ids generados
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CartItemEntity>): List<Long>

    // Actualiza un item existente
    @Update
    suspend fun update(item: CartItemEntity)

    // Elimina un item del carrito
    @Delete
    suspend fun delete(item: CartItemEntity)

    // Elimina todos los items del carrito para el cliente dado
    @Query("DELETE FROM cart_item WHERE id_cliente = :idCliente")
    suspend fun clearByCliente(idCliente: Long)

    // Retorna la cantidad de items que tiene el cliente en el carrito
    @Query("SELECT COUNT(*) FROM cart_item WHERE id_cliente = :idCliente")
    suspend fun getCountByCliente(idCliente: Long): Int
}
