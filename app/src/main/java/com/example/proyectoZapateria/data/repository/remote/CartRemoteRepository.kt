package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.carrito.CarritoApiService
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemRequest
import com.example.proyectoZapateria.data.remote.carrito.dto.CartItemResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRemoteRepository @Inject constructor(
    private val api: CarritoApiService
) {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val inMemoryCache: MutableMap<Long, MutableList<CartItemResponse>> = mutableMapOf()
    private val cacheFlows: MutableMap<Long, MutableStateFlow<List<CartItemResponse>>> = mutableMapOf()
    private val clientsWithLocalChanges: MutableSet<Long> = mutableSetOf()
    private val lastLocalUpdate: MutableMap<Long, Long> = mutableMapOf()

    private val fetchCounter = AtomicLong(0)
    private val lastRemoteSeq: MutableMap<Long, Long> = mutableMapOf()

    private val GRACE_MS: Long = 5_000

    private fun stateForClient(idCliente: Long): MutableStateFlow<List<CartItemResponse>> {
        return cacheFlows.getOrPut(idCliente) {
            val initial = synchronized(inMemoryCache) { inMemoryCache[idCliente]?.toList() ?: emptyList() }
            MutableStateFlow(initial)
        }
    }
    // Actualizar automaticamente el cache con los datos del servidor
    private fun updateCacheForClient(idCliente: Long, items: List<CartItemResponse>) {
        synchronized(inMemoryCache) {
            inMemoryCache[idCliente] = items.toMutableList()
            val flow = cacheFlows.getOrPut(idCliente) { MutableStateFlow(items.toList()) }
            flow.value = items.toList()
        }
    }

    // Snapshot of cache
    fun getCacheSnapshot(idCliente: Long): List<CartItemResponse> = synchronized(inMemoryCache) {
        inMemoryCache[idCliente]?.toList() ?: cacheFlows[idCliente]?.value ?: emptyList()
    }

    // Main public: flow of cart items for client
    fun getCartForCliente(idCliente: Long): Flow<List<CartItemResponse>> {
        val state = stateForClient(idCliente)

        // launch background fetch (best-effort) to populate cache from remote
        repoScope.launch {
            val seq = fetchCounter.incrementAndGet()
            try {
                val resp = api.getCart(idCliente)
                if (resp.isSuccessful) {
                    val body = resp.body() ?: emptyList()
                    // Do not blindly overwrite local cache if there's a very recent local update
                    val lastLocal = lastLocalUpdate[idCliente]
                    val now = System.currentTimeMillis()
                    val localRecent = lastLocal != null && (now - lastLocal) < GRACE_MS
                    val hasPendingLocal = clientsWithLocalChanges.contains(idCliente)

                    val currentCache = synchronized(inMemoryCache) { inMemoryCache[idCliente]?.toList() ?: emptyList() }
                    if (body.isEmpty() && currentCache.isNotEmpty() && !localRecent && !hasPendingLocal) {
                        Log.d("CartRemoteRepo", "getCartForCliente: skipping remote empty because local cache non-empty for cliente=$idCliente")
                    } else if (body.isEmpty() && (localRecent || hasPendingLocal)) {
                        Log.d("CartRemoteRepo", "getCartForCliente: remote empty but recent local updates or pending changes exist for cliente=$idCliente -> keeping local cache (hadRecentLocal=$localRecent hasPendingLocal=$hasPendingLocal)")
                    } else {
                        updateCacheForClient(idCliente, body)
                        lastRemoteSeq[idCliente] = seq
                        Log.d("CartRemoteRepo", "getCartForCliente: applied remote items size=${body.size} for cliente=$idCliente seq=$seq")
                    }
                } else {
                    Log.w("CartRemoteRepo", "getCartForCliente: remote failed ${resp.code()} ${resp.message()} for cliente=$idCliente")
                }
            } catch (e: Exception) {
                Log.w("CartRemoteRepo", "getCartForCliente: exception fetching remote for cliente=$idCliente: ${e.message}")
            }
        }

        return state.asStateFlow()
    }

    // Add or update: update-only (expects request.id for update). Returns id on success or local-temp id fallback.
    suspend fun addOrUpdate(request: CartItemRequest): Long {
        val client = request.clienteId
        // mark local change
        lastLocalUpdate[client] = System.currentTimeMillis()
        clientsWithLocalChanges.add(client)

        try {
            val resp = api.addOrUpdate(request)
            if (resp.isSuccessful) {
                val body = resp.body() ?: emptyList()
                // hace que el carrito se actualice en el cache
                updateCacheForClient(client, body)
                clientsWithLocalChanges.remove(client)
                //devuelve el id del carrito
                val match = body.firstOrNull { it.modeloId == request.modeloId && (it.talla == request.talla) }
                return match?.id ?: (request.id ?: 0L)
            } else {
                Log.w("CartRemoteRepo", "addOrUpdate: remote failed ${resp.code()} ${resp.message()}")
            }
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "addOrUpdate: exception: ${e.message}")
        }

        // fallback: local update and return synthetic id
        val synthetic = System.currentTimeMillis()
        lastLocalUpdate[client] = System.currentTimeMillis()
        synchronized(inMemoryCache) {
            val list = inMemoryCache.getOrPut(client) { mutableListOf() }
            val idx = list.indexOfFirst { it.id == request.id }
            val inserted = CartItemResponse(
                id = request.id ?: synthetic,
                clienteId = request.clienteId,
                modeloId = request.modeloId,
                talla = request.talla,
                cantidad = request.cantidad,
                precioUnitario = request.precioUnitario,
                nombreProducto = request.nombreProducto,
                fechaCreacion = null,
                fechaActualizacion = null
            )
            if (idx >= 0) list[idx] = inserted else list.add(inserted)
            updateCacheForClient(client, list.toList())
        }
        clientsWithLocalChanges.remove(client)
        return request.id ?: synthetic
    }

    // agregar y retornar el carrito actualizado
    suspend fun addAndReturnCart(request: CartItemRequest, clienteId: Long?): List<CartItemResponse> {
        val idClient = clienteId ?: request.clienteId
        lastLocalUpdate[idClient] = System.currentTimeMillis()
        clientsWithLocalChanges.add(idClient)

        try {
            val resp = api.addToCart(idClient, request)
            if (resp.isSuccessful) {
                val body = resp.body() ?: emptyList()
                // fill missing talla from request if server omitted it
                val fixed = body.map { if (it.talla.isBlank()) it.copy(talla = request.talla) else it }
                updateCacheForClient(idClient, fixed)
                clientsWithLocalChanges.remove(idClient)
                return fixed
            } else {
                Log.w("CartRemoteRepo", "addAndReturnCart: remote failed ${resp.code()} ${resp.message()}")
            }
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "addAndReturnCart: exception: ${e.message}")
        }

        // fallback local add
        val synthetic = System.currentTimeMillis()
        lastLocalUpdate[idClient] = System.currentTimeMillis()
        synchronized(inMemoryCache) {
            val list = inMemoryCache.getOrPut(idClient) { mutableListOf() }
            val inserted = CartItemResponse(
                id = synthetic,
                clienteId = idClient,
                modeloId = request.modeloId,
                talla = request.talla,
                cantidad = request.cantidad,
                precioUnitario = request.precioUnitario,
                nombreProducto = request.nombreProducto,
                fechaCreacion = null,
                fechaActualizacion = null
            )
            val idx = list.indexOfFirst { it.modeloId == request.modeloId && it.talla == request.talla }
            if (idx >= 0) list[idx] = inserted else list.add(inserted)
            updateCacheForClient(idClient, list.toList())
            clientsWithLocalChanges.remove(idClient)
            return list.toList()
        }
    }

    suspend fun getCountByCliente(idCliente: Long): Int {
        try {
            val resp = api.count(idCliente)
            if (resp.isSuccessful) return resp.body() ?: 0
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "getCountByCliente: exception: ${e.message}")
        }
        return synchronized(inMemoryCache) { inMemoryCache[idCliente]?.size ?: cacheFlows[idCliente]?.value?.size ?: 0 }
    }

    suspend fun deleteById(clienteId: Long, idCartItem: Long) {
        lastLocalUpdate[clienteId] = System.currentTimeMillis()
        clientsWithLocalChanges.add(clienteId)
        try {
            val resp = api.deleteItem(clienteId, idCartItem)
            if (resp.isSuccessful) {
                synchronized(inMemoryCache) {
                    val list = inMemoryCache[clienteId]
                    list?.removeIf { it.id == idCartItem }
                    cacheFlows.getOrPut(clienteId) { MutableStateFlow(list?.toList() ?: emptyList()) }.value = list?.toList() ?: emptyList()
                }
                clientsWithLocalChanges.remove(clienteId)
                return
            }
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "deleteById: exception: ${e.message}")
        }
        // fallback local deletion
        synchronized(inMemoryCache) {
            val list = inMemoryCache[clienteId]
            list?.removeIf { it.id == idCartItem }
            cacheFlows.getOrPut(clienteId) { MutableStateFlow(list?.toList() ?: emptyList()) }.value = list?.toList() ?: emptyList()
        }
        clientsWithLocalChanges.remove(clienteId)
    }

    suspend fun clear(idCliente: Long) {
        lastLocalUpdate[idCliente] = System.currentTimeMillis()
        clientsWithLocalChanges.add(idCliente)
        try {
            val resp = api.clear(idCliente)
            if (resp.isSuccessful) {
                synchronized(inMemoryCache) {
                    inMemoryCache.remove(idCliente)
                    cacheFlows.getOrPut(idCliente) { MutableStateFlow(emptyList()) }.value = emptyList()
                }
                clientsWithLocalChanges.remove(idCliente)
                return
            }
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "clear: exception: ${e.message}")
        }
        synchronized(inMemoryCache) {
            inMemoryCache.remove(idCliente)
            cacheFlows.getOrPut(idCliente) { MutableStateFlow(emptyList()) }.value = emptyList()
        }
        clientsWithLocalChanges.remove(idCliente)
    }

    suspend fun getItem(idCliente: Long, idModelo: Long, talla: String): CartItemResponse? {
        try {
            val resp = api.getItem(idCliente, idModelo, talla)
            if (resp.isSuccessful) return resp.body()
        } catch (e: Exception) {
            Log.w("CartRemoteRepo", "getItem: exception: ${e.message}")
        }
        return synchronized(inMemoryCache) { inMemoryCache[idCliente]?.firstOrNull { it.modeloId == idModelo && it.talla == talla } ?: cacheFlows[idCliente]?.value?.firstOrNull { it.modeloId == idModelo && it.talla == talla } }
    }

    fun hasRecentLocalUpdate(idCliente: Long, withinMs: Long): Boolean {
        val last = lastLocalUpdate[idCliente] ?: return false
        return (System.currentTimeMillis() - last) <= withinMs
    }

    // Indica si hay cambios locales no sincronizados para este cliente
    fun hasPendingLocalChanges(idCliente: Long): Boolean {
        return clientsWithLocalChanges.contains(idCliente)
    }
}
