package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.ventas.dto.BoletaDTO
import com.example.proyectoZapateria.data.repository.ReportesRepository
import com.example.proyectoZapateria.domain.model.FiltroReporte
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ReportesRepositoryTest {

    private lateinit var ventasRepository: VentasRemoteRepository
    private lateinit var repository: ReportesRepository

    @Before
    fun setup() {
        ventasRepository = mockk()
        repository = ReportesRepository(ventasRepository)
    }

    // ==========================================
    // Tests de GENERAR REPORTE ANUAL
    // ==========================================

    @Test
    fun `generarReporte anual retorna reporte correctamente`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2024-08-20T11:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 3L,
                clienteId = 3L,
                total = 30000,
                estado = "CANCELADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-09-10T12:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(2, reporte.numeroVentasRealizadas) // Solo completadas
        Assert.assertEquals(1, reporte.numeroVentasCanceladas)
        Assert.assertEquals(125000, reporte.ingresosTotal) // 50000 + 75000
        Assert.assertEquals(0, reporte.detallesVentas.size) // Sin mes, no hay detalles
    }

    // ==========================================
    // Tests de GENERAR REPORTE MENSUAL
    // ==========================================

    @Test
    fun `generarReporte mensual retorna reporte con detalles`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = 6)

        // Crear fecha en junio 2024
        val cal = Calendar.getInstance()
        cal.set(2024, 5, 15, 10, 0, 0) // Mes 5 = Junio (0-indexed)
        cal.set(Calendar.MILLISECOND, 0)
        val fechaJunio = "${cal.get(Calendar.YEAR)}-${
            String.format(
                "%02d",
                cal.get(Calendar.MONTH) + 1
            )
        }-${String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))}T10:00:00Z"

        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = fechaJunio,
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 30000,
                estado = "PENDIENTE",
                metodoPago = "TARJETA",
                fechaVenta = fechaJunio,
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(
            2,
            reporte.numeroVentasRealizadas
        ) // COMPLETADA y PENDIENTE (no canceladas)
        Assert.assertEquals(0, reporte.numeroVentasCanceladas)
        Assert.assertEquals(80000, reporte.ingresosTotal)
        Assert.assertEquals(2, reporte.detallesVentas.size) // Con mes, incluye detalles
    }

    @Test
    fun `generarReporte mensual filtra boletas fuera del mes`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = 6)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2024-07-20T11:00:00Z",
                detalles = emptyList()
            ), // Julio, no debe contar
            BoletaDTO(
                id = 3L,
                clienteId = 3L,
                total = 30000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-05-10T12:00:00Z",
                detalles = emptyList()
            )  // Mayo, no debe contar
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.numeroVentasRealizadas) // Solo la de junio
        Assert.assertEquals(50000, reporte.ingresosTotal)
    }

    // ==========================================
    // Tests de CANCELADAS
    // ==========================================

    @Test
    fun `generarReporte cuenta correctamente ventas canceladas`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "CANCELADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "CANCELADA",
                metodoPago = "TARJETA",
                fechaVenta = "2024-08-20T11:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 3L,
                clienteId = 3L,
                total = 30000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-09-10T12:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.numeroVentasRealizadas)
        Assert.assertEquals(2, reporte.numeroVentasCanceladas)
        Assert.assertEquals(30000, reporte.ingresosTotal) // Solo cuenta las no canceladas
    }

    @Test
    fun `generarReporte maneja estado cancelada en minusculas y mayusculas`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "cancelada",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "Cancelada",
                metodoPago = "TARJETA",
                fechaVenta = "2024-08-20T11:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 3L,
                clienteId = 3L,
                total = 30000,
                estado = "CANCELADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-09-10T12:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(0, reporte.numeroVentasRealizadas)
        Assert.assertEquals(3, reporte.numeroVentasCanceladas) // Todas son canceladas
        Assert.assertEquals(0, reporte.ingresosTotal)
    }

    // ==========================================
    // Tests de CASOS ESPECIALES
    // ==========================================

    @Test
    fun `generarReporte retorna reporte vacio cuando no hay boletas`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(emptyList())

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(0, reporte.numeroVentasRealizadas)
        Assert.assertEquals(0, reporte.numeroVentasCanceladas)
        Assert.assertEquals(0, reporte.ingresosTotal)
    }

    @Test
    fun `generarReporte maneja error al obtener boletas`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.failure(Exception("Network error"))

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `generarReporte maneja fechas invalidas`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "fecha-invalida",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.numeroVentasRealizadas) // Solo cuenta la valida
        Assert.assertEquals(75000, reporte.ingresosTotal)
    }

    // ==========================================
    // Tests de DETALLES VENTAS
    // ==========================================

    @Test
    fun `generarReporte mensual incluye detalles correctos`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = 6)
        val boletas = listOf(
            BoletaDTO(
                id = 100L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.detallesVentas.size)

        val detalle = reporte.detallesVentas[0]
        Assert.assertEquals("B-100", detalle.numeroBoleta)
        Assert.assertEquals("Cliente #1", detalle.nombreCliente)
        Assert.assertEquals(50000, detalle.montoTotal)
        Assert.assertEquals("realizada", detalle.estado)
    }

    @Test
    fun `generarReporte mensual marca canceladas en detalles`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = 6)
        val boletas = listOf(
            BoletaDTO(
                id = 100L,
                clienteId = 1L,
                total = 50000,
                estado = "CANCELADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-06-15T10:00:00Z",
                detalles = emptyList()
            )
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.detallesVentas.size)
        Assert.assertEquals("cancelada", reporte.detallesVentas[0].estado)
    }

    // ==========================================
    // Tests de RANGOS DE FECHAS
    // ==========================================

    @Test
    fun `generarReporte diciembre incluye solo boletas de diciembre`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = 12)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-12-15T10:00:00Z",
                detalles = emptyList()
            ),
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2025-01-05T11:00:00Z",
                detalles = emptyList()
            ) // Enero siguiente año
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(1, reporte.numeroVentasRealizadas) // Solo diciembre
        Assert.assertEquals(50000, reporte.ingresosTotal)
    }

    @Test
    fun `generarReporte anual incluye todo el anio`() = runBlocking {
        // Arrange
        val filtro = FiltroReporte(anio = 2024, mes = null)
        val boletas = listOf(
            BoletaDTO(
                id = 1L,
                clienteId = 1L,
                total = 50000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2024-01-15T10:00:00Z",
                detalles = emptyList()
            ), // Enero
            BoletaDTO(
                id = 2L,
                clienteId = 2L,
                total = 75000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2024-12-31T23:59:59Z",
                detalles = emptyList()
            ), // Diciembre
            BoletaDTO(
                id = 3L,
                clienteId = 3L,
                total = 30000,
                estado = "COMPLETADA",
                metodoPago = "EFECTIVO",
                fechaVenta = "2023-12-31T10:00:00Z",
                detalles = emptyList()
            ), // Año anterior
            BoletaDTO(
                id = 4L,
                clienteId = 4L,
                total = 40000,
                estado = "COMPLETADA",
                metodoPago = "TARJETA",
                fechaVenta = "2025-01-01T10:00:00Z",
                detalles = emptyList()
            )  // Año siguiente
        )

        coEvery { ventasRepository.obtenerTodasLasBoletas() } returns Result.success(boletas)

        // Act
        val result = repository.generarReporte(filtro)

        // Assert
        Assert.assertTrue(result.isSuccess)
        val reporte = result.getOrNull()!!
        Assert.assertEquals(2, reporte.numeroVentasRealizadas) // Solo 2024
        Assert.assertEquals(125000, reporte.ingresosTotal)
    }
}