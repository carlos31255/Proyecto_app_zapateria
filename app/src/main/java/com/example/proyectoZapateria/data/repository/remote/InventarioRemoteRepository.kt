package com.example.proyectoZapateria.data.repository.remote

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
    private val productoApi: ProductoApiService
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

    suspend fun actualizarModelo(id: Long, producto: ProductoDTO): Result<ProductoDTO> {
        val result = safeApiCall { productoApi.actualizarProducto(id, producto) }
        return result
    }

    suspend fun actualizarModeloConImagen(id: Long, productoJson: RequestBody, imagen: MultipartBody.Part?) = safeApiCall { productoApi.actualizarProductoConImagen(id, productoJson, imagen) }

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

    suspend fun crearInventario(dto: InventarioDTO): Result<InventarioDTO> {
        val result = safeApiCall { inventarioApi.crearInventario(dto) }
        return result
    }

    suspend fun actualizarInventario(id: Long, dto: InventarioDTO): Result<InventarioDTO> {
        val result = safeApiCall { inventarioApi.actualizarInventario(id, dto) }
        return result
    }

    suspend fun eliminarStock(id: Long): Result<Void> {
        val result = safeApiCall { inventarioApi.eliminarInventario(id) }
        return result
    }

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

    suspend fun getTallasLogged(): Result<List<TallaDTO>> {
        return getTallas()
    }

    suspend fun getInventarioPorModeloLogged(modeloId: Long): Result<List<InventarioDTO>> = run {
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
                if (res.isSuccess) {
                    val body = res.getOrNull()
                    if (body is Collection<*>) {
                        if (body.isNotEmpty()) {
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
            }
        }

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
        val specific = safeApiCall { productoApi.obtenerTallasPorProducto(productoId) }
        if (specific.isSuccess) {
            val list = specific.getOrNull() ?: emptyList()
            if (list.isNotEmpty()) {
                return Result.success(list)
            }
        }

        val global = safeApiCall { productoApi.obtenerTodasLasTallas() }
        if (global.isSuccess) {
            return global
        }
        return specific.takeIf { it.isFailure } ?: global
    }

    suspend fun getProductoById(id: Long): Result<ProductoDTO> {
        return safeApiCall {
            productoApi.obtenerProductoPorId(id)
        }
    }
}
