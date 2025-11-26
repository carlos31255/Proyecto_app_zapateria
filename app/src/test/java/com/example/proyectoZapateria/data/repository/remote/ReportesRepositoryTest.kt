package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.reportes.ReportesApiService
import com.example.proyectoZapateria.data.remote.reportes.dto.*
import com.example.proyectoZapateria.data.remote.ventas.ReportesVentasApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class ReportesRepositoryTest {

    private val api = mockk<ReportesApiService>()
    private val ventasApi = mockk<ReportesVentasApiService>()
    private val repository = ReportesRemoteRepository(api, ventasApi)

    @Test
    fun fetchEstadisticasGenerales_retorna_estadisticas_validas() = runBlocking {
        val estadisticas = EstadisticasGeneralesDTO(
            totalProductos = 100L,
            totalMovimientos = 500L,
            stockTotal = 1000,
            productosStockBajo = 5L,
            productosSinStock = 2L,
            promedioStock = 10.0
        )

        coEvery { api.obtenerEstadisticasGenerales(null, null) } returns Response.success(estadisticas)

        val result = repository.fetchEstadisticasGenerales()

        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull()!!.totalProductos)
    }

    @Test
    fun fetchStockBajo_retorna_lista_valida() = runBlocking {
        val items = listOf(
            StockBajoItemDTO(
                id = 1L,
                productoId = 10L,
                nombre = "Nike Air",
                talla = "42",
                cantidad = 2,
                stockMinimo = 5,
                faltante = -3
            )
        )

        coEvery { api.obtenerStockBajo() } returns Response.success(items)

        val result = repository.fetchStockBajo()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun fetchMovimientosEstadisticas_retorna_estadisticas_validas() = runBlocking {
        val movimientos = MovimientosEstadisticasDTO(
            totalMovimientos = 100L,
            movimientosEntrada = 60L,
            movimientosSalida = 40L,
            totalEntradas = 200,
            totalSalidas = 150,
            saldoNeto = 50
        )

        coEvery { api.obtenerMovimientosEstadisticas() } returns Response.success(movimientos)

        val result = repository.fetchMovimientosEstadisticas()

        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull()!!.totalMovimientos)
    }

    @Test
    fun fetchEstadisticasProducto_retorna_estadisticas_validas() = runBlocking {
        val estadisticas = ProductoEstadisticasDTO(
            productoId = 1L,
            nombreProducto = "Nike Air Max",
            totalTallas = 5,
            stockTotal = 50,
            detalleTallas = emptyList(),
            totalMovimientos = 20L
        )

        coEvery { api.obtenerEstadisticasProducto(1L) } returns Response.success(estadisticas)

        val result = repository.fetchEstadisticasProducto(1L)

        assertTrue(result.isSuccess)
        assertEquals("Nike Air Max", result.getOrNull()!!.nombreProducto)
    }

    @Test
    fun fetchTopStock_retorna_lista_valida() = runBlocking {
        val topProductos = listOf(
            TopProductoDTO(
                productoId = 1L,
                nombre = "Nike Air",
                talla = "42",
                cantidad = 100
            )
        )

        coEvery { api.obtenerTopStock(10) } returns Response.success(topProductos)

        val result = repository.fetchTopStock(10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }
}