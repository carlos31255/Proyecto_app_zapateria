package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.usuario.ClienteApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.ClienteDTO
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class ClienteRemoteRepositoryTest {

    @Test
    fun getClientes_retorna_lista_valida() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val clientes = listOf(
            ClienteDTO(idPersona = 1L, categoria = "VIP", nombreCompleto = "Juan Pérez", email = "juan@test.com", telefono = "987654321", activo = true),
            ClienteDTO(idPersona = 2L, categoria = "Regular", nombreCompleto = "María González", email = "maria@test.com", telefono = "912345678", activo = true)
        )

        clienteApi.todosResponse = Response.success(clientes)

        // Act
        val result = repository.obtenerTodosLosClientes()

        // Assert
        assertTrue(result.isSuccess)
        val lista = result.getOrNull()!!
        assertEquals(2, lista.size)
        assertEquals("Juan Pérez", lista[0].nombreCompleto)
    }

    @Test
    fun getClienteById_retorna_objeto_valido() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val idPersona = 1L
        val cliente = ClienteDTO(idPersona = idPersona, categoria = "VIP", nombreCompleto = "Juan Pérez", email = "juan@test.com", telefono = "987654321", activo = true)

        clienteApi.byIdResponse = Response.success(cliente)

        // Act
        val result = repository.obtenerClientePorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        val c = result.getOrNull()!!
        assertNotNull(c)
        assertEquals(idPersona, c!!.idPersona)
        assertEquals("Juan Pérez", c.nombreCompleto)
    }

    @Test
    fun getClientesPorCategoria_retorna_lista_valida() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val categoria = "VIP"
        val clientesVIP = listOf(
            ClienteDTO(idPersona = 1L, categoria = "VIP", nombreCompleto = "Juan Pérez", email = "juan@test.com", telefono = "987654321", activo = true)
        )

        clienteApi.porCategoriaResponse = Response.success(clientesVIP)

        // Act
        val result = repository.obtenerClientesPorCategoria(categoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("VIP", result.getOrNull()!![0].categoria)
    }

    @Test
    fun crearCliente_retorna_exito() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val nuevo = ClienteDTO(idPersona = null, categoria = "Regular", nombreCompleto = "Nuevo Cliente", email = "nuevo@test.com", telefono = "956789012", activo = true)
        val creado = nuevo.copy(idPersona = 10L)

        clienteApi.crearResponse = Response.success(creado)

        // Act
        val result = repository.crearCliente(nuevo)

        // Assert
        assertTrue(result.isSuccess)
        val c = result.getOrNull()!!
        assertEquals(10L, c!!.idPersona)
        assertEquals("Nuevo Cliente", c.nombreCompleto)
    }

    @Test
    fun actualizarCategoria_retorna_exito() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val idPersona = 1L
        val nuevaCategoria = "VIP"
        val actualizado = ClienteDTO(idPersona = idPersona, categoria = nuevaCategoria, nombreCompleto = "Juan Pérez", email = "juan@test.com", telefono = "987654321", activo = true)

        clienteApi.actualizarCategoriaResponse = Response.success(actualizado)

        // Act
        val result = repository.actualizarCategoria(idPersona, nuevaCategoria)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("VIP", result.getOrNull()!!.categoria)
    }

    @Test
    fun eliminarCliente_retorna_exito_204() = runBlocking {
        // Arrange
        val clienteApi = FakeClienteApiService()
        val repository = ClienteRemoteRepository(clienteApi)

        val idPersona = 1L
        clienteApi.eliminarResponse = Response.success<Void>(204, null)

        // Act
        val result = repository.eliminarCliente(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    // Fake simple del ApiService que permite configurar respuestas por test
    private class FakeClienteApiService : ClienteApiService {
        var todosResponse: Response<List<ClienteDTO>> = Response.success(emptyList())
        var byIdResponse: Response<ClienteDTO> = Response.success(null)
        var porCategoriaResponse: Response<List<ClienteDTO>> = Response.success(emptyList())
        var crearResponse: Response<ClienteDTO> = Response.success(null)
        var actualizarCategoriaResponse: Response<ClienteDTO> = Response.success(null)
        var eliminarResponse: Response<Void> = Response.success(null)

        override suspend fun obtenerTodosLosClientes(): Response<List<ClienteDTO>> = todosResponse
        override suspend fun obtenerClientePorId(idPersona: Long): Response<ClienteDTO> = byIdResponse
        override suspend fun obtenerClientesPorCategoria(categoria: String): Response<List<ClienteDTO>> = porCategoriaResponse
        override suspend fun crearCliente(clienteDTO: ClienteDTO): Response<ClienteDTO> = crearResponse
        override suspend fun actualizarCategoria(idPersona: Long, nuevaCategoria: String): Response<ClienteDTO> = actualizarCategoriaResponse
        override suspend fun eliminarCliente(idPersona: Long): Response<Void> = eliminarResponse
    }

}
