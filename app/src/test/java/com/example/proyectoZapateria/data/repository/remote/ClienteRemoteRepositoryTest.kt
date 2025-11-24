package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ClienteRemoteRepositoryTest {

    private lateinit var clienteApi: ClienteApiService
    private lateinit var repository: ClienteRemoteRepository

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

        clienteApi = mockk()
        repository = ClienteRemoteRepository(clienteApi)
    }

    // ==========================================
    // Tests de OBTENER TODOS LOS CLIENTES
    // ==========================================

    @Test
    fun `obtenerTodosLosClientes retorna lista valida cuando hay clientes`() = runBlocking {
        // Arrange
        val clientes = listOf(
            ClienteDTO(
                idPersona = 1L,
                categoria = "VIP",
                nombreCompleto = "Juan Pérez",
                email = "juan@test.com",
                telefono = "987654321",
                activo = true
            ),
            ClienteDTO(
                idPersona = 2L,
                categoria = "Regular",
                nombreCompleto = "María González",
                email = "maria@test.com",
                telefono = "912345678",
                activo = true
            ),
            ClienteDTO(
                idPersona = 3L,
                categoria = "Premium",
                nombreCompleto = "Carlos López",
                email = "carlos@test.com",
                telefono = "923456789",
                activo = true
            )
        )

        coEvery { clienteApi.obtenerTodosLosClientes() } returns clientes

        // Act
        val result = repository.obtenerTodosLosClientes()

        // Assert
        assertTrue(result.isSuccess)
        val clientesObtenidos = result.getOrNull()!!
        assertEquals(3, clientesObtenidos.size)
        assertEquals("Juan Pérez", clientesObtenidos[0].nombreCompleto)
        assertEquals("VIP", clientesObtenidos[0].categoria)
        assertEquals("María González", clientesObtenidos[1].nombreCompleto)
        assertEquals("Regular", clientesObtenidos[1].categoria)

        coVerify(exactly = 1) { clienteApi.obtenerTodosLosClientes() }
    }

    @Test
    fun `obtenerTodosLosClientes retorna lista vacia cuando no hay clientes`() = runBlocking {
        // Arrange
        coEvery { clienteApi.obtenerTodosLosClientes() } returns emptyList()

        // Act
        val result = repository.obtenerTodosLosClientes()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodosLosClientes maneja excepcion de red`() = runBlocking {
        // Arrange
        coEvery { clienteApi.obtenerTodosLosClientes() } throws Exception("Network error")

        // Act
        val result = repository.obtenerTodosLosClientes()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER CLIENTE POR ID
    // ==========================================

    @Test
    fun `obtenerClientePorId retorna cliente cuando existe`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val cliente = ClienteDTO(
            idPersona = idPersona,
            categoria = "VIP",
            nombreCompleto = "Juan Pérez",
            email = "juan@test.com",
            telefono = "987654321",
            activo = true
        )

        coEvery { clienteApi.obtenerClientePorId(idPersona) } returns Response.success(cliente)

        // Act
        val result = repository.obtenerClientePorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        val clienteObtenido = result.getOrNull()!!
        assertEquals(idPersona, clienteObtenido.idPersona)
        assertEquals("Juan Pérez", clienteObtenido.nombreCompleto)
        assertEquals("VIP", clienteObtenido.categoria)
        assertEquals("juan@test.com", clienteObtenido.email)
        assertTrue(clienteObtenido.activo!!)

        coVerify(exactly = 1) { clienteApi.obtenerClientePorId(idPersona) }
    }

    @Test
    fun `obtenerClientePorId retorna error cuando cliente no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        coEvery { clienteApi.obtenerClientePorId(idPersona) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.obtenerClientePorId(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("404") ?: false)
    }

    @Test
    fun `obtenerClientePorId maneja respuesta exitosa con body null`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { clienteApi.obtenerClientePorId(idPersona) } returns Response.success(null)

        // Act
        val result = repository.obtenerClientePorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `obtenerClientePorId maneja excepcion de red`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { clienteApi.obtenerClientePorId(idPersona) } throws Exception("Connection timeout")

        // Act
        val result = repository.obtenerClientePorId(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Connection timeout", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER CLIENTES POR CATEGORÍA
    // ==========================================

    @Test
    fun `obtenerClientesPorCategoria retorna clientes VIP correctamente`() = runBlocking {
        // Arrange
        val categoria = "VIP"
        val clientesVIP = listOf(
            ClienteDTO(
                idPersona = 1L,
                categoria = "VIP",
                nombreCompleto = "Juan Pérez",
                email = "juan@test.com",
                telefono = "987654321",
                activo = true
            ),
            ClienteDTO(
                idPersona = 5L,
                categoria = "VIP",
                nombreCompleto = "Ana Torres",
                email = "ana@test.com",
                telefono = "945678901",
                activo = true
            )
        )

        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } returns clientesVIP

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        val clientes = result.getOrNull()!!
        assertEquals(2, clientes.size)
        assertTrue(clientes.all { it.categoria == "VIP" })
        coVerify(exactly = 1) { clienteApi.obtenerClientesPorCategoria(categoria) }
    }

    @Test
    fun `obtenerClientesPorCategoria retorna clientes Premium correctamente`() = runBlocking {
        // Arrange
        val categoria = "Premium"
        val clientesPremium = listOf(
            ClienteDTO(
                idPersona = 3L,
                categoria = "Premium",
                nombreCompleto = "Carlos López",
                email = "carlos@test.com",
                telefono = "923456789",
                activo = true
            )
        )

        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } returns clientesPremium

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Premium", result.getOrNull()!![0].categoria)
    }

    @Test
    fun `obtenerClientesPorCategoria retorna clientes Regular correctamente`() = runBlocking {
        // Arrange
        val categoria = "Regular"
        val clientesRegular = listOf(
            ClienteDTO(
                idPersona = 2L,
                categoria = "Regular",
                nombreCompleto = "María González",
                email = "maria@test.com",
                telefono = "912345678",
                activo = true
            ),
            ClienteDTO(
                idPersona = 4L,
                categoria = "Regular",
                nombreCompleto = "Pedro Ramírez",
                email = "pedro@test.com",
                telefono = "934567890",
                activo = true
            )
        )

        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } returns clientesRegular

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.categoria == "Regular" })
    }

    @Test
    fun `obtenerClientesPorCategoria retorna lista vacia cuando no hay clientes de esa categoria`() = runBlocking {
        // Arrange
        val categoria = "VIP"
        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } returns emptyList()

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerClientesPorCategoria maneja excepcion de red`() = runBlocking {
        // Arrange
        val categoria = "VIP"
        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } throws Exception("Server error")

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de CREAR CLIENTE
    // ==========================================

    @Test
    fun `crearCliente crea cliente exitosamente`() = runBlocking {
        // Arrange
        val nuevoCliente = ClienteDTO(
            idPersona = null,
            categoria = "Regular",
            nombreCompleto = "Nuevo Cliente",
            email = "nuevo@test.com",
            telefono = "956789012",
            activo = true
        )

        val clienteCreado = nuevoCliente.copy(idPersona = 10L)

        coEvery { clienteApi.crearCliente(nuevoCliente) } returns Response.success(clienteCreado)

        // Act
        val result = repository.crearCliente(nuevoCliente)

        // Assert
        assertTrue(result.isSuccess)
        val cliente = result.getOrNull()!!
        assertEquals(10L, cliente.idPersona)
        assertEquals("Nuevo Cliente", cliente.nombreCompleto)
        assertEquals("Regular", cliente.categoria)
        assertEquals("nuevo@test.com", cliente.email)

        coVerify(exactly = 1) { clienteApi.crearCliente(nuevoCliente) }
    }

    @Test
    fun `crearCliente falla cuando el email ya existe`() = runBlocking {
        // Arrange
        val nuevoCliente = ClienteDTO(
            idPersona = null,
            categoria = "Regular",
            nombreCompleto = "Nuevo Cliente",
            email = "existente@test.com",
            telefono = "956789012",
            activo = true
        )

        coEvery { clienteApi.crearCliente(nuevoCliente) } returns Response.error(
            409,
            "Email already exists".toResponseBody()
        )

        // Act
        val result = repository.crearCliente(nuevoCliente)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("409") ?: false)
    }

    @Test
    fun `crearCliente falla con datos invalidos`() = runBlocking {
        // Arrange
        val clienteInvalido = ClienteDTO(
            idPersona = null,
            categoria = "",
            nombreCompleto = "",
            email = "invalid-email",
            telefono = "",
            activo = true
        )

        coEvery { clienteApi.crearCliente(clienteInvalido) } returns Response.error(
            400,
            "Bad request".toResponseBody()
        )

        // Act
        val result = repository.crearCliente(clienteInvalido)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("400") ?: false)
    }

    @Test
    fun `crearCliente maneja excepcion de red`() = runBlocking {
        // Arrange
        val nuevoCliente = ClienteDTO(
            idPersona = null,
            categoria = "Regular",
            nombreCompleto = "Nuevo Cliente",
            email = "nuevo@test.com",
            telefono = "956789012",
            activo = true
        )

        coEvery { clienteApi.crearCliente(nuevoCliente) } throws Exception("Network timeout")

        // Act
        val result = repository.crearCliente(nuevoCliente)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de ACTUALIZAR CATEGORÍA
    // ==========================================

    @Test
    fun `actualizarCategoria actualiza de Regular a VIP exitosamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val nuevaCategoria = "VIP"
        val clienteActualizado = ClienteDTO(
            idPersona = idPersona,
            categoria = nuevaCategoria,
            nombreCompleto = "Juan Pérez",
            email = "juan@test.com",
            telefono = "987654321",
            activo = true
        )

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } returns
            Response.success(clienteActualizado)

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isSuccess)
        val cliente = result.getOrNull()!!
        assertEquals("VIP", cliente.categoria)
        assertEquals(idPersona, cliente.idPersona)

        coVerify(exactly = 1) { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) }
    }

    @Test
    fun `actualizarCategoria actualiza de VIP a Premium exitosamente`() = runBlocking {
        // Arrange
        val idPersona = 2L
        val nuevaCategoria = "Premium"
        val clienteActualizado = ClienteDTO(
            idPersona = idPersona,
            categoria = nuevaCategoria,
            nombreCompleto = "María González",
            email = "maria@test.com",
            telefono = "912345678",
            activo = true
        )

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } returns
            Response.success(clienteActualizado)

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Premium", result.getOrNull()?.categoria)
    }

    @Test
    fun `actualizarCategoria actualiza de Premium a Regular exitosamente`() = runBlocking {
        // Arrange
        val idPersona = 3L
        val nuevaCategoria = "Regular"
        val clienteActualizado = ClienteDTO(
            idPersona = idPersona,
            categoria = nuevaCategoria,
            nombreCompleto = "Carlos López",
            email = "carlos@test.com",
            telefono = "923456789",
            activo = true
        )

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } returns
            Response.success(clienteActualizado)

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Regular", result.getOrNull()?.categoria)
    }

    @Test
    fun `actualizarCategoria falla cuando el cliente no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        val nuevaCategoria = "VIP"

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } returns
            Response.error(404, "Cliente not found".toResponseBody())

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("404") ?: false)
    }

    @Test
    fun `actualizarCategoria falla con categoria invalida`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val nuevaCategoria = "INVALID"

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } returns
            Response.error(400, "Invalid category".toResponseBody())

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("400") ?: false)
    }

    @Test
    fun `actualizarCategoria maneja excepcion de red`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val nuevaCategoria = "VIP"

        coEvery { clienteApi.actualizarCategoria(idPersona, nuevaCategoria) } throws
            Exception("Connection lost")

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Connection lost", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de ELIMINAR CLIENTE
    // ==========================================

    @Test
    fun `eliminarCliente elimina cliente exitosamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val emptyResponse = Response.success<Void>(204, null)

        coEvery { clienteApi.eliminarCliente(idPersona) } returns emptyResponse

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)

        coVerify(exactly = 1) { clienteApi.eliminarCliente(idPersona) }
    }

    @Test
    fun `eliminarCliente retorna true con respuesta 200`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val successResponse = Response.success<Void>(200, null)

        coEvery { clienteApi.eliminarCliente(idPersona) } returns successResponse

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `eliminarCliente falla cuando el cliente no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        val errorResponse = Response.error<Void>(404, "Not found".toResponseBody())

        coEvery { clienteApi.eliminarCliente(idPersona) } returns errorResponse

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!) // isSuccessful es false para error 404
    }

    @Test
    fun `eliminarCliente maneja error 500 del servidor`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val serverErrorResponse = Response.error<Void>(500, "Internal server error".toResponseBody())

        coEvery { clienteApi.eliminarCliente(idPersona) } returns serverErrorResponse

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!) // isSuccessful es false para error 500
    }

    @Test
    fun `eliminarCliente maneja excepcion de red`() = runBlocking {
        // Arrange
        val idPersona = 1L

        coEvery { clienteApi.eliminarCliente(idPersona) } throws Exception("Network error")

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de CASOS EDGE Y VALIDACIONES
    // ==========================================

    @Test
    fun `obtenerTodosLosClientes maneja clientes con campos null`() = runBlocking {
        // Arrange
        val clientes = listOf(
            ClienteDTO(
                idPersona = 1L,
                categoria = null,
                nombreCompleto = "Cliente Sin Categoría",
                email = "cliente@test.com",
                telefono = null,
                activo = true
            )
        )

        coEvery { clienteApi.obtenerTodosLosClientes() } returns clientes

        // Act
        val result = repository.obtenerTodosLosClientes()

        // Assert
        assertTrue(result.isSuccess)
        val cliente = result.getOrNull()!![0]
        assertNull(cliente.categoria)
        assertNull(cliente.telefono)
        assertNotNull(cliente.nombreCompleto)
    }

    @Test
    fun `crearCliente con categoria VIP desde el inicio`() = runBlocking {
        // Arrange
        val clienteVIP = ClienteDTO(
            idPersona = null,
            categoria = "VIP",
            nombreCompleto = "Cliente VIP Nuevo",
            email = "vip@test.com",
            telefono = "911111111",
            activo = true
        )

        val clienteCreado = clienteVIP.copy(idPersona = 20L)

        coEvery { clienteApi.crearCliente(clienteVIP) } returns Response.success(clienteCreado)

        // Act
        val result = repository.crearCliente(clienteVIP)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("VIP", result.getOrNull()?.categoria)
    }

    @Test
    fun `obtenerClientesPorCategoria es case-sensitive`() = runBlocking {
        // Arrange
        val categoria = "vip" // minúsculas
        val clientesVIP = listOf(
            ClienteDTO(
                idPersona = 1L,
                categoria = "VIP", // mayúsculas
                nombreCompleto = "Juan Pérez",
                email = "juan@test.com",
                telefono = "987654321",
                activo = true
            )
        )

        coEvery { clienteApi.obtenerClientesPorCategoria(categoria) } returns clientesVIP

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        // La API debe manejar case-sensitivity, aquí solo verificamos que el repositorio pasa el parámetro correctamente
        coVerify { clienteApi.obtenerClientesPorCategoria("vip") }
    }

    @Test
    fun `crearCliente con activo false`() = runBlocking {
        // Arrange
        val clienteInactivo = ClienteDTO(
            idPersona = null,
            categoria = "Regular",
            nombreCompleto = "Cliente Inactivo",
            email = "inactivo@test.com",
            telefono = "900000000",
            activo = false
        )

        val clienteCreado = clienteInactivo.copy(idPersona = 30L)

        coEvery { clienteApi.crearCliente(clienteInactivo) } returns Response.success(clienteCreado)

        // Act
        val result = repository.crearCliente(clienteInactivo)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()?.activo!!)
    }
}

