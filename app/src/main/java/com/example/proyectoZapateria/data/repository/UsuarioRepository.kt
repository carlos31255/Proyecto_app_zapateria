package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.usuario.UsuarioConPersonaYRol
import com.example.proyectoZapateria.data.local.usuario.UsuarioDao
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Obtener todos los usuarios
    fun getAllUsuarios(): Flow<List<UsuarioEntity>> = usuarioDao.getAll()

    // Obtener todos los usuarios con datos completos
    fun getAllUsuariosCompletos(): Flow<List<UsuarioConPersonaYRol>> = usuarioDao.getAllConPersonaYRol()

    // Obtener usuario por ID
    suspend fun getUsuarioById(id: Int): UsuarioEntity? {
        return usuarioDao.getById(id)
    }

    // Obtener usuario completo por ID
    suspend fun getUsuarioCompletoById(id: Int): UsuarioConPersonaYRol? {
        return usuarioDao.getByIdConPersonaYRol(id)
    }

    // Obtener usuario por username (para login)
    suspend fun getUsuarioByUsername(username: String): UsuarioConPersonaYRol? {
        // Primero búsqueda exacta
        var res = usuarioDao.getByUsernameConPersonaYRol(username)
        if (res == null) {
            // Intentar búsqueda insensible a mayúsculas/minúsculas
            res = usuarioDao.getByUsernameConPersonaYRolInsensitive(username)
        }
        return res
    }

    // Obtener usuarios por rol
    fun getUsuariosByRol(idRol: Int): Flow<List<UsuarioEntity>> {
        return usuarioDao.getByRol(idRol)
    }

    // Obtener usuarios completos por rol
    fun getUsuariosCompletosByRol(idRol: Int): Flow<List<UsuarioConPersonaYRol>> {
        return usuarioDao.getByRolConPersonaYRol(idRol)
    }

    // Insertar nuevo usuario
    suspend fun insertUsuario(usuario: UsuarioEntity): Result<Long> {
        return try {
            // Verificar que no exista ya un usuario con ese id_persona
            val existe = usuarioDao.getById(usuario.idPersona) != null
            if (existe) {
                return Result.failure(Exception("Ya existe un usuario para esta persona"))
            }

            val id = usuarioDao.insert(usuario)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar usuario
    suspend fun updateUsuario(usuario: UsuarioEntity): Result<Unit> {
        return try {
            usuarioDao.update(usuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar usuario
    suspend fun deleteUsuario(usuario: UsuarioEntity): Result<Unit> {
        return try {
            usuarioDao.delete(usuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Contar usuarios
    suspend fun getCount(): Int {
        return usuarioDao.getCount()
    }

    // Contar usuarios por rol
    suspend fun getCountByRol(idRol: Int): Int {
        return usuarioDao.getCountByRol(idRol)
    }

    // Verificar si existe usuario
    suspend fun existeUsuario(idPersona: Int): Boolean {
        return usuarioDao.getById(idPersona) != null
    }
}
