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
    private val inventarioRepository: InventarioRepository,
    private val tallaRepository: TallaRepository
) {
    // Obtiene un Flow con todos los items del carrito para un cliente
    fun getCartForCliente(idCliente: Int): Flow<List<CartItemEntity>> = cartDao.getByCliente(idCliente)

    // Obtiene un item por su id
    suspend fun getById(id: Int): CartItemEntity? = cartDao.getById(id)

    // Inserta o actualiza (replace) un item del carrito sin validaciones
    suspend fun addOrUpdate(item: CartItemEntity): Long {
        return cartDao.insert(item)
    }

    // Inserta múltiples items al carrito de una vez (útil para importar carritos o compras rápidas)
    suspend fun insertAll(items: List<CartItemEntity>): List<Long> = cartDao.insertAll(items)

    // Actualiza un item existente
    suspend fun update(item: CartItemEntity) {
        cartDao.update(item)
    }

    // Elimina un item
    suspend fun delete(item: CartItemEntity) {
        cartDao.delete(item)
    }

    // Vacía el carrito de un cliente
    suspend fun clear(idCliente: Int) {
        cartDao.clearByCliente(idCliente)
    }

    // Cuenta los ítems del carrito de un cliente
    suspend fun getCountByCliente(idCliente: Int): Int = cartDao.getCountByCliente(idCliente)

    /**
     * Agrega un item al carrito con validaciones:
     * - verifica que la talla exista
     * - verifica que haya stock suficiente (considerando el item existente en carrito)
     * Si ya existe un item para cliente+modelo+talla, suma cantidades.
     */
    suspend fun addValidated(item: CartItemEntity): Result<Long> {
        // Verificar talla
        val tallaEntity = tallaRepository.getByNumero(item.talla)
        if (tallaEntity == null) {
            return Result.failure(Exception("Talla ${item.talla} no disponible"))
        }

        // Verificar inventario para modelo + talla
        val inventario = inventarioRepository.getByModeloYTalla(item.idModelo, tallaEntity.idTalla)
        if (inventario == null) {
            return Result.failure(Exception("Producto talla ${item.talla} no disponible en inventario"))
        }

        // Si ya existe en carrito, sumar cantidades
        val existente = cartDao.getByClienteModeloTalla(item.idCliente, item.idModelo, item.talla)
        val nuevaCantidad = (existente?.cantidad ?: 0) + item.cantidad

        // Validar stock disponible
        if (inventario.stockActual < nuevaCantidad) {
            val cantidadEnCarrito = existente?.cantidad ?: 0
            val mensaje = if (cantidadEnCarrito > 0) {
                "Ya tienes $cantidadEnCarrito en el carrito. Stock disponible: ${inventario.stockActual} unidades (talla ${item.talla})"
            } else {
                "Stock insuficiente. Solo hay ${inventario.stockActual} unidades disponibles (talla ${item.talla})"
            }
            return Result.failure(Exception(mensaje))
        }

        return try {
            val toSave = if (existente != null) existente.copy(cantidad = nuevaCantidad) else item
            val id = cartDao.insert(toSave)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
