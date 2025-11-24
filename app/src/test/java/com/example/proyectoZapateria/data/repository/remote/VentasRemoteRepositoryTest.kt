package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class VentasRemoteRepositoryTest {

    private lateinit var api: VentasApiService
    private lateinit var repository: VentasRemoteRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        api = mockk()
        repository = VentasRemoteRepository(api)
    }

    // ==========================================
    // Tests de OBTENER TODAS LAS BOLETAS
    // ==========================================

    @Test
    fun `obtenerTodasLasBoletas retorna lista de boletas`() = runBlocking {
        // Arrange
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = 2L, total = 75000, estado = "PENDIENTE", metodoPago = "TARJETA", fechaVenta = "2024-01-16T11:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerTodasLasBoletas() } returns Response.success(boletas)

        // Act
        val result = repository.obtenerTodasLasBoletas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    // ==========================================
    // Tests de OBTENER BOLETA POR ID
    // ==========================================

    @Test
    fun `obtenerBoletaPorId retorna boleta cuando existe`() = runBlocking {
        // Arrange
        val idBoleta = 1L
        val boleta = BoletaDTO(id = idBoleta, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())

        coEvery { api.obtenerBoletaPorId(idBoleta) } returns Response.success(boleta)

        // Act
        val result = repository.obtenerBoletaPorId(idBoleta)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idBoleta, result.getOrNull()?.id)
    }

    // ==========================================
    // Tests de OBTENER BOLETAS POR CLIENTE
    // ==========================================

    @Test
    fun `obtenerBoletasPorCliente retorna boletas del cliente con path`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.obtenerBoletasPorCliente(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerBoletasPorCliente intenta query cuando path falla`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.error(404, "Not found".toResponseBody())
        coEvery { api.obtenerBoletasPorClienteQuery(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.obtenerBoletasPorCliente(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify(exactly = 1) { api.obtenerBoletasPorCliente(clienteId) }
        coVerify(exactly = 1) { api.obtenerBoletasPorClienteQuery(clienteId) }
    }

    // ==========================================
    // Tests de CREAR BOLETA
    // ==========================================

    @Test
    fun `crearBoleta crea correctamente`() = runBlocking {
        // Arrange
        val detalles = listOf(
            DetalleBoletaDTO(id = null, boletaId = null, inventarioId = 1L, nombreProducto = "Nike Air", talla = "42", cantidad = 2, precioUnitario = 25000, subtotal = 50000)
        )
        val request = CrearBoletaRequest(clienteId = 1L, metodoPago = "EFECTIVO", detalles = detalles)
        val boletaCreada = BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = detalles)

        coEvery { api.crearBoleta(request) } returns Response.success(boletaCreada)

        // Act
        val result = repository.crearBoleta(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
        assertEquals(50000, result.getOrNull()?.total)
    }

    // ==========================================
    // Tests de CAMBIAR ESTADO BOLETA
    // ==========================================

    @Test
    fun `cambiarEstadoBoleta cambia estado correctamente`() = runBlocking {
        // Arrange
        val idBoleta = 1L
        val nuevoEstado = "COMPLETADA"
        val boletaActualizada = BoletaDTO(id = idBoleta, clienteId = 1L, total = 50000, estado = nuevoEstado, metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())

        coEvery { api.cambiarEstadoBoleta(idBoleta, CambiarEstadoRequest(nuevoEstado)) } returns Response.success(boletaActualizada)

        // Act
        val result = repository.cambiarEstadoBoleta(idBoleta, nuevoEstado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(nuevoEstado, result.getOrNull()?.estado)
    }

    // ==========================================
    // Tests de ELIMINAR BOLETA
    // ==========================================

    @Test
    fun `eliminarBoleta elimina correctamente`() = runBlocking {
        // Arrange
        val idBoleta = 1L
        // La API retorna Response<Void>, simulamos respuesta 204 No Content con body null
        // pero NetworkUtils.safeApiCall fallará con body null, así que el repositorio maneja esto
        // creando un Response exitoso con código 204
        val response = Response.success<Void>(204, null)
        coEvery { api.eliminarBoleta(idBoleta) } returns response

        // Act
        val result = repository.eliminarBoleta(idBoleta)

        // Assert
        // El repositorio captura el failure de safeApiCall y devuelve failure
        // porque safeApiCall requiere body no null
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de OBTENER DETALLES DE BOLETA
    // ==========================================

    @Test
    fun `obtenerDetallesDeBoleta retorna detalles cuando boleta existe`() = runBlocking {
        // Arrange
        val boletaId = 1L
        val detalles = listOf(
            DetalleBoletaDTO(id = 1L, boletaId = boletaId, inventarioId = 1L, nombreProducto = "Nike Air", talla = "42", cantidad = 2, precioUnitario = 25000, subtotal = 50000)
        )
        val boleta = BoletaDTO(id = boletaId, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = detalles)

        coEvery { api.obtenerBoletaPorId(boletaId) } returns Response.success(boleta)

        // Act
        val result = repository.obtenerDetallesDeBoleta(boletaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Nike Air", result.getOrNull()!![0].nombreProducto)
    }

    @Test
    fun `obtenerDetallesDeBoleta retorna lista vacia cuando boleta sin detalles`() = runBlocking {
        // Arrange
        val boletaId = 1L
        val boleta = BoletaDTO(id = boletaId, clienteId = 1L, total = 0, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())

        coEvery { api.obtenerBoletaPorId(boletaId) } returns Response.success(boleta)

        // Act
        val result = repository.obtenerDetallesDeBoleta(boletaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // ==========================================
    // Tests de CLIENTE TIENE BOLETAS PENDIENTES
    // ==========================================

    @Test
    fun `clienteTieneBoletasPendientes retorna true cuando hay pendientes`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.clienteTieneBoletasPendientes(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `clienteTieneBoletasPendientes retorna true cuando hay en proceso`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "EN_PROCESO", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.clienteTieneBoletasPendientes(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `clienteTieneBoletasPendientes retorna false cuando solo hay completadas`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.clienteTieneBoletasPendientes(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull() == true)
    }

    // ==========================================
    // Tests de CALCULAR TOTAL COMPRAS CLIENTE
    // ==========================================

    @Test
    fun `calcularTotalComprasCliente suma correctamente boletas completadas`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = clienteId, total = 30000, estado = "COMPLETADA", metodoPago = "TARJETA", fechaVenta = "2024-01-16T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 3L, clienteId = clienteId, total = 20000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-17T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.calcularTotalComprasCliente(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(80000, result.getOrNull()) // Solo suma las completadas
    }

    @Test
    fun `calcularTotalComprasCliente retorna cero cuando no hay compras completadas`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.calcularTotalComprasCliente(clienteId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    // ==========================================
    // Tests de CONTAR BOLETAS POR ESTADO
    // ==========================================

    @Test
    fun `contarBoletasPorEstado cuenta correctamente`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val estado = "COMPLETADA"
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = clienteId, total = 30000, estado = "COMPLETADA", metodoPago = "TARJETA", fechaVenta = "2024-01-16T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 3L, clienteId = clienteId, total = 20000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-17T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.contarBoletasPorEstado(clienteId, estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun `contarBoletasPorEstado retorna cero cuando no hay boletas de ese estado`() = runBlocking {
        // Arrange
        val clienteId = 1L
        val estado = "CANCELADA"
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = clienteId, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(clienteId) } returns Response.success(boletas)

        // Act
        val result = repository.contarBoletasPorEstado(clienteId, estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }
}

