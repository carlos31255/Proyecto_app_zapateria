package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class InventarioRemoteRepository @Inject constructor(
    private val inventarioApi: InventarioApiService,
    private val productoApi: ProductoApiService // Inyectamos el nuevo servicio
) {

    // ==========================================
    // Lógica de CATÁLOGO
    // ==========================================

    suspend fun getMarcas(): Result<List<MarcaDTO>> = safeApiCall { productoApi.obtenerTodasLasMarcas() }

    suspend fun getMarcaById(id: Long): Result<MarcaDTO> = safeApiCall { productoApi.obtenerMarcaPorId(id) }

    suspend fun getTallas(): Result<List<TallaDTO>> = safeApiCall { productoApi.obtenerTodasLasTallas() }

    suspend fun getModelos(): Result<List<ProductoDTO>> = safeApiCall { productoApi.obtenerTodosLosProductos() }

    suspend fun getModeloById(id: Long): Result<ProductoDTO> = safeApiCall { productoApi.obtenerProductoPorId(id) }

    suspend fun getModelosByMarca(marcaId: Long): Result<List<ProductoDTO>> = safeApiCall { productoApi.obtenerProductosPorMarca(marcaId) }

    suspend fun searchModelos(query: String): Result<List<ProductoDTO>> = safeApiCall { productoApi.buscarProductos(query) }

    suspend fun crearModelo(producto: ProductoDTO) = safeApiCall { productoApi.crearProducto(producto) }

    suspend fun crearModeloConImagen(productoJson: RequestBody, imagen: MultipartBody.Part?) = safeApiCall { productoApi.crearProductoMultipart(productoJson, imagen) }

    suspend fun actualizarModelo(id: Long, producto: ProductoDTO) = safeApiCall { productoApi.actualizarProducto(id, producto) }

    suspend fun eliminarModelo(id: Long) = safeApiCall { productoApi.eliminarProducto(id) }

    // ==========================================
    // Lógica de STOCK
    // ==========================================

    // Probar varias rutas sin prefijo /api hasta encontrar una que funcione.
    suspend fun getInventarioPorProducto(productoId: Long): Result<List<InventarioDTO>> {
        // Intentar la ruta principal sin prefijo
        val tryNoApi = safeApiCall { inventarioApi.obtenerInventarioPorModelo(productoId) }
        if (tryNoApi.isSuccess) return tryNoApi
        // Intentar la ruta alternativa sin prefijo (/inventario/modelo/{id})
        val tryAltNoApi = safeApiCall { inventarioApi.obtenerInventarioPorModeloAlt_NoApi(productoId) }
        if (tryAltNoApi.isSuccess) return tryAltNoApi
        // Intentar query general sin prefijo
        return safeApiCall { inventarioApi.obtenerInventarioPorQuery(productoId) }
    }

    suspend fun getInventarioPorModelo(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorModelo(modeloId) }
    // Usar la variante sin prefijo: /inventario/modelo/{modeloId}
    suspend fun getInventarioPorModeloAlt(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorModeloAlt_NoApi(modeloId) }
    suspend fun getInventarioPorQuery(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorQuery(modeloId) }

    suspend fun getInventarioById(id: Long): Result<InventarioDTO> {
        // Usar la ruta sin prefijo únicamente
        return safeApiCall { inventarioApi.obtenerInventarioPorId(id) }
    }

    suspend fun crearInventario(dto: InventarioDTO) = safeApiCall { inventarioApi.crearInventario(dto) }

    suspend fun actualizarInventario(id: Long, dto: InventarioDTO) = safeApiCall { inventarioApi.actualizarInventario(id, dto) }

    suspend fun eliminarStock(id: Long) = safeApiCall { inventarioApi.eliminarInventario(id) }

    // --- Helper ---
    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                if (response.isSuccessful && response.code() == 204) {
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(true as T)
                }
                val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                val code = response.code()
                val msg = response.message()
                Result.failure(Exception("Error " + code + ": " + msg + " - body=" + (errorBody ?: "<empty>")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "InventarioRemoteRepo"
    }

    suspend fun getTallasLogged(): Result<List<TallaDTO>> {
        Log.d(TAG, "Invocando obtenerTodasLasTallas()")
        val res = getTallas()
        Log.d(TAG, "Resultado obtenerTodasLasTallas: success=${res.isSuccess} err=${res.exceptionOrNull()?.message}")
        return res
    }

    //  usar los helpers del propio repo
    suspend fun getInventarioPorModeloLogged(modeloId: Long): Result<List<InventarioDTO>> = run {
        Log.d(TAG, "Invocando obtenerInventarioPorModelo modeloId=$modeloId")

        val attempts = listOf<suspend () -> Result<List<InventarioDTO>>>(
            { getInventarioPorModelo(modeloId) },
            { getInventarioPorModeloAlt(modeloId) },
            { getInventarioPorQuery(modeloId) },
            { getInventarioPorProducto(modeloId) }
        )

        var lastFailure: Result<List<InventarioDTO>>? = null
        for (attempt in attempts) {
            try {
                val res = attempt()
                val size = (res.getOrNull() as? Collection<*>)?.size ?: -1
                Log.d(TAG, "Intento ruta: success=${res.isSuccess} size=$size err=${res.exceptionOrNull()?.message}")
                if (res.isSuccess) {
                    val body = res.getOrNull()
                    if (body is Collection<*>) {
                        if (body.isNotEmpty()) {
                            Log.d(TAG, "Resultado obtenerInventarioPorModelo (final): success=true size=${body.size}")
                            return@run res
                        } else {
                            lastFailure = res
                        }
                    } else {
                        return@run res
                    }
                } else {
                    lastFailure = res
                }
            } catch (e: Exception) {
                lastFailure = Result.failure(e)
                Log.w(TAG, "Attempt threw: ${e.message}")
            }
        }

        Log.d(TAG, "Resultado obtenerInventarioPorModelo (final): success=${lastFailure?.isSuccess} err=${lastFailure?.exceptionOrNull()?.message}")
        lastFailure ?: Result.failure(Exception("No se pudo ejecutar llamadas"))
    }

    suspend fun obtenerImagenProducto(id: Long): Result<ByteArray?> {
        return try {
            val response = productoApi.obtenerImagenProducto(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                try {
                    val bytes = body.bytes()
                    Result.success(bytes)
                } catch (e: Exception) {
                    Result.failure(Exception("Error leyendo bytes de la imagen: ${e.message}"))
                }
            } else {
                // si no hay imagen o error 404, retornar success con null para indicar ausencia
                if (response.code() == 404 || response.code() == 204) {
                    Result.success(null)
                } else {
                    val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    Result.failure(Exception("Error ${response.code()}: ${response.message()} - body=${err ?: "<empty>"}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTallasPorProducto(productoId: Long): Result<List<TallaDTO>> {
        Log.d(TAG, "Invocando obtenerTallasPorProducto productoId=$productoId")
        // Primero intentar ruta específica del producto
        val specific = safeApiCall { productoApi.obtenerTallasPorProducto(productoId) }
        if (specific.isSuccess) {
            val list = specific.getOrNull() ?: emptyList()
            if (list.isNotEmpty()) {
                Log.d(TAG, "obtenerTallasPorProducto: éxito específico count=${list.size}")
                return Result.success(list)
            } else {
                Log.w(TAG, "obtenerTallasPorProducto: ruta específica devolvió lista vacía, intentaremos tallas globales")
            }
        } else {
            Log.w(TAG, "obtenerTallasPorProducto: fallo ruta específica: ${specific.exceptionOrNull()?.message}")
        }

        // Fallback: obtener todas las tallas globales
        val global = safeApiCall { productoApi.obtenerTodasLasTallas() }
        if (global.isSuccess) {
            Log.d(TAG, "getTallasPorProducto: fallback a tallas globales success=${global.getOrNull()?.size ?: 0}")
            return global
        }
        Log.w(TAG, "getTallasPorProducto: fallback también falló: ${global.exceptionOrNull()?.message}")
        // devolver el primer error (specific) si fue failure, sino el global
        return specific.takeIf { it.isFailure } ?: global
    }

    suspend fun getProductoById(id: Long): Result<ProductoDTO> {
        return safeApiCall {
            productoApi.obtenerProductoPorId(id)
        }
    }
}
