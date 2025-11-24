package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
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

    suspend fun actualizarModelo(id: Long, producto: ProductoDTO) = safeApiCall { productoApi.actualizarProducto(id, producto) }

    suspend fun eliminarModelo(id: Long) = safeApiCall { productoApi.eliminarProducto(id) }

    // ==========================================
    // Lógica de STOCK
    // ==========================================

    suspend fun getInventarioPorModelo(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorModelo(modeloId) }
    suspend fun getInventarioPorModeloAlt(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorModeloAlt(modeloId) }
    suspend fun getInventarioPorQuery(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorQuery(modeloId) }

    suspend fun getStockBajo() = safeApiCall { inventarioApi.obtenerStockBajo() }

    suspend fun getInventarioById(id: Long) = safeApiCall { inventarioApi.obtenerInventarioPorId(id) }

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

    suspend fun getTallasLogged(): Result<List<com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO>> {
        Log.d(TAG, "Invocando obtenerTodasLasTallas()")
        val res = getTallas()
        Log.d(TAG, "Resultado obtenerTodasLasTallas: success=${res.isSuccess} err=${res.exceptionOrNull()?.message}")
        return res
    }

    suspend fun getInventarioPorModeloLogged(modeloId: Long) = run {
        Log.d(TAG, "Invocando obtenerInventarioPorModelo modeloId=$modeloId")
        var res = getInventarioPorModelo(modeloId)
        // Si la llamada falló, probar rutas alternativas
        if (res.isFailure) {
            Log.w(TAG, "Ruta principal falló, intentando ruta alternativa modelo/$modeloId. Err=${res.exceptionOrNull()?.message}")
            res = try { getInventarioPorModeloAlt(modeloId) } catch (e: Exception) { Result.failure(e) }
        }

        // Si la llamada fue exitosa pero la lista está vacía, también intentamos endpoints alternativos
        val bodyEmpty = try {
            val body = res.getOrNull()
            (body is Collection<*>) && body.isEmpty()
        } catch (_: Exception) { false }

        if (bodyEmpty) {
            Log.w(TAG, "Ruta principal devolvió lista vacía, intentando ruta alternativa modelo/$modeloId")
            val altRes = try { getInventarioPorModeloAlt(modeloId) } catch (e: Exception) { Result.failure<List<InventarioDTO>>(e) }
            if (altRes.isSuccess && !(altRes.getOrNull() as? Collection<*>)?.isEmpty()!!) {
                res = altRes
            } else {
                Log.w(TAG, "Ruta alternativa también vacía o falló, intentando query con parametro")
                val qRes = try { getInventarioPorQuery(modeloId) } catch (e: Exception) { Result.failure<List<InventarioDTO>>(e) }
                if (qRes.isSuccess && !(qRes.getOrNull() as? Collection<*>)?.isEmpty()!!) {
                    res = qRes
                } else {
                    // dejar res como estaba (vacío) si alternativas no dieron datos
                    Log.w(TAG, "Alternativas no devolvieron datos validos")
                }
            }
        }

        Log.d(TAG, "Resultado obtenerInventarioPorModelo (final): success=${res.isSuccess} err=${res.exceptionOrNull()?.message}")
        res
    }
}
