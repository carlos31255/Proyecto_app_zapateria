package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.usuario.TransportistaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class TransportistaRemoteRepositoryTest {

    private val api = mockk<TransportistaApiService>()
    private val repository = TransportistaRemoteRepository(api)

    @Test
    fun obtenerTodos_retorna_lista_valida() = runBlocking {
        val transportistas = listOf(
            TransportistaDTO(
                idTransportista = 1L,
                idPersona = 10L,
                patente = "ABC123",
                tipoVehiculo = "Moto",
                activo = true
            ),
            TransportistaDTO(
                idTransportista = 2L,
                idPersona = 11L,
                patente = "XYZ789",
                tipoVehiculo = "Auto",
                activo = true
            )
        )

        coEvery { api.obtenerTodos() } returns Response.success(transportistas)

        val result = repository.obtenerTodos()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("ABC123", result.getOrNull()!![0].patente)
    }

    @Test
    fun obtenerPorId_retorna_transportista_valido() = runBlocking {
        val transportista = TransportistaDTO(
            idTransportista = 1L,
            idPersona = 10L,
            patente = "ABC123",
            tipoVehiculo = "Moto",
            activo = true
        )

        coEvery { api.obtenerPorId(1L) } returns Response.success(transportista)

        val result = repository.obtenerPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals("ABC123", result.getOrNull()?.patente)
    }

    @Test
    fun obtenerPorPersona_retorna_transportista_valido() = runBlocking {
        val transportista = TransportistaDTO(
            idTransportista = 1L,
            idPersona = 10L,
            patente = "ABC123",
            tipoVehiculo = "Moto",
            activo = true
        )

        coEvery { api.obtenerPorPersona(10L) } returns Response.success(transportista)

        val result = repository.obtenerPorPersona(10L)

        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()?.idPersona)
    }

    @Test
    fun crear_retorna_exito() = runBlocking {
        val nuevoTransportista = TransportistaDTO(
            idTransportista = null,
            idPersona = 10L,
            patente = "DEF456",
            tipoVehiculo = "Camioneta",
            activo = true
        )
        val transportistaCreado = nuevoTransportista.copy(idTransportista = 3L)

        coEvery { api.crear(nuevoTransportista) } returns Response.success(transportistaCreado)

        val result = repository.crear(nuevoTransportista)

        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull()?.idTransportista)
        assertEquals("DEF456", result.getOrNull()?.patente)
    }

    @Test
    fun actualizar_retorna_exito() = runBlocking {
        val transportistaActualizado = TransportistaDTO(
            idTransportista = 1L,
            idPersona = 10L,
            patente = "ABC999",
            tipoVehiculo = "Auto",
            activo = true
        )

        coEvery { api.actualizar(1L, transportistaActualizado) } returns Response.success(transportistaActualizado)

        val result = repository.actualizar(1L, transportistaActualizado)

        assertTrue(result.isSuccess)
        assertEquals("ABC999", result.getOrNull()?.patente)
    }

    @Test
    fun eliminar_retorna_exito() = runBlocking {
        coEvery { api.eliminar(1L) } returns Response.success(null)

        val result = repository.eliminar(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
}

