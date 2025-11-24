package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UsuarioRemoteRepositoryTest {

    private lateinit var usuarioApi: UsuarioApiService
    private lateinit var repository: UsuarioRemoteRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        usuarioApi = mockk()
        repository = UsuarioRemoteRepository(usuarioApi)
    }

    // ==========================================
    // Tests de OBTENER TODOS LOS USUARIOS
    // ==========================================

    @Test
    fun `obtenerTodosLosUsuarios retorna lista cuando hay usuarios`() = runBlocking {
        // Arrange
        val usuarios = listOf(
            UsuarioDTO(idPersona = 1L, idRol = 1L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN"),
            UsuarioDTO(idPersona = 2L, idRol = 2L, nombreCompleto = "Maria Lopez", username = "mlopez", nombreRol = "VENDEDOR")
        )

        coEvery { usuarioApi.obtenerTodosLosUsuarios() } returns usuarios

        // Act
        val result = repository.obtenerTodosLosUsuarios()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodosLosUsuarios maneja UnknownHostException`() = runBlocking {
        // Arrange
        coEvery { usuarioApi.obtenerTodosLosUsuarios() } throws UnknownHostException("No internet")

        // Act
        val result = repository.obtenerTodosLosUsuarios()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Sin conexión") ?: false)
    }

    @Test
    fun `obtenerTodosLosUsuarios maneja SocketTimeoutException`() = runBlocking {
        // Arrange
        coEvery { usuarioApi.obtenerTodosLosUsuarios() } throws SocketTimeoutException("Timeout")

        // Act
        val result = repository.obtenerTodosLosUsuarios()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Timeout") ?: false)
    }

    // ==========================================
    // Tests de OBTENER USUARIO POR ID
    // ==========================================

    @Test
    fun `obtenerUsuarioPorId retorna usuario cuando existe`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val usuario = UsuarioDTO(idPersona = idPersona, idRol = 1L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN")

        coEvery { usuarioApi.obtenerUsuarioPorId(idPersona) } returns Response.success(usuario)

        // Act
        val result = repository.obtenerUsuarioPorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idPersona, result.getOrNull()?.idPersona)
    }

    @Test
    fun `obtenerUsuarioPorId retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        coEvery { usuarioApi.obtenerUsuarioPorId(idPersona) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.obtenerUsuarioPorId(idPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de OBTENER USUARIOS POR ROL
    // ==========================================

    @Test
    fun `obtenerUsuariosPorRol retorna usuarios del rol especificado`() = runBlocking {
        // Arrange
        val idRol = 1L
        val usuarios = listOf(
            UsuarioDTO(idPersona = 1L, idRol = idRol, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN"),
            UsuarioDTO(idPersona = 2L, idRol = idRol, nombreCompleto = "Pedro Gomez", username = "pgomez", nombreRol = "ADMIN")
        )

        coEvery { usuarioApi.obtenerUsuariosPorRol(idRol) } returns usuarios

        // Act
        val result = repository.obtenerUsuariosPorRol(idRol)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.idRol == idRol })
    }

    @Test
    fun `obtenerUsuariosPorRol maneja IOException`() = runBlocking {
        // Arrange
        val idRol = 1L
        coEvery { usuarioApi.obtenerUsuariosPorRol(idRol) } throws java.io.IOException("Network error")

        // Act
        val result = repository.obtenerUsuariosPorRol(idRol)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error de red") ?: false)
    }

    // ==========================================
    // Tests de CREAR USUARIO
    // ==========================================

    @Test
    fun `crearUsuario crea correctamente`() = runBlocking {
        // Arrange
        val nuevoUsuario = UsuarioDTO(idPersona = 10L, idRol = 2L, nombreCompleto = "Carlos Ruiz", username = "cruiz", nombreRol = "VENDEDOR")

        coEvery { usuarioApi.crearUsuario(nuevoUsuario) } returns Response.success(nuevoUsuario)

        // Act
        val result = repository.crearUsuario(nuevoUsuario)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()?.idPersona)
    }

    @Test
    fun `crearUsuario falla con datos invalidos`() = runBlocking {
        // Arrange
        val usuarioInvalido = UsuarioDTO(idPersona = null, idRol = null, nombreCompleto = "", username = "", nombreRol = "")

        coEvery { usuarioApi.crearUsuario(usuarioInvalido) } returns Response.error(400, "Bad request".toResponseBody())

        // Act
        val result = repository.crearUsuario(usuarioInvalido)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ACTUALIZAR ROL
    // ==========================================

    @Test
    fun `actualizarRolUsuario actualiza correctamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val nuevoIdRol = 2L
        val usuarioActualizado = UsuarioDTO(idPersona = idPersona, idRol = nuevoIdRol, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "VENDEDOR")

        coEvery { usuarioApi.actualizarRolUsuario(idPersona, nuevoIdRol) } returns Response.success(usuarioActualizado)

        // Act
        val result = repository.actualizarRolUsuario(idPersona, nuevoIdRol)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(nuevoIdRol, result.getOrNull()?.idRol)
    }

    @Test
    fun `actualizarRolUsuario falla cuando usuario no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        val nuevoIdRol = 2L

        coEvery { usuarioApi.actualizarRolUsuario(idPersona, nuevoIdRol) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.actualizarRolUsuario(idPersona, nuevoIdRol)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ELIMINAR USUARIO
    // ==========================================

    @Test
    fun `eliminarUsuario elimina correctamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { usuarioApi.eliminarUsuario(idPersona) } returns Response.success(null)

        // Act
        val result = repository.eliminarUsuario(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `eliminarUsuario falla cuando no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        coEvery { usuarioApi.eliminarUsuario(idPersona) } returns Response.error(404, "Not found".toResponseBody())

        // Act
        val result = repository.eliminarUsuario(idPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `eliminarUsuario maneja excepcion`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { usuarioApi.eliminarUsuario(idPersona) } throws Exception("Network error")

        // Act
        val result = repository.eliminarUsuario(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}

