package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.persona.PersonaDao
import com.example.proyectoZapateria.data.local.usuario.UsuarioConPersonaYRol
import com.example.proyectoZapateria.data.local.usuario.UsuarioDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para manejar la autenticación y el usuario actual
 */
class AuthRepository(
    private val personaDao: PersonaDao,
    private val usuarioDao: UsuarioDao
) {
    // Estado del usuario actual
    private val _currentUser = MutableStateFlow<UsuarioConPersonaYRol?>(null)
    val currentUser: StateFlow<UsuarioConPersonaYRol?> = _currentUser.asStateFlow()

    /**
     * Establece el usuario actual autenticado
     */
    fun setCurrentUser(user: UsuarioConPersonaYRol?) {
        _currentUser.value = user
    }

    /**
     * Obtiene el usuario actual
     */
    fun getCurrentUser(): UsuarioConPersonaYRol? {
        return _currentUser.value
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        _currentUser.value = null
    }
}

