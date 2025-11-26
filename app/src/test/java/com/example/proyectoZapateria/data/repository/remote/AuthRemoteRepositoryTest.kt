package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.RolDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AuthRemoteRepositoryTest {

    // Mocks
    private val personaRemoteRepository = mockk<PersonaRemoteRepository>()
    private val usuarioRemoteRepository = mockk<UsuarioRemoteRepository>()
    private val rolRemoteRepository = mockk<RolRemoteRepository>()
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)

    // Repositorio bajo prueba
    private val repository = AuthRemoteRepository(
        personaRemoteRepository,
        usuarioRemoteRepository,
        rolRemoteRepository,
        sessionPreferences
    )

    @Test
    fun login_retorna_usuario_completo_y_guarda_sesion() = runBlocking {
        // Arrange
        val username = "usuario@test.com"
        val password = "password123"

        val persona = PersonaDTO(
            idPersona = 1L,
            nombre = "Juan",
            apellido = "Pérez",
            rut = "12345678-9",
            telefono = "987654321",
            email = username,
            idComuna = 1L,
            calle = "Calle Falsa",
            numeroPuerta = "123",
            username = username,
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        val usuario = UsuarioDTO(
            idPersona = 1L,
            idRol = 3L,
            nombreCompleto = "Juan Pérez",
            username = username,
            nombreRol = "Cliente",
            activo = true
        )

        val rol = RolDTO(
            idRol = 3L,
            nombreRol = "Cliente",
            descripcion = "Rol de cliente"
        )

        coEvery { personaRemoteRepository.verificarCredenciales(username, password) } returns Result.success(persona)
        coEvery { usuarioRemoteRepository.obtenerUsuarioPorId(1L) } returns Result.success(usuario)
        coEvery { rolRemoteRepository.obtenerRolPorId(3L) } returns Result.success(rol)

        // Act
        val result = repository.login(username, password)

        // Assert
        assertTrue(result.isSuccess)
        val usuarioCompleto = result.getOrNull()!!
        assertEquals(1L, usuarioCompleto.idPersona)
        assertEquals("Juan", usuarioCompleto.nombre)
        assertEquals("Pérez", usuarioCompleto.apellido)
        assertEquals(username, usuarioCompleto.email)
        assertEquals("Cliente", usuarioCompleto.nombreRol)
        assertTrue(usuarioCompleto.activo)

        // Verificar que se guardó en SessionPreferences (suspend function)
        coVerify(exactly = 1) {
            sessionPreferences.saveSession(
                userId = 1L,
                username = username,
                userRole = "Cliente",
                userRoleId = 3L
            )
        }

        // Verificar que se actualizó el currentUser
        assertEquals(usuarioCompleto, repository.getCurrentUser())
    }

    @Test
    fun register_crea_usuario_retorna_usuario_completo() = runBlocking {
        // Arrange
        val nombre = "María"
        val apellido = "González"
        val email = "maria@test.com"
        val telefono = "912345678"
        val rut = "22222222-2"
        val password = "password123"
        val calle = "Av. Principal"
        val numeroPuerta = "456"

        val personaCreada = PersonaDTO(
            idPersona = 5L,
            nombre = nombre,
            apellido = apellido,
            rut = rut,
            telefono = telefono,
            email = email,
            idComuna = null,
            calle = calle,
            numeroPuerta = numeroPuerta,
            username = email,
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        val usuarioCreado = UsuarioDTO(
            idPersona = 5L,
            idRol = 3L,
            nombreCompleto = "$nombre $apellido",
            username = email,
            nombreRol = "Cliente",
            activo = true
        )

        coEvery { personaRemoteRepository.crearPersona(any()) } returns Result.success(personaCreada)
        coEvery { usuarioRemoteRepository.crearUsuario(any()) } returns Result.success(usuarioCreado)

        // Act
        val result = repository.register(nombre, apellido, email, telefono, rut, password, idComuna = null, calle = calle, numeroPuerta = numeroPuerta)

        // Assert
        assertTrue(result.isSuccess)
        val usuarioCompleto = result.getOrNull()!!
        assertEquals(5L, usuarioCompleto.idPersona)
        assertEquals(nombre, usuarioCompleto.nombre)
        assertEquals(apellido, usuarioCompleto.apellido)
        assertEquals(email, usuarioCompleto.email)
        assertEquals(3L, usuarioCompleto.idRol)
        assertEquals("Cliente", usuarioCompleto.nombreRol)
        assertTrue(usuarioCompleto.activo)

        // Verificar que se llamó a crearPersona con password
        coVerify {
            personaRemoteRepository.crearPersona(match {
                it.password == password &&
                it.nombre == nombre &&
                it.email == email
            })
        }
    }

    @Test
    fun obtenerUsuarioPorId_retorna_usuario_completo_activo() = runBlocking {
        // Arrange
        val idPersona = 1L

        val persona = PersonaDTO(
            idPersona = idPersona,
            nombre = "Carlos",
            apellido = "Ramírez",
            rut = "11111111-1",
            telefono = "934567890",
            email = "carlos@test.com",
            idComuna = 2L,
            calle = "Calle Real",
            numeroPuerta = "321",
            username = "carlos",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        val usuario = UsuarioDTO(
            idPersona = idPersona,
            idRol = 2L,
            nombreCompleto = "Carlos Ramírez",
            username = "carlos",
            nombreRol = "Vendedor",
            activo = true
        )

        val rol = RolDTO(
            idRol = 2L,
            nombreRol = "Vendedor",
            descripcion = "Rol de vendedor"
        )

        coEvery { personaRemoteRepository.obtenerPersonaPorId(idPersona) } returns Result.success(persona)
        coEvery { usuarioRemoteRepository.obtenerUsuarioPorId(idPersona) } returns Result.success(usuario)
        coEvery { rolRemoteRepository.obtenerRolPorId(2L) } returns Result.success(rol)

        // Act
        val result = repository.obtenerUsuarioPorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        val usuarioCompleto = result.getOrNull()!!
        assertEquals(idPersona, usuarioCompleto.idPersona)
        assertEquals("Carlos", usuarioCompleto.nombre)
        assertEquals("Vendedor", usuarioCompleto.nombreRol)

        // Verificar que se guardó la sesión
        coVerify {
            sessionPreferences.saveSession(
                userId = idPersona,
                username = "carlos",
                userRole = "Vendedor",
                userRoleId = 2L
            )
        }
    }
}
