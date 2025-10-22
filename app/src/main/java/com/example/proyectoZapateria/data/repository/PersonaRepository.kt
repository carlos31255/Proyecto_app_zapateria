package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.persona.PersonaDao
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {

    // Obtener todas las personas
    fun getAllPersonas(): Flow<List<PersonaEntity>> = personaDao.getAll()

    // Obtener personas por estado
    fun getPersonasByEstado(estado: String): Flow<List<PersonaEntity>> = personaDao.getByEstado(estado)

    // Obtener persona por ID
    suspend fun getPersonaById(id: Int): PersonaEntity? {
        return personaDao.getById(id)
    }

    // Obtener persona por username (para login)
    suspend fun getPersonaByUsername(username: String): PersonaEntity? {
        return personaDao.getByUsername(username)
    }

    // Obtener persona por email
    suspend fun getPersonaByEmail(email: String): PersonaEntity? {
        return personaDao.getByEmail(email)
    }

    // Obtener persona por RUT
    suspend fun getPersonaByRut(rut: String): PersonaEntity? {
        return personaDao.getByRut(rut)
    }

    // Buscar personas
    fun searchPersonas(query: String): Flow<List<PersonaEntity>> {
        return personaDao.searchPersonas(query)
    }

    // Insertar nueva persona (con validaciones)
    suspend fun insertPersona(persona: PersonaEntity): Result<Long> {
        return try {
            // Validar que no exista username duplicado
            val existeUsername = personaDao.getByUsername(persona.username) != null
            if (existeUsername) {
                return Result.failure(Exception("El nombre de usuario ya existe"))
            }

            // Validar que no exista RUT duplicado
            val existeRut = personaDao.getByRut(persona.rut) != null
            if (existeRut) {
                return Result.failure(Exception("El RUT ya está registrado"))
            }

            // Validar que no exista email duplicado (si se proporciona)
            if (!persona.email.isNullOrBlank()) {
                val existeEmail = personaDao.getByEmail(persona.email) != null
                if (existeEmail) {
                    return Result.failure(Exception("El email ya está registrado"))
                }
            }

            val id = personaDao.insert(persona)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar persona
    suspend fun updatePersona(persona: PersonaEntity): Result<Unit> {
        return try {
            personaDao.update(persona)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar persona
    suspend fun deletePersona(persona: PersonaEntity): Result<Unit> {
        return try {
            personaDao.delete(persona)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Contar personas
    suspend fun getCount(): Int {
        return personaDao.getCount()
    }

    // Verificar si existe username
    suspend fun existeUsername(username: String): Boolean {
        return personaDao.getByUsername(username) != null
    }

    // Verificar si existe email
    suspend fun existeEmail(email: String): Boolean {
        return personaDao.getByEmail(email) != null
    }

    // Verificar si existe RUT
    suspend fun existeRut(rut: String): Boolean {
        return personaDao.getByRut(rut) != null
    }
}

