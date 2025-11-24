package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductoRemoteRepositoryTest {

    private lateinit var inventarioRemoteRepository: InventarioRemoteRepository
    private lateinit var repository: ProductoRemoteRepository

    @Before
    fun setup() {
        inventarioRemoteRepository = mockk()
        repository = ProductoRemoteRepository(inventarioRemoteRepository)
    }

    // ==========================================
    // Tests de GET ALL MODELOS
    // ==========================================

    @Test
    fun `getAllModelos retorna lista de productos cuando hay datos`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(
                id = 1L,
                nombre = "Air Max 90",
                marcaId = 1L,
                descripcion = "Zapatilla deportiva",
                precioUnitario = 50000,
                imagenUrl = "http://example.com/nike.jpg"
            ),
            ProductoDTO(
                id = 2L,
                nombre = "Ultraboost",
                marcaId = 2L,
                descripcion = "Running shoes",
                precioUnitario = 75000,
                imagenUrl = "http://example.com/adidas.jpg"
            )
        )

        coEvery { inventarioRemoteRepository.getModelos() } returns Result.success(productos)

        // Act
        val result = repository.getAllModelos().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Air Max 90", result[0].nombre)
        assertEquals("Ultraboost", result[1].nombre)

        coVerify(exactly = 1) { inventarioRemoteRepository.getModelos() }
    }

    @Test
    fun `getAllModelos retorna lista vacia cuando falla la llamada`() = runBlocking {
        // Arrange
        coEvery { inventarioRemoteRepository.getModelos() } returns Result.failure(Exception("Network error"))

        // Act
        val result = repository.getAllModelos().first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllModelos retorna lista vacia cuando getOrNull es null`() = runBlocking {
        // Arrange
        coEvery { inventarioRemoteRepository.getModelos() } returns Result.success(emptyList())

        // Act
        val result = repository.getAllModelos().first()

        // Assert
        assertTrue(result.isEmpty())
    }

    // ==========================================
    // Tests de GET MODELOS ACTIVOS
    // ==========================================

    @Test
    fun `getModelosActivos retorna mismos datos que getAllModelos`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(
                id = 1L,
                nombre = "Air Max 90",
                marcaId = 1L,
                descripcion = "Zapatilla deportiva",
                precioUnitario = 50000,
                imagenUrl = "http://example.com/nike.jpg"
            )
        )

        coEvery { inventarioRemoteRepository.getModelos() } returns Result.success(productos)

        // Act
        val resultAll = repository.getAllModelos().first()
        val resultActivos = repository.getModelosActivos().first()

        // Assert
        assertEquals(resultAll, resultActivos)
        assertEquals(1, resultActivos.size)
    }

    // ==========================================
    // Tests de GET MODELOS BY MARCA
    // ==========================================

    @Test
    fun `getModelosByMarca retorna productos de la marca especificada`() = runBlocking {
        // Arrange
        val idMarca = 1L
        val productos = listOf(
            ProductoDTO(
                id = 1L,
                nombre = "Air Max 90",
                marcaId = idMarca,
                descripcion = "Zapatilla deportiva",
                precioUnitario = 50000,
                imagenUrl = "http://example.com/nike.jpg"
            ),
            ProductoDTO(
                id = 2L,
                nombre = "Air Force 1",
                marcaId = idMarca,
                descripcion = "Classic sneakers",
                precioUnitario = 45000,
                imagenUrl = "http://example.com/nike2.jpg"
            )
        )

        coEvery { inventarioRemoteRepository.getModelosByMarca(idMarca) } returns Result.success(productos)

        // Act
        val result = repository.getModelosByMarca(idMarca).first()

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.marcaId == idMarca })

        coVerify(exactly = 1) { inventarioRemoteRepository.getModelosByMarca(idMarca) }
    }

    @Test
    fun `getModelosByMarca retorna lista vacia cuando no hay productos de esa marca`() = runBlocking {
        // Arrange
        val idMarca = 99L
        coEvery { inventarioRemoteRepository.getModelosByMarca(idMarca) } returns Result.success(emptyList())

        // Act
        val result = repository.getModelosByMarca(idMarca).first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getModelosByMarca retorna lista vacia cuando falla la llamada`() = runBlocking {
        // Arrange
        val idMarca = 1L
        coEvery { inventarioRemoteRepository.getModelosByMarca(idMarca) } returns Result.failure(Exception("Error"))

        // Act
        val result = repository.getModelosByMarca(idMarca).first()

        // Assert
        assertTrue(result.isEmpty())
    }

    // ==========================================
    // Tests de GET MODELOS ACTIVOS BY MARCA
    // ==========================================

    @Test
    fun `getModelosActivosByMarca retorna mismos datos que getModelosByMarca`() = runBlocking {
        // Arrange
        val idMarca = 1L
        val productos = listOf(
            ProductoDTO(
                id = 1L,
                nombre = "Air Max 90",
                marcaId = idMarca,
                descripcion = "Zapatilla deportiva",
                precioUnitario = 50000,
                imagenUrl = "http://example.com/nike.jpg"
            )
        )

        coEvery { inventarioRemoteRepository.getModelosByMarca(idMarca) } returns Result.success(productos)

        // Act
        val resultNormal = repository.getModelosByMarca(idMarca).first()
        val resultActivos = repository.getModelosActivosByMarca(idMarca).first()

        // Assert
        assertEquals(resultNormal, resultActivos)
    }

    // ==========================================
    // Tests de GET MODELO BY ID
    // ==========================================

    @Test
    fun `getModeloById retorna producto cuando existe`() = runBlocking {
        // Arrange
        val idModelo = 1L
        val producto = ProductoDTO(
            id = idModelo,
            nombre = "Air Max 90",
            marcaId = 1L,
            descripcion = "Zapatilla deportiva",
            precioUnitario = 50000,
            imagenUrl = "http://example.com/nike.jpg"
        )

        coEvery { inventarioRemoteRepository.getModeloById(idModelo) } returns Result.success(producto)

        // Act
        val result = repository.getModeloById(idModelo)

        // Assert
        assertNotNull(result)
        assertEquals(idModelo, result?.id)
        assertEquals("Air Max 90", result?.nombre)

        coVerify(exactly = 1) { inventarioRemoteRepository.getModeloById(idModelo) }
    }

    @Test
    fun `getModeloById retorna null cuando no existe`() = runBlocking {
        // Arrange
        val idModelo = 999L
        coEvery { inventarioRemoteRepository.getModeloById(idModelo) } returns Result.failure(Exception("Not found"))

        // Act
        val result = repository.getModeloById(idModelo)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getModeloById retorna null cuando falla la llamada`() = runBlocking {
        // Arrange
        val idModelo = 1L
        coEvery { inventarioRemoteRepository.getModeloById(idModelo) } returns Result.failure(Exception("Error"))

        // Act
        val result = repository.getModeloById(idModelo)

        // Assert
        assertNull(result)
    }

    // ==========================================
    // Tests de CASOS EDGE
    // ==========================================

    @Test
    fun `getAllModelos maneja multiples marcas correctamente`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(id = 1L, nombre = "Nike Air", marcaId = 1L, descripcion = "Nike shoes", precioUnitario = 40000, imagenUrl = "url1"),
            ProductoDTO(id = 2L, nombre = "Adidas Ultra", marcaId = 2L, descripcion = "Adidas shoes", precioUnitario = 50000, imagenUrl = "url2"),
            ProductoDTO(id = 3L, nombre = "Puma Speed", marcaId = 3L, descripcion = "Puma shoes", precioUnitario = 35000, imagenUrl = "url3")
        )

        coEvery { inventarioRemoteRepository.getModelos() } returns Result.success(productos)

        // Act
        val result = repository.getAllModelos().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals(setOf(1L, 2L, 3L), result.map { it.marcaId }.toSet())
    }

    @Test
    fun `getModelosByMarca con marca sin productos retorna lista vacia`() = runBlocking {
        // Arrange
        val idMarca = 999L
        coEvery { inventarioRemoteRepository.getModelosByMarca(idMarca) } returns Result.success(emptyList())

        // Act
        val result = repository.getModelosByMarca(idMarca).first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Flow emite una sola vez con getAllModelos`() = runBlocking {
        // Arrange
        val productos = listOf(
            ProductoDTO(id = 1L, nombre = "Test", marcaId = 1L, descripcion = "Desc", precioUnitario = 30000, imagenUrl = "url")
        )
        coEvery { inventarioRemoteRepository.getModelos() } returns Result.success(productos)

        // Act
        var emissionCount = 0
        repository.getAllModelos().collect {
            emissionCount++
            assertEquals(1, it.size)
        }

        // Assert
        assertEquals(1, emissionCount)
    }
}

