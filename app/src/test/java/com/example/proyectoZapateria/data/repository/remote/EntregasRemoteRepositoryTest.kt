package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.entregas.EntregasApiService
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class EntregasRemoteRepositoryTest {

    private lateinit var api: EntregasApiService
    private lateinit var repository: EntregasRemoteRepository

    @Before
    fun setup() {
        // Mockear Log de Android para tests unitarios
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        api = mockk()
        repository = EntregasRemoteRepository(api)
    }

    // ==========================================
    // Tests de OBTENER TODAS LAS ENTREGAS
    // ==========================================

    @Test
    fun `obtenerTodasLasEntregas retorna lista valida cuando hay entregas`() = runBlocking {
        // Arrange
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = 5L,
                estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            ),
            EntregaDTO(
                idEntrega = 2L,
                idBoleta = 101L,
                idTransportista = 5L,
                estadoEntrega = "completada",
                fechaAsignacion = "2024-01-14",
                fechaEntrega = "2024-01-15",
                observacion = "Entregado correctamente",
                numeroBoleta = "BOL-002",
                nombreCliente = "María González",
                direccionEntrega = "Av. Principal 456"
            )
        )

        coEvery { api.obtenerTodasLasEntregas() } returns Response.success(entregas)

        // Act
        val result = repository.obtenerTodasLasEntregas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("pendiente", result.getOrNull()!![0].estadoEntrega)
        assertEquals("completada", result.getOrNull()!![1].estadoEntrega)

        coVerify(exactly = 1) { api.obtenerTodasLasEntregas() }
    }

    @Test
    fun `obtenerTodasLasEntregas retorna lista vacia cuando no hay entregas`() = runBlocking {
        // Arrange
        coEvery { api.obtenerTodasLasEntregas() } returns Response.success(emptyList())

        // Act
        val result = repository.obtenerTodasLasEntregas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodasLasEntregas maneja error de red`() = runBlocking {
        // Arrange
        coEvery { api.obtenerTodasLasEntregas() } throws Exception("Network error")

        // Act
        val result = repository.obtenerTodasLasEntregas()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER ENTREGA POR ID
    // ==========================================

    @Test
    fun `obtenerEntregaPorId retorna entrega cuando existe`() = runBlocking {
        // Arrange
        val idEntrega = 1L
        val entrega = EntregaDTO(
            idEntrega = idEntrega,
            idBoleta = 100L,
            idTransportista = 5L,
            estadoEntrega = "en_proceso",
            fechaAsignacion = "2024-01-15",
            fechaEntrega = null,
            observacion = null,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.obtenerEntregaPorId(idEntrega) } returns Response.success(entrega)

        // Act
        val result = repository.obtenerEntregaPorId(idEntrega)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idEntrega, result.getOrNull()!!.idEntrega)
        assertEquals("en_proceso", result.getOrNull()!!.estadoEntrega)

        coVerify(exactly = 1) { api.obtenerEntregaPorId(idEntrega) }
    }

    @Test
    fun `obtenerEntregaPorId retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idEntrega = 999L
        coEvery { api.obtenerEntregaPorId(idEntrega) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.obtenerEntregaPorId(idEntrega)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de OBTENER ENTREGAS POR TRANSPORTISTA
    // ==========================================

    @Test
    fun `obtenerEntregasPorTransportista retorna lista de entregas del transportista`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = transportistaId,
                estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            ),
            EntregaDTO(
                idEntrega = 2L,
                idBoleta = 101L,
                idTransportista = transportistaId,
                estadoEntrega = "en_proceso",
                fechaAsignacion = "2024-01-14",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-002",
                nombreCliente = "María González",
                direccionEntrega = "Av. Principal 456"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(entregas)

        // Act
        val result = repository.obtenerEntregasPorTransportista(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.idTransportista == transportistaId })

        verify { Log.d("EntregasRemoteRepository", "obtenerEntregasPorTransportista: transportistaId=$transportistaId") }
    }

    @Test
    fun `obtenerEntregasPorTransportista retorna lista vacia cuando no hay entregas`() = runBlocking {
        // Arrange
        val transportistaId = 10L
        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(emptyList())

        // Act
        val result = repository.obtenerEntregasPorTransportista(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // ==========================================
    // Tests de OBTENER ENTREGAS POR ESTADO
    // ==========================================

    @Test
    fun `obtenerEntregasPorEstado retorna entregas pendientes`() = runBlocking {
        // Arrange
        val estado = "pendiente"
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = 5L,
                estadoEntrega = estado,
                fechaAsignacion = "2024-01-15",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            )
        )

        coEvery { api.obtenerEntregasPorEstado(estado) } returns Response.success(entregas)

        // Act
        val result = repository.obtenerEntregasPorEstado(estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(estado, result.getOrNull()!![0].estadoEntrega)

        verify { Log.d("EntregasRemoteRepository", "obtenerEntregasPorEstado: estado=$estado") }
    }

    @Test
    fun `obtenerEntregasPorEstado retorna entregas completadas`() = runBlocking {
        // Arrange
        val estado = "completada"
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 2L,
                idBoleta = 101L,
                idTransportista = 5L,
                estadoEntrega = estado,
                fechaAsignacion = "2024-01-14",
                fechaEntrega = "2024-01-15",
                observacion = "Entregado",
                numeroBoleta = "BOL-002",
                nombreCliente = "María González",
                direccionEntrega = "Av. Principal 456"
            )
        )

        coEvery { api.obtenerEntregasPorEstado(estado) } returns Response.success(entregas)

        // Act
        val result = repository.obtenerEntregasPorEstado(estado)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.all { it.estadoEntrega == estado })
    }

    @Test
    fun `obtenerEntregasPorEstado retorna entregas en proceso`() = runBlocking {
        // Arrange
        val estado = "en_proceso"
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 3L,
                idBoleta = 102L,
                idTransportista = 6L,
                estadoEntrega = estado,
                fechaAsignacion = "2024-01-16",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-003",
                nombreCliente = "Carlos López",
                direccionEntrega = "Calle Sur 789"
            )
        )

        coEvery { api.obtenerEntregasPorEstado(estado) } returns Response.success(entregas)

        // Act
        val result = repository.obtenerEntregasPorEstado(estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(estado, result.getOrNull()!![0].estadoEntrega)
    }

    // ==========================================
    // Tests de ASIGNAR TRANSPORTISTA
    // ==========================================

    @Test
    fun `asignarTransportista asigna correctamente`() = runBlocking {
        // Arrange
        val entregaId = 1L
        val transportistaId = 5L
        val entregaActualizada = EntregaDTO(
            idEntrega = entregaId,
            idBoleta = 100L,
            idTransportista = transportistaId,
            estadoEntrega = "asignada",
            fechaAsignacion = "2024-01-15",
            fechaEntrega = null,
            observacion = null,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.asignarTransportista(entregaId, transportistaId) } returns Response.success(entregaActualizada)

        // Act
        val result = repository.asignarTransportista(entregaId, transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(transportistaId, result.getOrNull()!!.idTransportista)

        verify { Log.d("EntregasRemoteRepository", "asignarTransportista: entregaId=$entregaId, transportistaId=$transportistaId") }
    }

    @Test
    fun `asignarTransportista falla cuando entrega no existe`() = runBlocking {
        // Arrange
        val entregaId = 999L
        val transportistaId = 5L

        coEvery { api.asignarTransportista(entregaId, transportistaId) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.asignarTransportista(entregaId, transportistaId)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de COMPLETAR ENTREGA
    // ==========================================

    @Test
    fun `completarEntrega completa correctamente con observacion`() = runBlocking {
        // Arrange
        val entregaId = 1L
        val observacion = "Entregado al cliente"
        val entregaCompletada = EntregaDTO(
            idEntrega = entregaId,
            idBoleta = 100L,
            idTransportista = 5L,
            estadoEntrega = "completada",
            fechaAsignacion = "2024-01-15",
            fechaEntrega = "2024-01-16",
            observacion = observacion,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.completarEntrega(entregaId, observacion) } returns Response.success(entregaCompletada)

        // Act
        val result = repository.completarEntrega(entregaId, observacion)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("completada", result.getOrNull()!!.estadoEntrega)
        assertEquals(observacion, result.getOrNull()!!.observacion)
        assertNotNull(result.getOrNull()!!.fechaEntrega)

        verify { Log.d("EntregasRemoteRepository", "completarEntrega: entregaId=$entregaId, observacion=$observacion") }
    }

    @Test
    fun `completarEntrega completa correctamente sin observacion`() = runBlocking {
        // Arrange
        val entregaId = 1L
        val entregaCompletada = EntregaDTO(
            idEntrega = entregaId,
            idBoleta = 100L,
            idTransportista = 5L,
            estadoEntrega = "completada",
            fechaAsignacion = "2024-01-15",
            fechaEntrega = "2024-01-16",
            observacion = null,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.completarEntrega(entregaId, null) } returns Response.success(entregaCompletada)

        // Act
        val result = repository.completarEntrega(entregaId, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("completada", result.getOrNull()!!.estadoEntrega)
        assertNull(result.getOrNull()!!.observacion)
    }

    // ==========================================
    // Tests de CAMBIAR ESTADO ENTREGA
    // ==========================================

    @Test
    fun `cambiarEstadoEntrega cambia de pendiente a en_proceso`() = runBlocking {
        // Arrange
        val entregaId = 1L
        val nuevoEstado = "en_proceso"
        val entregaActualizada = EntregaDTO(
            idEntrega = entregaId,
            idBoleta = 100L,
            idTransportista = 5L,
            estadoEntrega = nuevoEstado,
            fechaAsignacion = "2024-01-15",
            fechaEntrega = null,
            observacion = null,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.cambiarEstadoEntrega(entregaId, nuevoEstado) } returns Response.success(entregaActualizada)

        // Act
        val result = repository.cambiarEstadoEntrega(entregaId, nuevoEstado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(nuevoEstado, result.getOrNull()!!.estadoEntrega)

        verify { Log.d("EntregasRemoteRepository", "cambiarEstadoEntrega: entregaId=$entregaId, nuevoEstado=$nuevoEstado") }
    }

    @Test
    fun `cambiarEstadoEntrega cambia de en_proceso a completada`() = runBlocking {
        // Arrange
        val entregaId = 1L
        val nuevoEstado = "completada"
        val entregaActualizada = EntregaDTO(
            idEntrega = entregaId,
            idBoleta = 100L,
            idTransportista = 5L,
            estadoEntrega = nuevoEstado,
            fechaAsignacion = "2024-01-15",
            fechaEntrega = "2024-01-16",
            observacion = null,
            numeroBoleta = "BOL-001",
            nombreCliente = "Juan Pérez",
            direccionEntrega = "Calle Falsa 123"
        )

        coEvery { api.cambiarEstadoEntrega(entregaId, nuevoEstado) } returns Response.success(entregaActualizada)

        // Act
        val result = repository.cambiarEstadoEntrega(entregaId, nuevoEstado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(nuevoEstado, result.getOrNull()!!.estadoEntrega)
    }

    // ==========================================
    // Tests de CONTAR ENTREGAS PENDIENTES
    // ==========================================

    @Test
    fun `contarEntregasPendientes cuenta correctamente entregas pendientes y en_proceso`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = transportistaId,
                estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            ),
            EntregaDTO(
                idEntrega = 2L,
                idBoleta = 101L,
                idTransportista = transportistaId,
                estadoEntrega = "en_proceso",
                fechaAsignacion = "2024-01-14",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-002",
                nombreCliente = "María González",
                direccionEntrega = "Av. Principal 456"
            ),
            EntregaDTO(
                idEntrega = 3L,
                idBoleta = 102L,
                idTransportista = transportistaId,
                estadoEntrega = "completada",
                fechaAsignacion = "2024-01-13",
                fechaEntrega = "2024-01-14",
                observacion = "Entregado",
                numeroBoleta = "BOL-003",
                nombreCliente = "Carlos López",
                direccionEntrega = "Calle Sur 789"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(entregas)

        // Act
        val result = repository.contarEntregasPendientes(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()) // 1 pendiente + 1 en_proceso
    }

    @Test
    fun `contarEntregasPendientes retorna cero cuando no hay pendientes`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = transportistaId,
                estadoEntrega = "completada",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = "2024-01-16",
                observacion = "Entregado",
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(entregas)

        // Act
        val result = repository.contarEntregasPendientes(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `contarEntregasPendientes maneja error al obtener entregas`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.error(
            500,
            "Server error".toResponseBody()
        )

        // Act
        val result = repository.contarEntregasPendientes(transportistaId)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de CONTAR ENTREGAS COMPLETADAS
    // ==========================================

    @Test
    fun `contarEntregasCompletadas cuenta correctamente`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = transportistaId,
                estadoEntrega = "completada",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = "2024-01-16",
                observacion = "Entregado",
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            ),
            EntregaDTO(
                idEntrega = 2L,
                idBoleta = 101L,
                idTransportista = transportistaId,
                estadoEntrega = "completada",
                fechaAsignacion = "2024-01-14",
                fechaEntrega = "2024-01-15",
                observacion = "Entregado",
                numeroBoleta = "BOL-002",
                nombreCliente = "María González",
                direccionEntrega = "Av. Principal 456"
            ),
            EntregaDTO(
                idEntrega = 3L,
                idBoleta = 102L,
                idTransportista = transportistaId,
                estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-16",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-003",
                nombreCliente = "Carlos López",
                direccionEntrega = "Calle Sur 789"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(entregas)

        // Act
        val result = repository.contarEntregasCompletadas(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun `contarEntregasCompletadas retorna cero cuando no hay completadas`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        val entregas = listOf(
            EntregaDTO(
                idEntrega = 1L,
                idBoleta = 100L,
                idTransportista = transportistaId,
                estadoEntrega = "pendiente",
                fechaAsignacion = "2024-01-15",
                fechaEntrega = null,
                observacion = null,
                numeroBoleta = "BOL-001",
                nombreCliente = "Juan Pérez",
                direccionEntrega = "Calle Falsa 123"
            )
        )

        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } returns Response.success(entregas)

        // Act
        val result = repository.contarEntregasCompletadas(transportistaId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `contarEntregasCompletadas maneja error al obtener entregas`() = runBlocking {
        // Arrange
        val transportistaId = 5L
        coEvery { api.obtenerEntregasPorTransportista(transportistaId) } throws Exception("Network error")

        // Act
        val result = repository.contarEntregasCompletadas(transportistaId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}

