package com.example.proyectoZapateria

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.proyectoZapateria.data.repository.AppRepositories
import com.example.proyectoZapateria.navigation.AppNavGraph
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.AuthViewModelFactory

/**
 * Punto de entrada principal de la aplicación.
 * Aquí se construyen todas las dependencias (Composition Root).
 */
@Composable
fun AppRoot() {
    // ====== CONSTRUCCIÓN DE DEPENDENCIAS (Composition Root) ======
    val context = LocalContext.current.applicationContext
    // ^ Obtenemos el applicationContext para construir la base de datos de Room.

    // Creamos todos los repositories en un solo objeto
    val repositories = remember { AppRepositories(context) }
    // ^ Singleton que contiene todos los repositories. No crea múltiples instancias.

    // ====== CREACIÓN DEL VIEWMODEL CON FACTORY ======
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            personaRepository = repositories.personaRepository,
            usuarioRepository = repositories.usuarioRepository
        )
    )
    // ^ Creamos el ViewModel con factory para inyectar los repositories.
    //   Esto reemplaza el uso de listas en memoria (demoUsers).

    // ====== NAVEGACIÓN ======
    val navController = rememberNavController()

    AppNavGraph(navController = navController, authViewModel = authViewModel)
}

