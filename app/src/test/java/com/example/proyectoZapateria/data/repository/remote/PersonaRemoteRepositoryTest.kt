package com.example.proyectoZapateria.data.repository.remote

import com.example.proyectoZapateria.data.remote.usuario.PersonaApiService
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class PersonaRemoteRepositoryTest {

    private val personaApi = mockk<PersonaApiService>()
    private val repository = PersonaRemoteRepository(personaApi)

    @Test
    fun obtenerTodasLasPersonas_retorna_lista_valida() = runBlocking {
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

        coEvery { personaApi.obtenerTodasLasPersonas() } returns Response.success(personas)

        val result = repository.obtenerTodasLasPersonas()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Juan", result.getOrNull()!![0].nombre)
    }

    @Test
    fun obtenerPersonaPorId_retorna_persona_valida() = runBlocking {
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
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorId(1L) } returns Response.success(persona)

        val result = repository.obtenerPersonaPorId(1L)

        assertTrue(result.isSuccess)
        assertEquals("Juan", result.getOrNull()!!.nombre)
    }

    @Test
    fun obtenerPersonaPorRut_retorna_persona_valida() = runBlocking {
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
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorRut("12345678-9") } returns Response.success(persona)

        val result = repository.obtenerPersonaPorRut("12345678-9")

        assertTrue(result.isSuccess)
        assertEquals("12345678-9", result.getOrNull()!!.rut)
    }

    @Test
    fun obtenerPersonaPorUsername_retorna_persona_valida() = runBlocking {
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
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.obtenerPersonaPorUsername("juan") } returns Response.success(persona)

        val result = repository.obtenerPersonaPorUsername("juan")

        assertTrue(result.isSuccess)
        assertEquals("juan", result.getOrNull()!!.username)
    }

    @Test
    fun buscarPersonasPorNombre_retorna_lista_valida() = runBlocking {
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

        coEvery { personaApi.buscarPersonasPorNombre("Juan") } returns Response.success(personas)

        val result = repository.buscarPersonasPorNombre("Juan")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun obtenerPersonasPorEstado_retorna_lista_valida() = runBlocking {
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

        coEvery { personaApi.obtenerPersonasPorEstado("activo") } returns Response.success(personas)

        val result = repository.obtenerPersonasPorEstado("activo")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun crearPersona_retorna_exito() = runBlocking {
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

        val result = repository.crearPersona(nuevaPersona)

        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()!!.idPersona)
        assertEquals("Pedro", result.getOrNull()!!.nombre)
    }

    @Test
    fun actualizarPersona_retorna_exito() = runBlocking {
        val personaActualizada = PersonaDTO(
            idPersona = 1L,
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

        coEvery { personaApi.actualizarPersona(1L, personaActualizada) } returns Response.success(personaActualizada)

        val result = repository.actualizarPersona(1L, personaActualizada)

        assertTrue(result.isSuccess)
        assertEquals("Pérez Actualizado", result.getOrNull()!!.apellido)
    }

    @Test
    fun eliminarPersona_retorna_exito() = runBlocking {
        coEvery { personaApi.eliminarPersona(1L) } returns Response.success<Void>(204, null)

        val result = repository.eliminarPersona(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun verificarCredenciales_retorna_persona_valida() = runBlocking {
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
            username = "juan",
            fechaRegistro = System.currentTimeMillis(),
            estado = "activo",
            password = null
        )

        coEvery { personaApi.verificarCredenciales("juan", "password123") } returns Response.success(persona)

        val result = repository.verificarCredenciales("juan", "password123")

        assertTrue(result.isSuccess)
        assertEquals("juan", result.getOrNull()!!.username)
        assertEquals("Juan", result.getOrNull()!!.nombre)
    }
}
