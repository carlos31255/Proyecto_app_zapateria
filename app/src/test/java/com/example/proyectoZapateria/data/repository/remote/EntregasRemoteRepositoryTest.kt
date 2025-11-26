package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class EntregasRemoteRepositoryTest {

    private val api = mockk<EntregasApiService>()
    private val repository = EntregasRemoteRepository(api)

    @Test
    fun obtenerTodasLasEntregas_retorna_lista_valida() = runBlocking {
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L, idBoleta = 100L, idTransportista = 5L, estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15", fechaEntrega = null, observacion = null,
                numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
            ),
            EntregaDTO(
                idEntrega = 2L, idBoleta = 101L, idTransportista = 5L, estadoEntrega = "completada",
                fechaAsignacion = "2024-01-14", fechaEntrega = "2024-01-15", observacion = "Ok",
                numeroBoleta = "BOL-002", nombreCliente = "Maria", direccionEntrega = "Calle 2"
            )
        )

        coEvery { api.obtenerTodasLasEntregas() } returns Response.success(entregas)

        val result = repository.obtenerTodasLasEntregas()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun obtenerEntregaPorId_retorna_entrega_valida() = runBlocking {
        val entrega = EntregaDTO(
            idEntrega = 1L, idBoleta = 100L, idTransportista = 5L, estadoEntrega = "en_proceso",
            fechaAsignacion = "2024-01-15", fechaEntrega = null, observacion = null,
            numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
        )

        coEvery { api.obtenerEntregaPorId(1L) } returns Response.success(entrega)

        val result = repository.obtenerEntregaPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()!!.idEntrega)
    }

    @Test
    fun obtenerEntregasPorTransportista_retorna_lista_valida() = runBlocking {
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L, idBoleta = 100L, idTransportista = 5L,
                estadoEntrega = "pendiente", fechaAsignacion = "2024-01-15", fechaEntrega = null,
                observacion = null, numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(5L) } returns Response.success(entregas)

        val result = repository.obtenerEntregasPorTransportista(5L)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull()!![0].idTransportista)
    }

    @Test
    fun obtenerEntregasPorEstado_retorna_lista_valida() = runBlocking {
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L, idBoleta = 100L, idTransportista = 5L, estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15", fechaEntrega = null, observacion = null,
                numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
            )
        )

        coEvery { api.obtenerEntregasPorEstado("pendiente") } returns Response.success(entregas)

        val result = repository.obtenerEntregasPorEstado("pendiente")

        assertTrue(result.isSuccess)
        assertEquals("pendiente", result.getOrNull()!![0].estadoEntrega)
    }

    @Test
    fun asignarTransportista_retorna_exito() = runBlocking {
        val responseDto = EntregaDTO(
            idEntrega = 1L, idBoleta = 100L, idTransportista = 5L,
            estadoEntrega = "asignada", fechaAsignacion = "2024-01-15", fechaEntrega = null,
            observacion = null, numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
        )

        coEvery { api.asignarTransportista(1L, 5L) } returns Response.success(responseDto)

        val result = repository.asignarTransportista(1L, 5L)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull()!!.idTransportista)
    }

    @Test
    fun completarEntrega_retorna_exito() = runBlocking {
        val responseDto = EntregaDTO(
            idEntrega = 1L, idBoleta = 100L, idTransportista = 5L,
            estadoEntrega = "completada", fechaAsignacion = "2024-01-15", fechaEntrega = "2024-01-16",
            observacion = "Entregado OK", numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
        )

        coEvery { api.completarEntrega(1L, any()) } returns Response.success(responseDto)

        val result = repository.completarEntrega(1L, "Entregado OK")

        assertTrue(result.isSuccess)
        assertEquals("completada", result.getOrNull()!!.estadoEntrega)
    }

    @Test
    fun cambiarEstadoEntrega_retorna_exito() = runBlocking {
        val responseDto = EntregaDTO(
            idEntrega = 1L, idBoleta = 100L, idTransportista = 5L,
            estadoEntrega = "en_proceso", fechaAsignacion = "2024-01-15", fechaEntrega = null,
            observacion = null, numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
        )

        coEvery { api.cambiarEstadoEntrega(1L, any()) } returns Response.success(responseDto)

        val result = repository.cambiarEstadoEntrega(1L, "en_proceso")

        assertTrue(result.isSuccess)
        assertEquals("en_proceso", result.getOrNull()!!.estadoEntrega)
    }

    @Test
    fun contarEntregasPendientes_retorna_conteo_correcto() = runBlocking {
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L, idBoleta = 100L, idTransportista = 5L, estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15", fechaEntrega = null, observacion = null,
                numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
            ),
            EntregaDTO(
                idEntrega = 2L, idBoleta = 101L, idTransportista = 5L, estadoEntrega = "en_proceso",
                fechaAsignacion = "2024-01-14", fechaEntrega = null, observacion = null,
                numeroBoleta = "BOL-002", nombreCliente = "Maria", direccionEntrega = "Calle 2"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(5L) } returns Response.success(entregas)

        val result = repository.contarEntregasPendientes(5L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun contarEntregasCompletadas_retorna_conteo_correcto() = runBlocking {
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L, idBoleta = 100L, idTransportista = 5L, estadoEntrega = "completada",
                fechaAsignacion = "2024-01-15", fechaEntrega = "2024-01-16", observacion = "Ok",
                numeroBoleta = "BOL-001", nombreCliente = "Juan", direccionEntrega = "Calle 1"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(5L) } returns Response.success(entregas)

        val result = repository.contarEntregasCompletadas(5L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
    }
}
