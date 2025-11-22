package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
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

    suspend fun getModelos(): Result<List<ModeloZapatoDTO>> = safeApiCall { productoApi.obtenerTodosLosModelos() }

    suspend fun getModeloById(id: Long): Result<ModeloZapatoDTO> = safeApiCall { productoApi.obtenerModeloPorId(id) }

    suspend fun getModelosByMarca(marcaId: Long): Result<List<ModeloZapatoDTO>> = safeApiCall { productoApi.obtenerModelosPorMarca(marcaId) }

    suspend fun searchModelos(query: String): Result<List<ModeloZapatoDTO>> = safeApiCall { productoApi.buscarModelos(query) }

    suspend fun crearModelo(modelo: ModeloZapatoDTO) = safeApiCall { productoApi.crearModelo(modelo) }

    suspend fun actualizarModelo(id: Long, modelo: ModeloZapatoDTO) = safeApiCall { productoApi.actualizarModelo(id, modelo) }

    suspend fun eliminarModelo(id: Long) = safeApiCall { productoApi.eliminarModelo(id) }

    // ==========================================
    // Lógica de STOCK
    // ==========================================

    suspend fun getInventarioPorModelo(modeloId: Long) = safeApiCall { inventarioApi.obtenerInventarioPorModelo(modeloId) }

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
}
