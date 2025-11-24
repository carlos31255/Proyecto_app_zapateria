package com.example.proyectoZapateria.data.repository.remote

import android.util.Log
import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PersonaRemoteRepositoryTest {

    private lateinit var personaApi: PersonaApiService
    private lateinit var repository: PersonaRemoteRepository

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

        personaApi = mockk()
        repository = PersonaRemoteRepository(personaApi)
    }

    // ==========================================
    // Tests de OBTENER TODAS LAS PERSONAS
    // ==========================================

    @Test
    fun `obtenerTodasLasPersonas retorna lista valida cuando hay personas`() = runBlocking {
        // Arrange
        val personas = listOf(
            PersonaDTO(
                idPersona = 1L,
                nombre = "Juan",
                apellido = "Pérez",
                rut = "12345678-9",
                telefono = "987654321",
                email = "juan@test.com",
                idComuna = 1L,
                calle = "Calle Falsa",
                numeroPuerta = "123",
                username = "juan",
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                password = null
            ),
            PersonaDTO(
                idPersona = 2L,
                nombre = "María",
                apellido = "González",
                rut = "98765432-1",
                telefono = "912345678",
                email = "maria@test.com",
                idComuna = 2L,
                calle = "Av. Principal",
                numeroPuerta = "456",
                username = "maria",
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                password = null
            )
        )

        coEvery { personaApi.obtenerTodasLasPersonas() } returns personas

        // Act
        val result = repository.obtenerTodasLasPersonas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Juan", result.getOrNull()!![0].nombre)
        assertEquals("María", result.getOrNull()!![1].nombre)

        coVerify(exactly = 1) { personaApi.obtenerTodasLasPersonas() }
    }

    @Test
    fun `obtenerTodasLasPersonas retorna lista vacia cuando no hay personas`() = runBlocking {
        // Arrange
        coEvery { personaApi.obtenerTodasLasPersonas() } returns emptyList()

        // Act
        val result = repository.obtenerTodasLasPersonas()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `obtenerTodasLasPersonas maneja UnknownHostException`() = runBlocking {
        // Arrange
        coEvery { personaApi.obtenerTodasLasPersonas() } throws UnknownHostException("No internet")

        // Act
        val result = repository.obtenerTodasLasPersonas()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Sin conexión") ?: false)
    }

    @Test
    fun `obtenerTodasLasPersonas maneja SocketTimeoutException`() = runBlocking {
        // Arrange
        coEvery { personaApi.obtenerTodasLasPersonas() } throws SocketTimeoutException("Timeout")

        // Act
        val result = repository.obtenerTodasLasPersonas()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Timeout") ?: false)
    }

    @Test
    fun `obtenerTodasLasPersonas maneja IOException`() = runBlocking {
        // Arrange
        coEvery { personaApi.obtenerTodasLasPersonas() } throws java.io.IOException("IO error")

        // Act
        val result = repository.obtenerTodasLasPersonas()

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error de red") ?: false)
    }

    // ==========================================
    // Tests de OBTENER PERSONA POR ID
    // ==========================================

    @Test
    fun `obtenerPersonaPorId retorna persona cuando existe`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val persona = PersonaDTO(
            idPersona = idPersona,
            nombre = "Juan",
            apellido = "Pérez",
            rut = "12345678-9",
            telefono = "987654321",
            email = "juan@test.com",
            idComuna = 1L,
            calle = "Calle Falsa",
            numeroPuerta = "123",
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorId(idPersona) } returns Response.success(persona)

        // Act
        val result = repository.obtenerPersonaPorId(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(idPersona, result.getOrNull()!!.idPersona)
        assertEquals("Juan", result.getOrNull()!!.nombre)
        assertEquals("Pérez", result.getOrNull()!!.apellido)

        coVerify(exactly = 1) { personaApi.obtenerPersonaPorId(idPersona) }
    }

    @Test
    fun `obtenerPersonaPorId retorna error cuando no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        coEvery { personaApi.obtenerPersonaPorId(idPersona) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.obtenerPersonaPorId(idPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `obtenerPersonaPorId maneja excepcion`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { personaApi.obtenerPersonaPorId(idPersona) } throws Exception("Network error")

        // Act
        val result = repository.obtenerPersonaPorId(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de OBTENER PERSONA POR RUT
    // ==========================================

    @Test
    fun `obtenerPersonaPorRut retorna persona cuando existe`() = runBlocking {
        // Arrange
        val rut = "12345678-9"
        val persona = PersonaDTO(
            idPersona = 1L,
            nombre = "Juan",
            apellido = "Pérez",
            rut = rut,
            telefono = "987654321",
            email = "juan@test.com",
            idComuna = 1L,
            calle = "Calle Falsa",
            numeroPuerta = "123",
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorRut(rut) } returns Response.success(persona)

        // Act
        val result = repository.obtenerPersonaPorRut(rut)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(rut, result.getOrNull()!!.rut)
        assertEquals("Juan", result.getOrNull()!!.nombre)

        coVerify(exactly = 1) { personaApi.obtenerPersonaPorRut(rut) }
    }

    @Test
    fun `obtenerPersonaPorRut retorna error cuando no existe`() = runBlocking {
        // Arrange
        val rut = "99999999-9"
        coEvery { personaApi.obtenerPersonaPorRut(rut) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.obtenerPersonaPorRut(rut)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de OBTENER PERSONA POR USERNAME
    // ==========================================

    @Test
    fun `obtenerPersonaPorUsername retorna persona cuando existe`() = runBlocking {
        // Arrange
        val username = "juan"
        val persona = PersonaDTO(
            idPersona = 1L,
            nombre = "Juan",
            apellido = "Pérez",
            rut = "12345678-9",
            telefono = "987654321",
            email = "juan@test.com",
            idComuna = 1L,
            calle = "Calle Falsa",
            numeroPuerta = "123",
            username = username,
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorUsername(username) } returns Response.success(persona)

        // Act
        val result = repository.obtenerPersonaPorUsername(username)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(username, result.getOrNull()!!.username)
        assertEquals("Juan", result.getOrNull()!!.nombre)

        coVerify(exactly = 1) { personaApi.obtenerPersonaPorUsername(username) }
    }

    @Test
    fun `obtenerPersonaPorUsername retorna error cuando no existe`() = runBlocking {
        // Arrange
        val username = "noexiste"
        coEvery { personaApi.obtenerPersonaPorUsername(username) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.obtenerPersonaPorUsername(username)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de BUSCAR PERSONAS POR NOMBRE
    // ==========================================

    @Test
    fun `buscarPersonasPorNombre retorna personas que coinciden`() = runBlocking {
        // Arrange
        val nombre = "Juan"
        val personas = listOf(
            PersonaDTO(
                idPersona = 1L,
                nombre = "Juan",
                apellido = "Pérez",
                rut = "12345678-9",
                telefono = "987654321",
                email = "juan@test.com",
                idComuna = 1L,
                calle = "Calle Falsa",
                numeroPuerta = "123",
                username = "juan",
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                password = null
            ),
            PersonaDTO(
                idPersona = 2L,
                nombre = "Juana",
                apellido = "López",
                rut = "11111111-1",
                telefono = "923456789",
                email = "juana@test.com",
                idComuna = 2L,
                calle = "Av. Central",
                numeroPuerta = "789",
                username = "juana",
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                password = null
            )
        )

        coEvery { personaApi.buscarPersonasPorNombre(nombre) } returns personas

        // Act
        val result = repository.buscarPersonasPorNombre(nombre)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.nombre!!.contains("Juan") })
    }

    @Test
    fun `buscarPersonasPorNombre retorna lista vacia cuando no hay coincidencias`() = runBlocking {
        // Arrange
        val nombre = "Inexistente"
        coEvery { personaApi.buscarPersonasPorNombre(nombre) } returns emptyList()

        // Act
        val result = repository.buscarPersonasPorNombre(nombre)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `buscarPersonasPorNombre maneja UnknownHostException`() = runBlocking {
        // Arrange
        val nombre = "Juan"
        coEvery { personaApi.buscarPersonasPorNombre(nombre) } throws UnknownHostException("No internet")

        // Act
        val result = repository.buscarPersonasPorNombre(nombre)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Sin conexión") ?: false)
    }

    // ==========================================
    // Tests de OBTENER PERSONAS POR ESTADO
    // ==========================================

    @Test
    fun `obtenerPersonasPorEstado retorna personas activas`() = runBlocking {
        // Arrange
        val estado = "activo"
        val personas = listOf(
            PersonaDTO(
                idPersona = 1L,
                nombre = "Juan",
                apellido = "Pérez",
                rut = "12345678-9",
                telefono = "987654321",
                email = "juan@test.com",
                idComuna = 1L,
                calle = "Calle Falsa",
                numeroPuerta = "123",
                username = "juan",
                fechaRegistro = System.currentTimeMillis(),
                estado = estado,
                password = null
            ),
            PersonaDTO(
                idPersona = 2L,
                nombre = "María",
                apellido = "González",
                rut = "98765432-1",
                telefono = "912345678",
                email = "maria@test.com",
                idComuna = 2L,
                calle = "Av. Principal",
                numeroPuerta = "456",
                username = "maria",
                fechaRegistro = System.currentTimeMillis(),
                estado = estado,
                password = null
            )
        )

        coEvery { personaApi.obtenerPersonasPorEstado(estado) } returns personas

        // Act
        val result = repository.obtenerPersonasPorEstado(estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.estado == estado })
    }

    @Test
    fun `obtenerPersonasPorEstado retorna personas inactivas`() = runBlocking {
        // Arrange
        val estado = "inactivo"
        val personas = listOf(
            PersonaDTO(
                idPersona = 3L,
                nombre = "Carlos",
                apellido = "Ramírez",
                rut = "11111111-1",
                telefono = "934567890",
                email = "carlos@test.com",
                idComuna = 3L,
                calle = "Calle Real",
                numeroPuerta = "321",
                username = "carlos",
                fechaRegistro = System.currentTimeMillis(),
                estado = estado,
                password = null
            )
        )

        coEvery { personaApi.obtenerPersonasPorEstado(estado) } returns personas

        // Act
        val result = repository.obtenerPersonasPorEstado(estado)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(estado, result.getOrNull()!![0].estado)
    }

    @Test
    fun `obtenerPersonasPorEstado maneja IOException`() = runBlocking {
        // Arrange
        val estado = "activo"
        coEvery { personaApi.obtenerPersonasPorEstado(estado) } throws java.io.IOException("Network error")

        // Act
        val result = repository.obtenerPersonasPorEstado(estado)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error de red") ?: false)
    }

    // ==========================================
    // Tests de CREAR PERSONA
    // ==========================================

    @Test
    fun `crearPersona crea correctamente`() = runBlocking {
        // Arrange
        val nuevaPersona = PersonaDTO(
            idPersona = null,
            nombre = "Pedro",
            apellido = "López",
            rut = "22222222-2",
            telefono = "945678901",
            email = "pedro@test.com",
            idComuna = 4L,
            calle = "Calle Nueva",
            numeroPuerta = "654",
            username = "pedro",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = "password123"
        )

        val personaCreada = nuevaPersona.copy(idPersona = 10L, password = null)

        coEvery { personaApi.crearPersona(nuevaPersona) } returns Response.success(personaCreada)

        // Act
        val result = repository.crearPersona(nuevaPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()!!.idPersona)
        assertEquals("Pedro", result.getOrNull()!!.nombre)
        assertNull(result.getOrNull()!!.password) // Password no debe retornarse

        coVerify(exactly = 1) { personaApi.crearPersona(nuevaPersona) }
    }

    @Test
    fun `crearPersona falla cuando email ya existe`() = runBlocking {
        // Arrange
        val nuevaPersona = PersonaDTO(
            idPersona = null,
            nombre = "Pedro",
            apellido = "López",
            rut = "22222222-2",
            telefono = "945678901",
            email = "existente@test.com",
            idComuna = 4L,
            calle = "Calle Nueva",
            numeroPuerta = "654",
            username = "pedro",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = "password123"
        )

        coEvery { personaApi.crearPersona(nuevaPersona) } returns Response.error(
            409,
            "Email already exists".toResponseBody()
        )

        // Act
        val result = repository.crearPersona(nuevaPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ACTUALIZAR PERSONA
    // ==========================================

    @Test
    fun `actualizarPersona actualiza correctamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val personaActualizada = PersonaDTO(
            idPersona = idPersona,
            nombre = "Juan",
            apellido = "Pérez Actualizado",
            rut = "12345678-9",
            telefono = "999999999",
            email = "juan.nuevo@test.com",
            idComuna = 1L,
            calle = "Calle Nueva",
            numeroPuerta = "999",
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.actualizarPersona(idPersona, personaActualizada) } returns Response.success(personaActualizada)

        // Act
        val result = repository.actualizarPersona(idPersona, personaActualizada)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Pérez Actualizado", result.getOrNull()!!.apellido)
        assertEquals("999999999", result.getOrNull()!!.telefono)
        assertEquals("juan.nuevo@test.com", result.getOrNull()!!.email)

        coVerify(exactly = 1) { personaApi.actualizarPersona(idPersona, personaActualizada) }
    }

    @Test
    fun `actualizarPersona falla cuando persona no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        val personaActualizada = PersonaDTO(
            idPersona = idPersona,
            nombre = "No",
            apellido = "Existe",
            rut = "99999999-9",
            telefono = "900000000",
            email = "noexiste@test.com",
            idComuna = 1L,
            calle = "Calle",
            numeroPuerta = "0",
            username = "noexiste",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.actualizarPersona(idPersona, personaActualizada) } returns Response.error(
            404,
            "Not found".toResponseBody()
        )

        // Act
        val result = repository.actualizarPersona(idPersona, personaActualizada)

        // Assert
        assertTrue(result.isFailure)
    }

    // ==========================================
    // Tests de ELIMINAR PERSONA
    // ==========================================

    @Test
    fun `eliminarPersona elimina correctamente`() = runBlocking {
        // Arrange
        val idPersona = 1L
        val emptyResponse = Response.success<Void>(204, null)

        coEvery { personaApi.eliminarPersona(idPersona) } returns emptyResponse

        // Act
        val result = repository.eliminarPersona(idPersona)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)

        coVerify(exactly = 1) { personaApi.eliminarPersona(idPersona) }
    }

    @Test
    fun `eliminarPersona falla cuando persona no existe`() = runBlocking {
        // Arrange
        val idPersona = 999L
        val errorResponse = Response.error<Void>(404, "Not found".toResponseBody())

        coEvery { personaApi.eliminarPersona(idPersona) } returns errorResponse

        // Act
        val result = repository.eliminarPersona(idPersona)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `eliminarPersona maneja excepcion`() = runBlocking {
        // Arrange
        val idPersona = 1L
        coEvery { personaApi.eliminarPersona(idPersona) } throws Exception("Network error")

        // Act
        val result = repository.eliminarPersona(idPersona)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ==========================================
    // Tests de VERIFICAR CREDENCIALES
    // ==========================================

    @Test
    fun `verificarCredenciales retorna persona cuando credenciales son correctas`() = runBlocking {
        // Arrange
        val username = "juan"
        val password = "password123"
        val persona = PersonaDTO(
            idPersona = 1L,
            nombre = "Juan",
            apellido = "Pérez",
            rut = "12345678-9",
            telefono = "987654321",
            email = "juan@test.com",
            idComuna = 1L,
            calle = "Calle Falsa",
            numeroPuerta = "123",
            username = username,
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.verificarCredenciales(username, password) } returns Response.success(persona)

        // Act
        val result = repository.verificarCredenciales(username, password)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(username, result.getOrNull()!!.username)
        assertEquals("Juan", result.getOrNull()!!.nombre)

        coVerify(exactly = 1) { personaApi.verificarCredenciales(username, password) }
    }

    @Test
    fun `verificarCredenciales retorna error cuando credenciales son incorrectas`() = runBlocking {
        // Arrange
        val username = "juan"
        val password = "wrongpassword"

        coEvery { personaApi.verificarCredenciales(username, password) } returns Response.error(
            401,
            "Unauthorized".toResponseBody()
        )

        // Act
        val result = repository.verificarCredenciales(username, password)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `verificarCredenciales maneja excepcion`() = runBlocking {
        // Arrange
        val username = "juan"
        val password = "password123"

        coEvery { personaApi.verificarCredenciales(username, password) } throws Exception("Network error")

        // Act
        val result = repository.verificarCredenciales(username, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}

