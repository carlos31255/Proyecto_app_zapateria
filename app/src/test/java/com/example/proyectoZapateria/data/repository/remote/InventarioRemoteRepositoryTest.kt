package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ModeloZapatoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class InventarioRemoteRepositoryTest {

    @Test
    fun getMarcas_retorna_lista_valida() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val marcas = listOf(MarcaDTO(id = 1L, nombre = "MarcaA", descripcion = "desc"))
        coEvery { productoApi.obtenerTodasLasMarcas() } returns Response.success(marcas)

        // Act
        val result = repo.getMarcas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("MarcaA", result.getOrNull()!![0].nombre)
    }

    @Test
    fun getModeloById_retorna_modelo_valido() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val modelo = ModeloZapatoDTO(id = 10L, nombre = "ZapatoX", marcaId = 2L, descripcion = "", precioUnitario = 1500, imagenUrl = null)
        coEvery { productoApi.obtenerModeloPorId(10L) } returns Response.success(modelo)

        // Act
        val result = repo.getModeloById(10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()!!.id)
        assertEquals("ZapatoX", result.getOrNull()!!.nombre)
    }

    @Test
    fun getTallas_retorna_lista_valida() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val tallas = listOf(TallaDTO(id = 1L, valor = "38", descripcion = null))
        coEvery { productoApi.obtenerTodasLasTallas() } returns Response.success(tallas)

        // Act
        val result = repo.getTallas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("38", result.getOrNull()!![0].valor)
    }

    @Test
    fun getInventarioPorModelo_retorna_lista_valida() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val inv = listOf(
            InventarioDTO(id = 5L, productoId = 10L, nombre = "ZapatoX", talla = "42", cantidad = 3, stockMinimo = 1, modeloId = 10L, tallaId = 2L)
        )
        coEvery { inventarioApi.obtenerInventarioPorModelo(10L) } returns Response.success(inv)

        // Act
        val result = repo.getInventarioPorModelo(10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(3, result.getOrNull()!![0].cantidad)
    }

    @Test
    fun crearInventario_retorna_inventario_creado() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val request = InventarioDTO(id = null, productoId = 11L, nombre = "Nuevo", talla = "M", cantidad = 5, stockMinimo = 2, modeloId = 11L, tallaId = 3L)
        val responseDto = request.copy(id = 99L)
        coEvery { inventarioApi.crearInventario(request) } returns Response.success(responseDto)

        // Act
        val result = repo.crearInventario(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(99L, result.getOrNull()!!.id)
    }

    @Test
    fun searchModelos_retorna_lista_valida() = runBlocking {
        // Arrange
        val productoApi = mockk<ProductoApiService>()
        val inventarioApi = mockk<InventarioApiService>()
        val repo = InventarioRemoteRepository(inventarioApi, productoApi)

        val modelos = listOf(ModeloZapatoDTO(id = 21L, nombre = "Nike Air", marcaId = 3L, descripcion = null, precioUnitario = 1000, imagenUrl = null))
        coEvery { productoApi.buscarModelos("Nike") } returns Response.success(modelos)

        // Act
        val result = repo.searchModelos("Nike")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!![0].nombre.contains("Nike"))
    }

}
