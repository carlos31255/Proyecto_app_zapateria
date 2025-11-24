package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.InventarioApiService
import com.example.proyectoZapateria.data.remote.inventario.ProductoApiService
import com.example.proyectoZapateria.data.remote.inventario.dto.InventarioDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.TallaDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class InventarioRemoteRepositoryTest {

    private lateinit var productoApi: ProductoApiService
    private lateinit var inventarioApi: InventarioApiService
    private lateinit var repository: InventarioRemoteRepository

    @Before
    fun setup() {
        productoApi = mockk()
        inventarioApi = mockk()
        repository = InventarioRemoteRepository(inventarioApi, productoApi)
    }

    // ==========================================
    // Tests de MARCAS
    // ==========================================

    @Test
    fun `getMarcas retorna lista valida cuando la API responde exitosamente`() = runBlocking {
        // Arrange
        val marcas = listOf(
            MarcaDTO(id = 1L, nombre = "Nike", descripcion = "Marca deportiva"),
            MarcaDTO(id = 2L, nombre = "Adidas", descripcion = "Marca deportiva")
        )
        coEvery { productoApi.obtenerTodasLasMarcas() } returns Response.success(marcas)

        // Act
        val result = repository.getMarcas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Nike", result.getOrNull()!![0].nombre)
        assertEquals("Adidas", result.getOrNull()!![1].nombre)
        coVerify(exactly = 1) { productoApi.obtenerTodasLasMarcas() }
    }

    @Test
    fun `getMarcas retorna error cuando la API falla`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerTodasLasMarcas() } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.getMarcas()

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        coVerify(exactly = 1) { productoApi.obtenerTodasLasMarcas() }
    }

    @Test
    fun `getMarcaById retorna marca valida cuando existe`() = runBlocking {
        // Arrange
        val marca = MarcaDTO(id = 1L, nombre = "Nike", descripcion = "Marca deportiva")
        coEvery { productoApi.obtenerMarcaPorId(1L) } returns Response.success(marca)

        // Act
        val result = repository.getMarcaById(1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()!!.id)
        assertEquals("Nike", result.getOrNull()!!.nombre)
        coVerify(exactly = 1) { productoApi.obtenerMarcaPorId(1L) }
    }

    @Test
    fun `getMarcaById retorna error cuando no existe`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerMarcaPorId(999L) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.getMarcaById(999L)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de TALLAS
    // ==========================================

    @Test
    fun `getTallas retorna lista valida cuando la API responde exitosamente`() = runBlocking {
        // Arrange
        val tallas = listOf(
            TallaDTO(id = 1L, valor = "38", descripcion = "Talla 38"),
            TallaDTO(id = 2L, valor = "42", descripcion = "Talla 42")
        )
        coEvery { productoApi.obtenerTodasLasTallas() } returns Response.success(tallas)

        // Act
        val result = repository.getTallas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("38", result.getOrNull()!![0].valor)
        assertEquals("42", result.getOrNull()!![1].valor)
        coVerify(exactly = 1) { productoApi.obtenerTodasLasTallas() }
    }

    @Test
    fun `getTallas retorna error cuando la API falla`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerTodasLasTallas() } returns Response.error(
            500,
            "Internal server error".toResponseBody()
        )

        // Act
        val result = repository.getTallas()

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de PRODUCTOS/MODELOS
    // ==========================================

    @Test
    fun `getModelos retorna lista valida cuando la API responde exitosamente`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(id = 1L, nombre = "Air Max", marcaId = 1L, descripcion = "Zapatillas deportivas", precioUnitario = 1500, imagenUrl = null),
            ProductoDTO(id = 2L, nombre = "Ultraboost", marcaId = 2L, descripcion = "Zapatillas running", precioUnitario = 2000, imagenUrl = null)
        )
        coEvery { productoApi.obtenerTodosLosProductos() } returns Response.success(productos)

        // Act
        val result = repository.getModelos()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Air Max", result.getOrNull()!![0].nombre)
    }

    @Test
    fun `getModeloById retorna producto valido cuando existe`() = runBlocking {
        // Arrange
        val producto = ProductoDTO(
            id = 10L,
            nombre = "ZapatoX",
            marcaId = 2L,
            descripcion = "Descripción del zapato",
            precioUnitario = 1500,
            imagenUrl = null
        )
        coEvery { productoApi.obtenerProductoPorId(10L) } returns Response.success(producto)

        // Act
        val result = repository.getModeloById(10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()!!.id)
        assertEquals("ZapatoX", result.getOrNull()!!.nombre)
        assertEquals(1500, result.getOrNull()!!.precioUnitario)
        coVerify(exactly = 1) { productoApi.obtenerProductoPorId(10L) }
    }

    @Test
    fun `getModeloById retorna error cuando no existe`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerProductoPorId(999L) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.getModeloById(999L)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `getModelosByMarca retorna lista valida de productos de una marca`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(id = 1L, nombre = "Air Max", marcaId = 1L, descripcion = null, precioUnitario = 1500, imagenUrl = null),
            ProductoDTO(id = 2L, nombre = "Air Force", marcaId = 1L, descripcion = null, precioUnitario = 1200, imagenUrl = null)
        )
        coEvery { productoApi.obtenerProductosPorMarca(1L) } returns Response.success(productos)

        // Act
        val result = repository.getModelosByMarca(1L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.marcaId == 1L })
    }

    @Test
    fun `searchModelos retorna productos que coinciden con la busqueda`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(id = 21L, nombre = "Nike Air Max", marcaId = 3L, descripcion = null, precioUnitario = 1000, imagenUrl = null),
            ProductoDTO(id = 22L, nombre = "Nike Revolution", marcaId = 3L, descripcion = null, precioUnitario = 800, imagenUrl = null)
        )
        coEvery { productoApi.buscarProductos("Nike") } returns Response.success(productos)

        // Act
        val result = repository.searchModelos("Nike")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.nombre.contains("Nike") })
        coVerify(exactly = 1) { productoApi.buscarProductos("Nike") }
    }

    @Test
    fun `searchModelos retorna lista vacia cuando no hay coincidencias`() = runBlocking {
        // Arrange
        coEvery { productoApi.buscarProductos("ZapatoInexistente") } returns Response.success(emptyList())

        // Act
        val result = repository.searchModelos("ZapatoInexistente")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `crearModelo crea producto exitosamente`() = runBlocking {
        // Arrange
        val nuevoProducto = ProductoDTO(
            id = 0L,
            nombre = "Nuevo Modelo",
            marcaId = 1L,
            descripcion = "Descripción",
            precioUnitario = 1500,
            imagenUrl = null
        )
        val productoCreado = nuevoProducto.copy(id = 100L)
        coEvery { productoApi.crearProducto(nuevoProducto) } returns Response.success(productoCreado)

        // Act
        val result = repository.crearModelo(nuevoProducto)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull()!!.id)
        coVerify(exactly = 1) { productoApi.crearProducto(nuevoProducto) }
    }

    @Test
    fun `actualizarModelo actualiza producto exitosamente`() = runBlocking {
        // Arrange
        val productoActualizado = ProductoDTO(
            id = 10L,
            nombre = "Modelo Actualizado",
            marcaId = 1L,
            descripcion = "Nueva descripción",
            precioUnitario = 1800,
            imagenUrl = null
        )
        coEvery { productoApi.actualizarProducto(10L, productoActualizado) } returns Response.success(productoActualizado)

        // Act
        val result = repository.actualizarModelo(10L, productoActualizado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Modelo Actualizado", result.getOrNull()!!.nombre)
        assertEquals(1800, result.getOrNull()!!.precioUnitario)
    }

    @Test
    fun `eliminarModelo elimina producto exitosamente`() = runBlocking {
        // Arrange
        val emptyResponse = Response.success<Void>(204, null)
        coEvery { productoApi.eliminarProducto(10L) } returns emptyResponse

        // Act
        val result = repository.eliminarModelo(10L)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { productoApi.eliminarProducto(10L) }
    }

    // ==========================================
    // Tests de INVENTARIO
    // ==========================================

    @Test
    fun `getInventarioPorModelo retorna lista valida de inventario`() = runBlocking {
        // Arrange
        val inventario = listOf(
            InventarioDTO(
                id = 5L,
                productoId = 10L,
                nombre = "ZapatoX",
                talla = "42",
                cantidad = 3,
                stockMinimo = 1,
                modeloId = 10L,
                tallaId = 2L
            ),
            InventarioDTO(
                id = 6L,
                productoId = 10L,
                nombre = "ZapatoX",
                talla = "43",
                cantidad = 5,
                stockMinimo = 2,
                modeloId = 10L,
                tallaId = 3L
            )
        )
        coEvery { inventarioApi.obtenerInventarioPorModelo(10L) } returns Response.success(inventario)

        // Act
        val result = repository.getInventarioPorModelo(10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(3, result.getOrNull()!![0].cantidad)
        assertEquals("42", result.getOrNull()!![0].talla)
        coVerify(exactly = 1) { inventarioApi.obtenerInventarioPorModelo(10L) }
    }

    @Test
    fun `getInventarioPorModelo retorna error cuando falla la API`() = runBlocking {
        // Arrange
        coEvery { inventarioApi.obtenerInventarioPorModelo(999L) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.getInventarioPorModelo(999L)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `getInventarioById retorna inventario especifico`() = runBlocking {
        // Arrange
        val inventario = InventarioDTO(
            id = 5L,
            productoId = 10L,
            nombre = "ZapatoX",
            talla = "42",
            cantidad = 3,
            stockMinimo = 1,
            modeloId = 10L,
            tallaId = 2L
        )
        coEvery { inventarioApi.obtenerInventarioPorId(5L) } returns Response.success(inventario)

        // Act
        val result = repository.getInventarioById(5L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull()!!.id)
        assertEquals(3, result.getOrNull()!!.cantidad)
    }

    @Test
    fun `getStockBajo retorna productos con stock bajo`() = runBlocking {
        // Arrange
        val stockBajo = listOf(
            InventarioDTO(
                id = 1L,
                productoId = 5L,
                nombre = "Producto1",
                talla = "40",
                cantidad = 2,
                stockMinimo = 5,
                modeloId = 5L,
                tallaId = 1L
            )
        )
        coEvery { inventarioApi.obtenerStockBajo() } returns Response.success(stockBajo)

        // Act
        val result = repository.getStockBajo()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!![0].cantidad < result.getOrNull()!![0].stockMinimo)
    }

    @Test
    fun `crearInventario crea nuevo inventario exitosamente`() = runBlocking {
        // Arrange
        val nuevoInventario = InventarioDTO(
            id = null,
            productoId = 11L,
            nombre = "Nuevo Zapato",
            talla = "42",
            cantidad = 10,
            stockMinimo = 2,
            modeloId = 11L,
            tallaId = 3L
        )
        val inventarioCreado = nuevoInventario.copy(id = 99L)
        coEvery { inventarioApi.crearInventario(nuevoInventario) } returns Response.success(inventarioCreado)

        // Act
        val result = repository.crearInventario(nuevoInventario)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(99L, result.getOrNull()!!.id)
        assertEquals(10, result.getOrNull()!!.cantidad)
        coVerify(exactly = 1) { inventarioApi.crearInventario(nuevoInventario) }
    }

    @Test
    fun `crearInventario retorna error cuando falla la creacion`() = runBlocking {
        // Arrange
        val nuevoInventario = InventarioDTO(
            id = null,
            productoId = 11L,
            nombre = "Nuevo Zapato",
            talla = "42",
            cantidad = 10,
            stockMinimo = 2,
            modeloId = 11L,
            tallaId = 3L
        )
        coEvery { inventarioApi.crearInventario(nuevoInventario) } returns Response.error(
            400,
            "Bad request".toResponseBody()
        )

        // Act
        val result = repository.crearInventario(nuevoInventario)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `actualizarInventario actualiza inventario exitosamente`() = runBlocking {
        // Arrange
        val inventarioActualizado = InventarioDTO(
            id = 5L,
            productoId = 10L,
            nombre = "ZapatoX",
            talla = "42",
            cantidad = 15,
            stockMinimo = 3,
            modeloId = 10L,
            tallaId = 2L
        )
        coEvery { inventarioApi.actualizarInventario(5L, inventarioActualizado) } returns Response.success(inventarioActualizado)

        // Act
        val result = repository.actualizarInventario(5L, inventarioActualizado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(15, result.getOrNull()!!.cantidad)
        assertEquals(3, result.getOrNull()!!.stockMinimo)
    }

    @Test
    fun `eliminarStock elimina inventario exitosamente`() = runBlocking {
        // Arrange
        val emptyResponse = Response.success<Void>(204, null)
        coEvery { inventarioApi.eliminarInventario(5L) } returns emptyResponse

        // Act
        val result = repository.eliminarStock(5L)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { inventarioApi.eliminarInventario(5L) }
    }

    // ==========================================
    // Tests de MANEJO DE ERRORES Y CASOS EDGE
    // ==========================================

    @Test
    fun `safeApiCall maneja excepciones de red correctamente`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerTodasLasMarcas() } throws Exception("Network error")

        // Act
        val result = repository.getMarcas()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") ?: false)
    }

    @Test
    fun `safeApiCall retorna success cuando recibe codigo 204 sin body`() = runBlocking {
        // Arrange
        val emptyResponse = Response.success<Void>(204, null)
        coEvery { inventarioApi.eliminarInventario(5L) } returns emptyResponse

        // Act
        val result = repository.eliminarStock(5L)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getInventarioPorModelo retorna lista vacia cuando no hay stock`() = runBlocking {
        // Arrange
        coEvery { inventarioApi.obtenerInventarioPorModelo(10L) } returns Response.success(emptyList())

        // Act
        val result = repository.getInventarioPorModelo(10L)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `getMarcas maneja error 500 correctamente`() = runBlocking {
        // Arrange
        coEvery { productoApi.obtenerTodasLasMarcas() } returns Response.error(
            500,
            "Internal server error".toResponseBody()
        )

        // Act
        val result = repository.getMarcas()

        // Assert
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(errorMessage.contains("500"))
    }

    @Test
    fun `crearInventario con datos invalidos retorna error`() = runBlocking {
        // Arrange
        val inventarioInvalido = InventarioDTO(
            id = null,
            productoId = -1L,
            nombre = "",
            talla = "",
            cantidad = -5,
            stockMinimo = -1,
            modeloId = -1L,
            tallaId = -1L
        )
        coEvery { inventarioApi.crearInventario(inventarioInvalido) } returns Response.error(
            422,
            "Unprocessable entity".toResponseBody()
        )

        // Act
        val result = repository.crearInventario(inventarioInvalido)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `actualizarModelo con id inexistente retorna error`() = runBlocking {
        // Arrange
        val producto = ProductoDTO(
            id = 999L,
            nombre = "No existe",
            marcaId = 1L,
            descripcion = null,
            precioUnitario = 1000,
            imagenUrl = null
        )
        coEvery { productoApi.actualizarProducto(999L, producto) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.actualizarModelo(999L, producto)

        // Assert
        assertTrue(result.isFailure)
    }
}

