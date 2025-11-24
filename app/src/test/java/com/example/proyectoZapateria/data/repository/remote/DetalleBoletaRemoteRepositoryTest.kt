package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DetalleBoletaRemoteRepositoryTest {

    private lateinit var ventasRemoteRepository: VentasRemoteRepository
    private lateinit var repository: DetalleBoletaRemoteRepository

    @Before
    fun setup() {
        // Mockear Log de Android para tests unitarios
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        ventasRemoteRepository = mockk()
        repository = DetalleBoletaRemoteRepository(ventasRemoteRepository)
    }

    // ==========================================
    // Tests de GET PRODUCTOS POR ID BOLETA
    // ==========================================

    @Test
    fun `getProductos retorna lista de productos cuando la boleta tiene detalles`() = runBlocking {
        // Arrange
        val idBoleta = 1L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Nike Air Max",
                talla = "42",
                cantidad = 2,
                precioUnitario = 50000,
                subtotal = 100000
            ),
            DetalleBoletaDTO(
                id = 2L,
                boletaId = idBoleta,
                inventarioId = 11L,
                nombreProducto = "Adidas Ultraboost",
                talla = "43",
                cantidad = 1,
                precioUnitario = 80000,
                subtotal = 80000
            ),
            DetalleBoletaDTO(
                id = 3L,
                boletaId = idBoleta,
                inventarioId = 12L,
                nombreProducto = "Puma Runner",
                talla = "41",
                cantidad = 3,
                precioUnitario = 40000,
                subtotal = 120000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(3, result.size)

        // Verificar primer producto
        assertEquals("Nike Air Max", result[0].nombreZapato)
        assertEquals("42", result[0].talla)
        assertEquals(2, result[0].cantidad)
        assertEquals("", result[0].marca) // Marca siempre vacía según mapeo

        // Verificar segundo producto
        assertEquals("Adidas Ultraboost", result[1].nombreZapato)
        assertEquals("43", result[1].talla)
        assertEquals(1, result[1].cantidad)

        // Verificar tercer producto
        assertEquals("Puma Runner", result[2].nombreZapato)
        assertEquals("41", result[2].talla)
        assertEquals(3, result[2].cantidad)

        coVerify(exactly = 1) { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) }
    }

    @Test
    fun `getProductos retorna lista vacia cuando la boleta no tiene detalles`() = runBlocking {
        // Arrange
        val idBoleta = 2L
        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(emptyList())

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)

        coVerify(exactly = 1) { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) }
    }

    @Test
    fun `getProductos retorna lista vacia cuando falla obtener detalles remotos`() = runBlocking {
        // Arrange
        val idBoleta = 3L
        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns
            Result.failure(Exception("Error al obtener detalles"))

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)

        // Verificar que se logueó el error
        verify { Log.w("DetalleBoletaRepo", any<String>()) }
    }

    @Test
    fun `getProductos maneja excepcion durante el proceso y retorna lista vacia`() = runBlocking {
        // Arrange
        val idBoleta = 4L
        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } throws
            Exception("Network error")

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)

        // Verificar que se logueó el error
        verify { Log.e("DetalleBoletaRepo", any<String>()) }
    }

    @Test
    fun `getProductos mapea correctamente detalles con campos null`() = runBlocking {
        // Arrange
        val idBoleta = 5L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = null, // null
                talla = null, // null
                cantidad = 1,
                precioUnitario = 50000,
                subtotal = 50000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("", result[0].nombreZapato) // null mapeado a ""
        assertEquals("", result[0].talla) // null mapeado a ""
        assertEquals(1, result[0].cantidad)
        assertEquals("", result[0].marca)
    }

    @Test
    fun `getProductos mapea correctamente cantidad cero`() = runBlocking {
        // Arrange
        val idBoleta = 6L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Zapato Test",
                talla = "40",
                cantidad = 0, // cantidad cero
                precioUnitario = 50000,
                subtotal = 0
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals(0, result[0].cantidad)
    }

    @Test
    fun `getProductos con un solo detalle retorna lista de un elemento`() = runBlocking {
        // Arrange
        val idBoleta = 7L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Único Producto",
                talla = "39",
                cantidad = 5,
                precioUnitario = 30000,
                subtotal = 150000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("Único Producto", result[0].nombreZapato)
        assertEquals("39", result[0].talla)
        assertEquals(5, result[0].cantidad)
    }

    @Test
    fun `getProductos maneja boleta con muchos detalles`() = runBlocking {
        // Arrange
        val idBoleta = 8L
        val detalles = (1..10).map { i ->
            DetalleBoletaDTO(
                id = i.toLong(),
                boletaId = idBoleta,
                inventarioId = (100 + i).toLong(),
                nombreProducto = "Producto $i",
                talla = "${38 + i}",
                cantidad = i,
                precioUnitario = 10000 * i,
                subtotal = 10000 * i * i
            )
        }

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(10, result.size)
        assertEquals("Producto 1", result[0].nombreZapato)
        assertEquals("Producto 10", result[9].nombreZapato)
        assertEquals(1, result[0].cantidad)
        assertEquals(10, result[9].cantidad)
    }

    @Test
    fun `getProductos preserva el orden de los detalles`() = runBlocking {
        // Arrange
        val idBoleta = 9L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 3L,
                boletaId = idBoleta,
                inventarioId = 13L,
                nombreProducto = "Tercero",
                talla = "44",
                cantidad = 3,
                precioUnitario = 30000,
                subtotal = 90000
            ),
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 11L,
                nombreProducto = "Primero",
                talla = "42",
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            ),
            DetalleBoletaDTO(
                id = 2L,
                boletaId = idBoleta,
                inventarioId = 12L,
                nombreProducto = "Segundo",
                talla = "43",
                cantidad = 2,
                precioUnitario = 20000,
                subtotal = 40000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(3, result.size)
        // Verificar que mantiene el orden original
        assertEquals("Tercero", result[0].nombreZapato)
        assertEquals("Primero", result[1].nombreZapato)
        assertEquals("Segundo", result[2].nombreZapato)
    }

    @Test
    fun `getProductos ignora campos no mapeados como precio y subtotal`() = runBlocking {
        // Arrange
        val idBoleta = 10L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Zapato Caro",
                talla = "45",
                cantidad = 1,
                precioUnitario = 999999, // Campo no mapeado
                subtotal = 999999 // Campo no mapeado
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(1, result.size)
        // ProductoDetalle no tiene campos de precio
        assertEquals("Zapato Caro", result[0].nombreZapato)
        assertEquals("45", result[0].talla)
        assertEquals(1, result[0].cantidad)
        assertEquals("", result[0].marca)
    }

    // ==========================================
    // Tests de GET PRODUCTOS POR NUMERO BOLETA
    // ==========================================

    @Test
    fun `getProductosPorNumeroBoleta retorna lista vacia siempre`() = runBlocking {
        // Arrange
        val numeroBoleta = "BOL-2024-001"

        // Act
        val result = repository.getProductosPorNumeroBoleta(numeroBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)

        // Verificar que se logueó el warning
        verify { Log.w("DetalleBoletaRepo", match<String> { it.contains("no implementado") && it.contains(numeroBoleta) }) }
    }

    @Test
    fun `getProductosPorNumeroBoleta con numero vacio retorna lista vacia`() = runBlocking {
        // Arrange
        val numeroBoleta = ""

        // Act
        val result = repository.getProductosPorNumeroBoleta(numeroBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getProductosPorNumeroBoleta con numero null-like retorna lista vacia`() = runBlocking {
        // Arrange
        val numeroBoleta = "null"

        // Act
        val result = repository.getProductosPorNumeroBoleta(numeroBoleta).first()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getProductosPorNumeroBoleta con diferentes formatos retorna lista vacia`() = runBlocking {
        // Arrange
        val numerosBoleta = listOf(
            "BOL-001",
            "12345",
            "BOLETA-2024-12-24",
            "B001",
            "factura-123"
        )

        // Act & Assert
        numerosBoleta.forEach { numero ->
            val result = repository.getProductosPorNumeroBoleta(numero).first()
            assertTrue("Debería retornar vacío para número: $numero", result.isEmpty())
        }

        // Verificar que se logueó para cada llamada (al menos el número de llamadas)
        verify(atLeast = numerosBoleta.size) { Log.w("DetalleBoletaRepo", any<String>()) }
    }

    // ==========================================
    // Tests de COMPORTAMIENTO FLOW
    // ==========================================

    @Test
    fun `getProductos emite una sola vez con datos validos`() = runBlocking {
        // Arrange
        val idBoleta = 11L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Test",
                talla = "40",
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        var emissionCount = 0
        repository.getProductos(idBoleta).collect { productos ->
            emissionCount++
            assertEquals(1, productos.size)
        }

        // Assert
        assertEquals(1, emissionCount) // Solo debe emitir una vez
    }

    @Test
    fun `getProductos emite una sola vez con lista vacia en caso de error`() = runBlocking {
        // Arrange
        val idBoleta = 12L
        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns
            Result.failure(Exception("Error"))

        // Act
        var emissionCount = 0
        repository.getProductos(idBoleta).collect { productos ->
            emissionCount++
            assertTrue(productos.isEmpty())
        }

        // Assert
        assertEquals(1, emissionCount) // Solo debe emitir una vez
    }

    @Test
    fun `getProductosPorNumeroBoleta emite una sola vez`() = runBlocking {
        // Arrange
        val numeroBoleta = "BOL-001"

        // Act
        var emissionCount = 0
        repository.getProductosPorNumeroBoleta(numeroBoleta).collect { productos ->
            emissionCount++
            assertTrue(productos.isEmpty())
        }

        // Assert
        assertEquals(1, emissionCount) // Solo debe emitir una vez
    }

    // ==========================================
    // Tests de MAPEO DE DATOS
    // ==========================================

    @Test
    fun `getProductos mapea correctamente todos los campos de DetalleBoletaDTO a ProductoDetalle`() = runBlocking {
        // Arrange
        val idBoleta = 13L
        val detalle = DetalleBoletaDTO(
            id = 1L,
            boletaId = idBoleta,
            inventarioId = 10L,
            nombreProducto = "Nike Jordan",
            talla = "44",
            cantidad = 7,
            precioUnitario = 120000,
            subtotal = 840000
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(listOf(detalle))

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        val producto = result[0]
        // Campos mapeados
        assertEquals("Nike Jordan", producto.nombreZapato)
        assertEquals("44", producto.talla)
        assertEquals(7, producto.cantidad)
        // Campo siempre vacío
        assertEquals("", producto.marca)
    }

    @Test
    fun `getProductos verifica que marca siempre es string vacio`() = runBlocking {
        // Arrange
        val idBoleta = 14L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Producto 1",
                talla = "40",
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            ),
            DetalleBoletaDTO(
                id = 2L,
                boletaId = idBoleta,
                inventarioId = 11L,
                nombreProducto = "Producto 2",
                talla = "41",
                cantidad = 2,
                precioUnitario = 20000,
                subtotal = 40000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        // Verificar que todos los productos tienen marca vacía
        assertTrue(result.all { it.marca == "" })
        result.forEach { producto ->
            assertEquals("", producto.marca)
        }
    }

    @Test
    fun `getProductos maneja tallas con diferentes formatos`() = runBlocking {
        // Arrange
        val idBoleta = 15L
        val detalles = listOf(
            DetalleBoletaDTO(
                id = 1L,
                boletaId = idBoleta,
                inventarioId = 10L,
                nombreProducto = "Zapato A",
                talla = "42", // Número simple
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            ),
            DetalleBoletaDTO(
                id = 2L,
                boletaId = idBoleta,
                inventarioId = 11L,
                nombreProducto = "Zapato B",
                talla = "M", // Letra
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            ),
            DetalleBoletaDTO(
                id = 3L,
                boletaId = idBoleta,
                inventarioId = 12L,
                nombreProducto = "Zapato C",
                talla = "8.5", // Con decimal
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            ),
            DetalleBoletaDTO(
                id = 4L,
                boletaId = idBoleta,
                inventarioId = 13L,
                nombreProducto = "Zapato D",
                talla = "EU 44", // Con prefijo
                cantidad = 1,
                precioUnitario = 10000,
                subtotal = 10000
            )
        )

        coEvery { ventasRemoteRepository.obtenerDetallesDeBoleta(idBoleta) } returns Result.success(detalles)

        // Act
        val result = repository.getProductos(idBoleta).first()

        // Assert
        assertEquals(4, result.size)
        assertEquals("42", result[0].talla)
        assertEquals("M", result[1].talla)
        assertEquals("8.5", result[2].talla)
        assertEquals("EU 44", result[3].talla)
    }
}

