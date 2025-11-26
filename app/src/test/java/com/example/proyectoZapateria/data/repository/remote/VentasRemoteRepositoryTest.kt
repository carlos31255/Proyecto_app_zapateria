package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.ventas.VentasApiService
import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.remote.ventas.dto.CambiarEstadoRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.CrearBoletaRequest
import com.example.proyectoZapateria.data.remote.ventas.dto.DetalleBoletaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class VentasRemoteRepositoryTest {

    private val api = mockk<VentasApiService>()
    private val repository = VentasRemoteRepository(api)

    @Test
    fun obtenerTodasLasBoletas_retorna_lista_valida() = runBlocking {
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = 2L, total = 75000, estado = "PENDIENTE", metodoPago = "TARJETA", fechaVenta = "2024-01-16T11:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerTodasLasBoletas() } returns Response.success(boletas)

        val result = repository.obtenerTodasLasBoletas()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun obtenerBoletaPorId_retorna_boleta_valida() = runBlocking {
        val boleta = BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())

        coEvery { api.obtenerBoletaPorId(1L) } returns Response.success(boleta)

        val result = repository.obtenerBoletaPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
    }

    @Test
    fun obtenerBoletasPorCliente_retorna_lista_valida() = runBlocking {
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(1L) } returns Response.success(boletas)

        val result = repository.obtenerBoletasPorCliente(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun crearBoleta_retorna_exito() = runBlocking {
        val detalles = listOf(
            DetalleBoletaDTO(id = null, boletaId = null, inventarioId = 1L, nombreProducto = "Nike Air", talla = "42", cantidad = 2, precioUnitario = 25000, subtotal = 50000)
        )
        val request = CrearBoletaRequest(clienteId = 1L, metodoPago = "EFECTIVO", detalles = detalles)
        val boletaCreada = BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "PENDIENTE", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = detalles)

        coEvery { api.crearBoleta(request) } returns Response.success(boletaCreada)

        val result = repository.crearBoleta(request)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
        assertEquals(50000, result.getOrNull()?.total)
    }

    @Test
    fun cambiarEstadoBoleta_retorna_exito() = runBlocking {
        val boletaActualizada = BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList())

        coEvery { api.cambiarEstadoBoleta(1L, CambiarEstadoRequest("COMPLETADA")) } returns Response.success(boletaActualizada)

        val result = repository.cambiarEstadoBoleta(1L, "COMPLETADA")

        assertTrue(result.isSuccess)
        assertEquals("COMPLETADA", result.getOrNull()?.estado)
    }

    @Test
    fun obtenerDetallesDeBoleta_retorna_lista_valida() = runBlocking {
        val detalles = listOf(
            DetalleBoletaDTO(id = 1L, boletaId = 1L, inventarioId = 1L, nombreProducto = "Nike Air", talla = "42", cantidad = 2, precioUnitario = 25000, subtotal = 50000)
        )
        val boleta = BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = detalles)

        coEvery { api.obtenerBoletaPorId(1L) } returns Response.success(boleta)

        val result = repository.obtenerDetallesDeBoleta(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Nike Air", result.getOrNull()!![0].nombreProducto)
    }

    @Test
    fun calcularTotalComprasCliente_retorna_total_correcto() = runBlocking {
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = 1L, total = 30000, estado = "COMPLETADA", metodoPago = "TARJETA", fechaVenta = "2024-01-16T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(1L) } returns Response.success(boletas)

        val result = repository.calcularTotalComprasCliente(1L)

        assertTrue(result.isSuccess)
        assertEquals(80000, result.getOrNull())
    }

    @Test
    fun contarBoletasPorEstado_retorna_conteo_correcto() = runBlocking {
        val boletas = listOf(
            BoletaDTO(id = 1L, clienteId = 1L, total = 50000, estado = "COMPLETADA", metodoPago = "EFECTIVO", fechaVenta = "2024-01-15T10:00:00Z", detalles = emptyList()),
            BoletaDTO(id = 2L, clienteId = 1L, total = 30000, estado = "COMPLETADA", metodoPago = "TARJETA", fechaVenta = "2024-01-16T10:00:00Z", detalles = emptyList())
        )

        coEvery { api.obtenerBoletasPorCliente(1L) } returns Response.success(boletas)

        val result = repository.contarBoletasPorEstado(1L, "COMPLETADA")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }
}

