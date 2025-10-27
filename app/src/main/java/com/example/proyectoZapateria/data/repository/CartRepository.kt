package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.cart.CartDao
import com.example.proyectoZapateria.data.local.cart.CartItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.example.proyectoZapateria.data.repository.InventarioRepository
import com.example.proyectoZapateria.data.repository.TallaRepository

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

    // Busca un item por cliente+modelo+talla
    suspend fun getByClienteModeloTalla(idCliente: Int, idModelo: Int, talla: String): CartItemEntity? = cartDao.getByClienteModeloTalla(idCliente, idModelo, talla)

    // Inserta o actualiza (replace) un item del carrito sin validaciones
    suspend fun addOrUpdate(item: CartItemEntity): Long {
        return cartDao.insert(item)
    }

    // Inserta múltiples items (batch)
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
     * Retorna Result.success(idInsertado) o Result.failure(Exception(msg)).
     */
    suspend fun addValidated(item: CartItemEntity): Result<Long> {
        // Verificar talla
        val tallaEntity = tallaRepository.getByNumero(item.talla)
        if (tallaEntity == null) {
            return Result.failure(Exception("Talla no encontrada: ${item.talla}"))
        }

        // Verificar inventario para modelo + talla
        val inventario = inventarioRepository.getByModeloYTalla(item.idModelo, tallaEntity.idTalla)
        if (inventario == null) {
            return Result.failure(Exception("Inventario no encontrado para modelo ${item.idModelo} talla ${item.talla}"))
        }

        // Si ya existe en carrito, sumar cantidades
        val existente = cartDao.getByClienteModeloTalla(item.idCliente, item.idModelo, item.talla)
        val nuevaCantidad = (existente?.cantidad ?: 0) + item.cantidad

        if (inventario.stockActual < nuevaCantidad) {
            return Result.failure(Exception("Stock insuficiente: disponible=${inventario.stockActual}, requerido=$nuevaCantidad"))
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
