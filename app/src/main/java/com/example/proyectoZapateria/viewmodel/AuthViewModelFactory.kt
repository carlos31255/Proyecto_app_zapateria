package com.example.proyectoZapateria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyectoZapateria.data.repository.PersonaRepository
import com.example.proyectoZapateria.data.repository.UsuarioRepository

/**
 * Factory para crear AuthViewModel con sus dependencias inyectadas.
 *
 * ¿Por qué necesitamos esto?
 * Los ViewModels normalmente se crean sin parámetros, pero cuando necesitamos
 * inyectar dependencias (como repositories), Android necesita saber CÓMO crear
 * el ViewModel con esos parámetros. El Factory es esa "receta de construcción".
 */
class AuthViewModelFactory(
    private val personaRepository: PersonaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Si solicitan AuthViewModel, lo creamos con los repositories
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(personaRepository, usuarioRepository) as T
        }
        // Si piden otra clase, lanzamos error descriptivo
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

