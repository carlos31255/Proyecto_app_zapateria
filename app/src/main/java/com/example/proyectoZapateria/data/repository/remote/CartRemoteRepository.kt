package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.carrito.CarritoApiService
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CartRemoteRepository @Inject constructor(
    private val api: CarritoApiService
) {

    // Caché en memoria para fallback cuando el backend no esté disponible (usa DTOs remotos)
    private val inMemoryCache: MutableMap<Long, MutableList<CartItemResponse>> = mutableMapOf()

    // Obtener items (Flow) para un cliente: intenta remoto, si falla usa cache en memoria
    fun getCartForCliente(idCliente: Long): Flow<List<CartItemResponse>> {
        return flow {
            try {
                val resp = api.getCart(idCliente)
                if (resp.isSuccessful) {
                    val body: List<CartItemResponse> = resp.body() ?: emptyList()
                    if (body.isNotEmpty()) {
                        inMemoryCache[idCliente] = body.toMutableList()
                        Log.d("CartRemoteRepo", "getCartForCliente: remote returned ${body.size} items for cliente=$idCliente")
                        emit(body)
                        return@flow
                    } else {
                        Log.d("CartRemoteRepo", "getCartForCliente: remote returned EMPTY list for cliente=$idCliente, checking in-memory cache")
                    }
                } else {
                    Log.w("CartRemoteRepo", "getCartForCliente: remote failed (${resp.code()} ${resp.message()}) for cliente=$idCliente")
                }
            } catch (e: Exception) {
                Log.w("CartRemoteRepo", "getCartForCliente: exception when calling remote for cliente=$idCliente: ${e.message}")
            }

            // Fallback: usar caché en memoria
            val l: List<CartItemResponse> = inMemoryCache[idCliente]?.toList() ?: emptyList()
            Log.d("CartRemoteRepo", "getCartForCliente: emitting in-memory list size=${l.size} for cliente=$idCliente")
            emit(l)
        }
    }

    // Añadir o actualizar un item usando CartItemRequest
    suspend fun addOrUpdate(request: CartItemRequest): Long {
        return try {
            val resp = api.addOrUpdate(request)
            if (resp.isSuccessful) {
                val id = resp.body() ?: 0L
                // actualizar caché (encontrar por modeloId+talla)
                val list = inMemoryCache.getOrPut(request.clienteId) { mutableListOf() }
                val idx = list.indexOfFirst { it.modeloId == request.modeloId && it.talla == request.talla }
                val inserted = CartItemResponse(id = id, clienteId = request.clienteId, modeloId = request.modeloId, talla = request.talla, cantidad = request.cantidad, precioUnitario = request.precioUnitario, nombreProducto = request.nombreProducto, fechaCreacion = null, fechaActualizacion = null)
                if (idx >= 0) list[idx] = inserted else list.add(inserted)
                id
            } else {
                Log.w("CartRemoteRepo", "addOrUpdate: remote failed: ${resp.code()} ${resp.message()}")
                // fallback local: generar id temporal
                val tempId = System.currentTimeMillis()
                val list = inMemoryCache.getOrPut(request.clienteId) { mutableListOf() }
                val inserted = CartItemResponse(id = tempId, clienteId = request.clienteId, modeloId = request.modeloId, talla = request.talla, cantidad = request.cantidad, precioUnitario = request.precioUnitario, nombreProducto = request.nombreProducto, fechaCreacion = null, fechaActualizacion = null)
                val idx = list.indexOfFirst { it.modeloId == request.modeloId && it.talla == request.talla }
                if (idx >= 0) list[idx] = inserted else list.add(inserted)
                tempId
            }
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "addOrUpdate: exception: ${e.message}")
            val tempId = System.currentTimeMillis()
            val list = inMemoryCache.getOrPut(request.clienteId) { mutableListOf() }
            val inserted = CartItemResponse(id = tempId, clienteId = request.clienteId, modeloId = request.modeloId, talla = request.talla, cantidad = request.cantidad, precioUnitario = request.precioUnitario, nombreProducto = request.nombreProducto, fechaCreacion = null, fechaActualizacion = null)
            val idx = list.indexOfFirst { it.modeloId == request.modeloId && it.talla == request.talla }
            if (idx >= 0) list[idx] = inserted else list.add(inserted)
            tempId
        }
    }

    // Variante que fuerza el clienteId como query param (backend actualizado acepta esto)
    suspend fun addOrUpdate(request: CartItemRequest, clienteId: Long): Long {
        val reqWithClient = request.copy(clienteId = clienteId)
        return addOrUpdate(reqWithClient)
    }

    suspend fun getCountByCliente(idCliente: Long): Int {
        try {
            val resp = api.count(idCliente)
            if (resp.isSuccessful) return resp.body() ?: 0
        } catch (_: Exception) {
        }
        return inMemoryCache[idCliente]?.size ?: 0
    }

    suspend fun deleteById(clienteId: Long, idCartItem: Long) {
        try {
            val resp = api.deleteItem(clienteId, idCartItem)
            if (resp.isSuccessful) {
                // remover de cache
                inMemoryCache[clienteId]?.removeIf { it.id == idCartItem }
                return
            }
        } catch (_: Exception) {
        }
        inMemoryCache[clienteId]?.removeIf { it.id == idCartItem }
    }

    suspend fun clear(idCliente: Long) {
        try {
            val resp = api.clear(idCliente)
            if (resp.isSuccessful) return
        } catch (_: Exception) {
        }
        inMemoryCache.remove(idCliente)
    }

    suspend fun getItem(idCliente: Long, idModelo: Long, talla: String): CartItemResponse? {
        try {
            val resp = api.getItem(idCliente, idModelo, talla)
            if (resp.isSuccessful) {
                return resp.body()
            }
        } catch (_: Exception) {
        }
        return inMemoryCache[idCliente]?.firstOrNull { it.modeloId == idModelo && it.talla == talla }
    }
}
