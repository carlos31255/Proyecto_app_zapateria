package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repositorio para operaciones del carrito.
 * Incluye validaciones contra inventario y tallas.
 */
class CartRepository @Inject constructor(
    private val cartDao: CartDao,
    // La validación se hará en el ViewModel contra el Microservicio.
) {
    // Obtener items
    fun getCartForCliente(idCliente: Long): Flow<List<CartItemEntity>> = cartDao.getByCliente(idCliente)

    // Agregar o actualizar (Sin validar stock aquí, se valida antes de llamar a este método)
    suspend fun addOrUpdate(item: CartItemEntity): Long {

        return cartDao.insert(item)
    }


    suspend fun getCountByCliente(idCliente: Long): Int {
        return cartDao.getCountByCliente(idCliente)
    }
    suspend fun delete(item: CartItemEntity) {
        cartDao.delete(item)
    }
    // metodo para limpiar el carrito
    suspend fun clear(idCliente: Long) {
        cartDao.clearByCliente(idCliente)
    }

    // Método auxiliar
    suspend fun getItem(idCliente: Long, idModelo: Long, talla: String): CartItemEntity? {
        return cartDao.getByClienteModeloTalla(idCliente, idModelo, talla)
    }
}
