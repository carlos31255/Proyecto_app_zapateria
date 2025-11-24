package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.TransportistaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.TransportistaDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TransportistaRemoteRepositoryTest {

    private lateinit var api: TransportistaApiService
    private lateinit var repository: TransportistaRemoteRepository

    @Before
    fun setup() {
        // Mockear Log de Android
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        api = mockk()
        repository = TransportistaRemoteRepository(api)
    }

    // ==========================================
    // Tests de OBTENER TODOS
    // ==========================================

    @Test
    fun `obtenerTodos retorna lista de transportistas`() = runBlocking {
        // Arrange
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

        coEvery { api.obtenerTodos() } returns transportistas

        // Act
        val result = repository.obtenerTodos()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("ABC123", result.getOrNull()!![0].patente)
    }

    @Test
    fun `obtenerTodos retorna lista vacia cuando no hay transportistas`() = runBlocking {
        // Arrange
        coEvery { api.obtenerTodos() } returns emptyList()

        // Act
        val result = repository.obtenerTodos()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodos maneja excepcion`() = runBlocking {
        // Arrange
        coEvery { api.obtenerTodos() } throws Exception("Network error")

        // Act
        val result = repository.obtenerTodos()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER POR ID
    // ==========================================

    @Test
    fun `obtenerPorId retorna transportista cuando existe`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        val transportista = TransportistaDTO(
            idTransportista = idTransportista,
            idPersona = 10L,
            patente = "ABC123",
            tipoVehiculo = "Moto",
            activo = true
        )

        coEvery { api.obtenerPorId(idTransportista) } returns Response.success(transportista)

        // Act
        val result = repository.obtenerPorId(idTransportista)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idTransportista, result.getOrNull()?.idTransportista)
        assertEquals("ABC123", result.getOrNull()?.patente)
    }

    @Test
    fun `obtenerPorId retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idTransportista = 999L
        coEvery { api.obtenerPorId(idTransportista) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.obtenerPorId(idTransportista)

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `obtenerPorId maneja body null`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        coEvery { api.obtenerPorId(idTransportista) } returns Response.success(null)

        // Act
        val result = repository.obtenerPorId(idTransportista)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Empty body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `obtenerPorId maneja excepcion`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        coEvery { api.obtenerPorId(idTransportista) } throws Exception("Network error")

        // Act
        val result = repository.obtenerPorId(idTransportista)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER POR PERSONA
    // ==========================================

    @Test
    fun `obtenerPorPersona retorna transportista cuando existe`() = runBlocking {
        // Arrange
        val idPersona = 10L
        val transportista = TransportistaDTO(
            idTransportista = 1L,
            idPersona = idPersona,
            patente = "ABC123",
            tipoVehiculo = "Moto",
            activo = true
        )

        coEvery { api.obtenerPorPersona(idPersona) } returns Response.success(transportista)

        // Act
        val result = repository.obtenerPorPersona(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idPersona, result.getOrNull()?.idPersona)
    }

    @Test
    fun `obtenerPorPersona retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        coEvery { api.obtenerPorPersona(idPersona) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.obtenerPorPersona(idPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de CREAR
    // ==========================================

    @Test
    fun `crear crea transportista correctamente`() = runBlocking {
        // Arrange
        val nuevoTransportista = TransportistaDTO(
            idTransportista = null,
            idPersona = 10L,
            patente = "DEF456",
            tipoVehiculo = "Camioneta",
            activo = true
        )
        val transportistaCreado = nuevoTransportista.copy(idTransportista = 3L)

        coEvery { api.crear(nuevoTransportista) } returns Response.success(transportistaCreado)

        // Act
        val result = repository.crear(nuevoTransportista)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull()?.idTransportista)
        assertEquals("DEF456", result.getOrNull()?.patente)
    }

    @Test
    fun `crear falla con datos invalidos`() = runBlocking {
        // Arrange
        val transportistaInvalido = TransportistaDTO(
            idTransportista = null,
            idPersona = 10L,
            patente = "",
            tipoVehiculo = "",
            activo = true
        )

        coEvery { api.crear(transportistaInvalido) } returns Response.error(400, "Bad request".toResponseBody())

        // Act
        val result = repository.crear(transportistaInvalido)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `crear maneja excepcion`() = runBlocking {
        // Arrange
        val nuevoTransportista = TransportistaDTO(
            idTransportista = null,
            idPersona = 10L,
            patente = "GHI789",
            tipoVehiculo = "Camioneta",
            activo = true
        )

        coEvery { api.crear(nuevoTransportista) } throws Exception("Network error")

        // Act
        val result = repository.crear(nuevoTransportista)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ACTUALIZAR
    // ==========================================

    @Test
    fun `actualizar actualiza transportista correctamente`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        val transportistaActualizado = TransportistaDTO(
            idTransportista = idTransportista,
            idPersona = 10L,
            patente = "ABC999",
            tipoVehiculo = "Auto",
            activo = true
        )

        coEvery { api.actualizar(idTransportista, transportistaActualizado) } returns Response.success(transportistaActualizado)

        // Act
        val result = repository.actualizar(idTransportista, transportistaActualizado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("ABC999", result.getOrNull()?.patente)
        assertEquals("Auto", result.getOrNull()?.tipoVehiculo)
    }

    @Test
    fun `actualizar falla cuando no existe`() = runBlocking {
        // Arrange
        val idTransportista = 999L
        val transportista = TransportistaDTO(
            idTransportista = idTransportista,
            idPersona = 10L,
            patente = "XXX999",
            tipoVehiculo = "Auto",
            activo = true
        )

        coEvery { api.actualizar(idTransportista, transportista) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.actualizar(idTransportista, transportista)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ELIMINAR
    // ==========================================

    @Test
    fun `eliminar elimina transportista correctamente`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        coEvery { api.eliminar(idTransportista) } returns Response.success(null)

        // Act
        val result = repository.eliminar(idTransportista)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `eliminar falla cuando no existe`() = runBlocking {
        // Arrange
        val idTransportista = 999L
        coEvery { api.eliminar(idTransportista) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.eliminar(idTransportista)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `eliminar maneja excepcion`() = runBlocking {
        // Arrange
        val idTransportista = 1L
        coEvery { api.eliminar(idTransportista) } throws Exception("Network error")

        // Act
        val result = repository.eliminar(idTransportista)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de CASOS EDGE
    // ==========================================

    @Test
    fun `crear transportista con patente duplicada falla`() = runBlocking {
        // Arrange
        val transportista = TransportistaDTO(
            idTransportista = null,
            idPersona = 10L,
            patente = "ABC123",
            tipoVehiculo = "Moto",
            activo = true
        )

        coEvery { api.crear(transportista) } returns Response.error(409, "Conflict".toResponseBody())

        // Act
        val result = repository.crear(transportista)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `obtenerTodos maneja lista grande de transportistas`() = runBlocking {
        // Arrange
        val transportistas = (1..100).map { i ->
            TransportistaDTO(
                idTransportista = i.toLong(),
                idPersona = (i + 100).toLong(),
                patente = "PAT${String.format("%03d", i)}",
                tipoVehiculo = if (i % 2 == 0) "Moto" else "Auto",
                activo = i % 3 != 0
            )
        }

        coEvery { api.obtenerTodos() } returns transportistas

        // Act
        val result = repository.obtenerTodos()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
    }
}

