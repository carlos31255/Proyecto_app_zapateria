package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.usuario.UsuarioApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class UsuarioRemoteRepositoryTest {

    private val usuarioApi = mockk<UsuarioApiService>()
    private val repository = UsuarioRemoteRepository(usuarioApi)

    @Test
    fun obtenerTodosLosUsuarios_retorna_lista_valida() = runBlocking {
        val usuarios = listOf(
            UsuarioDTO(idPersona = 1L, idRol = 1L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN"),
            UsuarioDTO(idPersona = 2L, idRol = 2L, nombreCompleto = "Maria Lopez", username = "mlopez", nombreRol = "VENDEDOR")
        )

        coEvery { usuarioApi.obtenerTodosLosUsuarios() } returns usuarios

        val result = repository.obtenerTodosLosUsuarios()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun obtenerUsuarioPorId_retorna_usuario_valido() = runBlocking {
        val usuario = UsuarioDTO(idPersona = 1L, idRol = 1L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN")

        coEvery { usuarioApi.obtenerUsuarioPorId(1L) } returns Response.success(usuario)

        val result = repository.obtenerUsuarioPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.idPersona)
    }

    @Test
    fun obtenerUsuariosPorRol_retorna_lista_valida() = runBlocking {
        val usuarios = listOf(
            UsuarioDTO(idPersona = 1L, idRol = 1L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "ADMIN"),
            UsuarioDTO(idPersona = 2L, idRol = 1L, nombreCompleto = "Pedro Gomez", username = "pgomez", nombreRol = "ADMIN")
        )

        coEvery { usuarioApi.obtenerUsuariosPorRol(1L) } returns usuarios

        val result = repository.obtenerUsuariosPorRol(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun crearUsuario_retorna_exito() = runBlocking {
        val nuevoUsuario = UsuarioDTO(idPersona = 10L, idRol = 2L, nombreCompleto = "Carlos Ruiz", username = "cruiz", nombreRol = "VENDEDOR")

        coEvery { usuarioApi.crearUsuario(nuevoUsuario) } returns Response.success(nuevoUsuario)

        val result = repository.crearUsuario(nuevoUsuario)

        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()?.idPersona)
    }

    @Test
    fun actualizarRolUsuario_retorna_exito() = runBlocking {
        val usuarioActualizado = UsuarioDTO(idPersona = 1L, idRol = 2L, nombreCompleto = "Juan Perez", username = "jperez", nombreRol = "VENDEDOR")

        coEvery { usuarioApi.actualizarRolUsuario(1L, 2L) } returns Response.success(usuarioActualizado)

        val result = repository.actualizarRolUsuario(1L, 2L)

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull()?.idRol)
    }

    @Test
    fun eliminarUsuario_retorna_exito() = runBlocking {
        coEvery { usuarioApi.eliminarUsuario(1L) } returns Response.success(null)

        val result = repository.eliminarUsuario(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
}

