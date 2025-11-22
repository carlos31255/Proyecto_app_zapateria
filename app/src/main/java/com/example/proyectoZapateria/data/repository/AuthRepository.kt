package com.example.proyectoZapateria.data.repository

import android.util.Log
import com.example.proyectoZapateria.data.model.UsuarioCompleto
import com.example.proyectoZapateria.data.remote.usuario.dto.PersonaDTO
import com.example.proyectoZapateria.data.remote.usuario.dto.UsuarioDTO
import com.example.proyectoZapateria.data.repository.remote.PersonaRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.RolRemoteRepository
import com.example.proyectoZapateria.data.repository.remote.UsuarioRemoteRepository
import com.example.proyectoZapateria.data.localstorage.SessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Repositorio para manejar la autenticación y el usuario actual usando microservicios remotos
@Singleton
class AuthRepository @Inject constructor(
    private val personaRemoteRepository: PersonaRemoteRepository,
    private val usuarioRemoteRepository: UsuarioRemoteRepository,
    private val rolRemoteRepository: RolRemoteRepository,
    private val sessionPreferences: SessionPreferences
) {
    // Estado del usuario actual
    private val _currentUser = MutableStateFlow<UsuarioCompleto?>(null)
    val currentUser: StateFlow<UsuarioCompleto?> = _currentUser.asStateFlow()

    // Autentica un usuario con username/email y contraseña usando el endpoint de verificación de credenciales del microservicio
    suspend fun login(username: String, password: String): Result<UsuarioCompleto> {
        return try {
            // 1. Verificar credenciales en el microservicio
            val personaResult = personaRemoteRepository.verificarCredenciales(username, password)

            if (personaResult.isFailure) {
                return Result.failure(personaResult.exceptionOrNull() ?: Exception("Error al verificar credenciales"))
            }

            val persona = personaResult.getOrNull() ?: return Result.failure(Exception("Usuario no encontrado"))

            // Obtener datos del usuario (rol)
            val usuarioResult = usuarioRemoteRepository.obtenerUsuarioPorId(persona.idPersona ?: 0)

            if (usuarioResult.isFailure) {
                return Result.failure(usuarioResult.exceptionOrNull() ?: Exception("Error al obtener datos del usuario"))
            }

            val usuario = usuarioResult.getOrNull() ?: return Result.failure(Exception("Datos de usuario no encontrados"))

            // 3. Verificar que el usuario esté activo
            if (usuario.activo == false || persona.estado != "activo") {
                return Result.failure(Exception("Usuario inactivo. Contacte al administrador"))
            }

            // 4. Obtener el rol
            val rolResult = rolRemoteRepository.obtenerRolPorId(usuario.idRol ?: 0)
            val rol = rolResult.getOrNull()

            // 5. Crear el UsuarioCompleto
            val usuarioCompleto = UsuarioCompleto(
                idPersona = persona.idPersona ?: 0,
                nombre = persona.nombre ?: "",
                apellido = persona.apellido ?: "",
                rut = persona.rut ?: "",
                telefono = persona.telefono ?: "",
                email = persona.email ?: "",
                idComuna = persona.idComuna,
                calle = persona.calle ?: "",
                numeroPuerta = persona.numeroPuerta ?: "",
                username = persona.username ?: "",
                fechaRegistro = persona.fechaRegistro ?: 0L,
                estado = persona.estado ?: "activo",
                idRol = usuario.idRol ?: 0,
                nombreRol = rol?.nombreRol ?: usuario.nombreRol ?: "",
                activo = usuario.activo ?: true
            )


            // 6. Guardar el usuario autenticado
            setCurrentUser(usuarioCompleto)

            // Guardar sesión en DataStore (SessionPreferences)
            try {
                sessionPreferences.saveSession(
                    userId = usuarioCompleto.idPersona,
                    username = usuarioCompleto.username,
                    userRole = usuarioCompleto.nombreRol,
                    userRoleId = usuarioCompleto.idRol
                )
            } catch (e: Exception) {
                Log.w("AuthRepository", "No se pudo guardar la sesión en preferences: ${e.message}")
            }

            Result.success(usuarioCompleto)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Registra un nuevo usuario con rol de cliente
    suspend fun register(
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        password: String,
        calle: String,
        numeroPuerta: String
    ): Result<UsuarioCompleto> {
        return try {
            // 1. Crear la persona en el microservicio
            val personaDTO = PersonaDTO(
                idPersona = null,
                nombre = nombre,
                apellido = apellido,
                rut = "00000000-0",
                telefono = telefono,
                email = email,
                idComuna = null,
                calle = calle,
                numeroPuerta = numeroPuerta,
                username = email,
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                password = password // Enviar la contraseña para que el backend la hashee
            )

            // Nota: El microservicio maneja el hash de la contraseña
            // Si necesitas enviar la contraseña, agrégala al DTO
            val personaResult = personaRemoteRepository.crearPersona(personaDTO)

            if (personaResult.isFailure) {
                return Result.failure(personaResult.exceptionOrNull() ?: Exception("Error al crear persona"))
            }

            val personaCreada = personaResult.getOrNull() ?: return Result.failure(Exception("No se pudo crear la persona"))

            // 2. Crear el usuario con rol de cliente (3)
            val usuarioDTO = UsuarioDTO(
                idPersona = personaCreada.idPersona ?: 0,
                idRol = 3, // Cliente
                nombreCompleto = "$nombre $apellido",
                username = email,
                nombreRol = "Cliente",
                activo = true
            )

            val usuarioResult = usuarioRemoteRepository.crearUsuario(usuarioDTO)

            if (usuarioResult.isFailure) {
                return Result.failure(usuarioResult.exceptionOrNull() ?: Exception("Error al crear usuario"))
            }

            // 3. Crear el UsuarioCompleto
            val usuarioCompleto = UsuarioCompleto(
                idPersona = personaCreada.idPersona ?: 0,
                nombre = nombre,
                apellido = apellido,
                rut = "00000000-0",
                telefono = telefono,
                email = email,
                idComuna = null,
                calle = numeroPuerta,
                numeroPuerta = numeroPuerta,
                username = email,
                fechaRegistro = System.currentTimeMillis(),
                estado = "activo",
                idRol = 3, // Cliente
                nombreRol = "Cliente",
                activo = true
            )

            Result.success(usuarioCompleto)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registro: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Obtiene un usuario por su ID para restaurar sesión
    suspend fun obtenerUsuarioPorId(idPersona: Long): Result<UsuarioCompleto> {
        return try {
            // 1. Obtener persona
            val personaResult = personaRemoteRepository.obtenerPersonaPorId(idPersona)
            if (personaResult.isFailure) {
                return Result.failure(personaResult.exceptionOrNull() ?: Exception("Error al obtener persona"))
            }

            val persona = personaResult.getOrNull() ?: return Result.failure(Exception("Persona no encontrada"))

            // 2. Obtener usuario
            val usuarioResult = usuarioRemoteRepository.obtenerUsuarioPorId(idPersona)
            if (usuarioResult.isFailure) {
                return Result.failure(usuarioResult.exceptionOrNull() ?: Exception("Error al obtener usuario"))
            }

            val usuario = usuarioResult.getOrNull() ?: return Result.failure(Exception("Usuario no encontrado"))

            // 3. Verificar que esté activo
            if (usuario.activo == false || persona.estado != "activo") {
                return Result.failure(Exception("Usuario inactivo"))
            }

            // 4. Obtener rol
            val rolResult = rolRemoteRepository.obtenerRolPorId(usuario.idRol ?: 0L)
            val rol = rolResult.getOrNull()

            // 5. Crear UsuarioCompleto
            val usuarioCompleto = UsuarioCompleto(
                idPersona = persona.idPersona ?: 0L,
                nombre = persona.nombre ?: "",
                apellido = persona.apellido ?: "",
                rut = persona.rut ?: "",
                telefono = persona.telefono ?: "",
                email = persona.email ?: "",
                idComuna = persona.idComuna,
                calle = persona.calle ?: "",
                numeroPuerta = persona.numeroPuerta ?: "",
                username = persona.username ?: "",
                fechaRegistro = persona.fechaRegistro ?: 0L,
                estado = persona.estado ?: "activo",
                idRol = usuario.idRol ?: 0L,
                nombreRol = rol?.nombreRol ?: usuario.nombreRol ?: "",
                activo = usuario.activo ?: true
            )

            // Guardar sesión en DataStore (SessionPreferences)
            try {
                sessionPreferences.saveSession(
                    userId = usuarioCompleto.idPersona,
                    username = usuarioCompleto.username,
                    userRole = usuarioCompleto.nombreRol,
                    userRoleId = usuarioCompleto.idRol
                )
            } catch (e: Exception) {
                Log.w("AuthRepository", "No se pudo guardar la sesión en preferences: ${e.message}")
            }

            Result.success(usuarioCompleto)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al obtener usuario por ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Establece el usuario actual autenticado
    fun setCurrentUser(user: UsuarioCompleto?) {
        _currentUser.value = user
    }

    // Obtiene el usuario actual
    fun getCurrentUser(): UsuarioCompleto? {
        return _currentUser.value
    }

    // Verifica si hay un usuario autenticado
    fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }

    // Cierra la sesión del usuario actual
    fun logout() {
        _currentUser.value = null
        // Borrar DataStore
        try {
            // launch a coroutine? but repository is not a coroutine scope; leave clearing to caller
            // sessionPreferences.clearSession() should be called by the caller when needed
        } catch (_: Exception) {
        }
    }
}
