package com.example.proyectoZapateria.data.local.usuario

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // Insertar nuevo usuario, retorna el ID generado
    @Insert
    suspend fun insert(usuario: UsuarioEntity): Long

    // Actualizar usuario existente
    @Update
    suspend fun update(usuario: UsuarioEntity)

    // Eliminar usuario
    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    // Obtener usuario por ID de persona
    @Query("SELECT * FROM usuario WHERE id_persona = :id")
    suspend fun getById(id: Int): UsuarioEntity?

    // Obtener todos los usuarios
    @Query("SELECT * FROM usuario")
    fun getAll(): Flow<List<UsuarioEntity>>

    // Obtener usuarios por rol
    @Query("SELECT * FROM usuario WHERE id_rol = :idRol")
    fun getByRol(idRol: Int): Flow<List<UsuarioEntity>>

    // Contar total de usuarios
    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun getCount(): Int

    // Contar usuarios por rol específico
    @Query("SELECT COUNT(*) FROM usuario WHERE id_rol = :idRol")
    suspend fun getCountByRol(idRol: Int): Int

    // Eliminar todos los usuarios
    @Query("DELETE FROM usuario")
    suspend fun deleteAll()

    // === Consultas con JOIN a Persona y Rol ===

    // Obtener usuario específico con datos de persona y rol
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE u.id_persona = :id
    """)
    suspend fun getByIdConPersonaYRol(id: Int): UsuarioConPersonaYRol?

    // Obtener todos los usuarios con datos de persona y rol
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        ORDER BY p.nombre ASC, p.apellido ASC
    """)
    fun getAllConPersonaYRol(): Flow<List<UsuarioConPersonaYRol>>

    // Obtener usuarios por rol con datos de persona y rol
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE u.id_rol = :idRol
        ORDER BY p.nombre ASC, p.apellido ASC
    """)
    fun getByRolConPersonaYRol(idRol: Int): Flow<List<UsuarioConPersonaYRol>>

    // Buscar usuario por username con datos completos (útil para login)
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE p.username = :username
    """)
    suspend fun getByUsernameConPersonaYRol(username: String): UsuarioConPersonaYRol?

    // Búsqueda insensible a mayúsculas/minúsculas por username (respaldo)
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE LOWER(p.username) = LOWER(:username)
    """)
    suspend fun getByUsernameConPersonaYRolInsensitive(username: String): UsuarioConPersonaYRol?

    // Buscar usuario por RUT con datos completos
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE p.rut = :rut
    """)
    suspend fun getByRutConPersonaYRol(rut: String): UsuarioConPersonaYRol?

    // Buscar usuarios por nombre, apellido o username
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE p.nombre LIKE '%' || :query || '%' 
           OR p.apellido LIKE '%' || :query || '%'
           OR p.username LIKE '%' || :query || '%'
    """)
    fun searchUsuarios(query: String): Flow<List<UsuarioConPersonaYRol>>

    // Obtener usuarios por estado (activo/inactivo) con datos completos
    @Query("""
        SELECT u.id_persona as idPersona, p.nombre, p.apellido, p.rut,
               p.telefono, p.email, p.username, p.estado,
               u.id_rol as idRol, r.nombre_rol as nombreRol, r.descripcion as descripcionRol
        FROM usuario u
        INNER JOIN persona p ON u.id_persona = p.id_persona
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE p.estado = :estado
        ORDER BY p.nombre ASC, p.apellido ASC
    """)
    fun getByEstadoConPersonaYRol(estado: String): Flow<List<UsuarioConPersonaYRol>>
}
