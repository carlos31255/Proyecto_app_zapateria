package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.usuario.RolApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RolRemoteRepositoryTest {

    private val rolApi = mockk<RolApiService>()
    private val repository = RolRemoteRepository(rolApi)

    @Test
    fun obtenerTodosLosRoles_retorna_lista_valida() = runBlocking {
        val roles = listOf(
            RolDTO(idRol = 1L, nombreRol = "ADMIN", descripcion = "Administrador del sistema"),
            RolDTO(idRol = 2L, nombreRol = "VENDEDOR", descripcion = "Vendedor de productos"),
            RolDTO(idRol = 3L, nombreRol = "CLIENTE", descripcion = "Cliente de la tienda")
        )

        coEvery { rolApi.obtenerTodosLosRoles() } returns Response.success(roles)

        val result = repository.obtenerTodosLosRoles()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        assertEquals("ADMIN", result.getOrNull()!![0].nombreRol)
    }

    @Test
    fun obtenerRolPorId_retorna_rol_valido() = runBlocking {
        val rol = RolDTO(idRol = 1L, nombreRol = "ADMIN", descripcion = "Administrador")

        coEvery { rolApi.obtenerRolPorId(1L) } returns Response.success(rol)

        val result = repository.obtenerRolPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull()?.nombreRol)
    }

    @Test
    fun obtenerRolPorNombre_retorna_rol_valido() = runBlocking {
        val rol = RolDTO(idRol = 1L, nombreRol = "ADMIN", descripcion = "Administrador")

        coEvery { rolApi.obtenerRolPorNombre("ADMIN") } returns Response.success(rol)

        val result = repository.obtenerRolPorNombre("ADMIN")

        assertTrue(result.isSuccess)
        assertEquals("ADMIN", result.getOrNull()?.nombreRol)
    }

    @Test
    fun crearRol_retorna_exito() = runBlocking {
        val nuevoRol = RolDTO(idRol = null, nombreRol = "GERENTE", descripcion = "Gerente de tienda")
        val rolCreado = nuevoRol.copy(idRol = 4L)

        coEvery { rolApi.crearRol(nuevoRol) } returns Response.success(rolCreado)

        val result = repository.crearRol(nuevoRol)

        assertTrue(result.isSuccess)
        assertEquals(4L, result.getOrNull()?.idRol)
        assertEquals("GERENTE", result.getOrNull()?.nombreRol)
    }

    @Test
    fun actualizarRol_retorna_exito() = runBlocking {
        val rolActualizado = RolDTO(idRol = 1L, nombreRol = "ADMIN", descripcion = "Administrador actualizado")

        coEvery { rolApi.actualizarRol(1L, rolActualizado) } returns Response.success(rolActualizado)

        val result = repository.actualizarRol(1L, rolActualizado)

        assertTrue(result.isSuccess)
        assertEquals("Administrador actualizado", result.getOrNull()?.descripcion)
    }

    @Test
    fun eliminarRol_retorna_exito() = runBlocking {
        coEvery { rolApi.eliminarRol(1L) } returns Response.success(null)

        val result = repository.eliminarRol(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
}

