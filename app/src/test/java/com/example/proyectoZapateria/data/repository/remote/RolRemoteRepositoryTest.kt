package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RolRemoteRepositoryTest {

    private lateinit var rolApi: RolApiService
    private lateinit var repository: RolRemoteRepository

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

        rolApi = mockk()
        repository = RolRemoteRepository(rolApi)
    }

    // ==========================================
    // Tests de OBTENER TODOS LOS ROLES
    // ==========================================

    @Test
    fun `obtenerTodosLosRoles retorna lista cuando hay roles`() = runBlocking {
        // Arrange
        val roles = listOf(
            RolDTO(idRol = 1L, nombreRol = "ADMIN", descripcion = "Administrador del sistema"),
            RolDTO(idRol = 2L, nombreRol = "VENDEDOR", descripcion = "Vendedor de productos"),
            RolDTO(idRol = 3L, nombreRol = "CLIENTE", descripcion = "Cliente de la tienda")
        )

        coEvery { rolApi.obtenerTodosLosRoles() } returns roles

        // Act
        val result = repository.obtenerTodosLosRoles()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        assertEquals("ADMIN", result.getOrNull()!![0].nombreRol)
    }

    @Test
    fun `obtenerTodosLosRoles retorna lista vacia cuando no hay roles`() = runBlocking {
        // Arrange
        coEvery { rolApi.obtenerTodosLosRoles() } returns emptyList()

        // Act
        val result = repository.obtenerTodosLosRoles()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodosLosRoles maneja UnknownHostException`() = runBlocking {
        // Arrange
        coEvery { rolApi.obtenerTodosLosRoles() } throws UnknownHostException("No internet")

        // Act
        val result = repository.obtenerTodosLosRoles()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Sin conexión") ?: false)
    }

    @Test
    fun `obtenerTodosLosRoles maneja SocketTimeoutException`() = runBlocking {
        // Arrange
        coEvery { rolApi.obtenerTodosLosRoles() } throws SocketTimeoutException("Timeout")

        // Act
        val result = repository.obtenerTodosLosRoles()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Timeout") ?: false)
    }

    @Test
    fun `obtenerTodosLosRoles maneja IOException`() = runBlocking {
        // Arrange
        coEvery { rolApi.obtenerTodosLosRoles() } throws java.io.IOException("IO error")

        // Act
        val result = repository.obtenerTodosLosRoles()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error de red") ?: false)
    }

    // ==========================================
    // Tests de OBTENER ROL POR ID
    // ==========================================

    @Test
    fun `obtenerRolPorId retorna rol cuando existe`() = runBlocking {
        // Arrange
        val idRol = 1L
        val rol = RolDTO(idRol = idRol, nombreRol = "ADMIN", descripcion = "Administrador")

        coEvery { rolApi.obtenerRolPorId(idRol) } returns Response.success(rol)

        // Act
        val result = repository.obtenerRolPorId(idRol)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull()?.nombreRol)
    }

    @Test
    fun `obtenerRolPorId retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idRol = 999L
        coEvery { rolApi.obtenerRolPorId(idRol) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.obtenerRolPorId(idRol)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de OBTENER ROL POR NOMBRE
    // ==========================================

    @Test
    fun `obtenerRolPorNombre retorna rol cuando existe`() = runBlocking {
        // Arrange
        val nombreRol = "ADMIN"
        val rol = RolDTO(idRol = 1L, nombreRol = nombreRol, descripcion = "Administrador")

        coEvery { rolApi.obtenerRolPorNombre(nombreRol) } returns Response.success(rol)

        // Act
        val result = repository.obtenerRolPorNombre(nombreRol)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(nombreRol, result.getOrNull()?.nombreRol)
    }

    @Test
    fun `obtenerRolPorNombre retorna error cuando no existe`() = runBlocking {
        // Arrange
        val nombreRol = "NOEXISTE"
        coEvery { rolApi.obtenerRolPorNombre(nombreRol) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.obtenerRolPorNombre(nombreRol)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de CREAR ROL
    // ==========================================

    @Test
    fun `crearRol crea correctamente`() = runBlocking {
        // Arrange
        val nuevoRol = RolDTO(idRol = null, nombreRol = "GERENTE", descripcion = "Gerente de tienda")
        val rolCreado = nuevoRol.copy(idRol = 4L)

        coEvery { rolApi.crearRol(nuevoRol) } returns Response.success(rolCreado)

        // Act
        val result = repository.crearRol(nuevoRol)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(4L, result.getOrNull()?.idRol)
        assertEquals("GERENTE", result.getOrNull()?.nombreRol)
    }

    @Test
    fun `crearRol falla cuando nombre duplicado`() = runBlocking {
        // Arrange
        val nuevoRol = RolDTO(idRol = null, nombreRol = "ADMIN", descripcion = "Duplicado")

        coEvery { rolApi.crearRol(nuevoRol) } returns Response.error(409, "Conflict".toResponseBody())

        // Act
        val result = repository.crearRol(nuevoRol)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ACTUALIZAR ROL
    // ==========================================

    @Test
    fun `actualizarRol actualiza correctamente`() = runBlocking {
        // Arrange
        val idRol = 1L
        val rolActualizado = RolDTO(idRol = idRol, nombreRol = "ADMIN", descripcion = "Administrador actualizado")

        coEvery { rolApi.actualizarRol(idRol, rolActualizado) } returns Response.success(rolActualizado)

        // Act
        val result = repository.actualizarRol(idRol, rolActualizado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Administrador actualizado", result.getOrNull()?.descripcion)
    }

    @Test
    fun `actualizarRol falla cuando no existe`() = runBlocking {
        // Arrange
        val idRol = 999L
        val rol = RolDTO(idRol = idRol, nombreRol = "TEST", descripcion = "Test")

        coEvery { rolApi.actualizarRol(idRol, rol) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.actualizarRol(idRol, rol)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ELIMINAR ROL
    // ==========================================

    @Test
    fun `eliminarRol elimina correctamente`() = runBlocking {
        // Arrange
        val idRol = 1L
        coEvery { rolApi.eliminarRol(idRol) } returns Response.success(null)

        // Act
        val result = repository.eliminarRol(idRol)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `eliminarRol falla cuando no existe`() = runBlocking {
        // Arrange
        val idRol = 999L
        coEvery { rolApi.eliminarRol(idRol) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.eliminarRol(idRol)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `eliminarRol maneja excepcion`() = runBlocking {
        // Arrange
        val idRol = 1L
        coEvery { rolApi.eliminarRol(idRol) } throws Exception("Network error")

        // Act
        val result = repository.eliminarRol(idRol)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}

