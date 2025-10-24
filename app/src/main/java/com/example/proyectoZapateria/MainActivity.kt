package com.example.proyectoZapateria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.proyectoZapateria.data.repository.AppRepositories
import com.example.proyectoZapateria.navigation.AppNavGraph
import com.example.proyectoZapateria.ui.theme.Proyecto_zapateriaTheme
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.AuthViewModelFactory
import com.example.proyectoZapateria.viewmodel.ProductoViewModel
import com.example.proyectoZapateria.viewmodel.ProductoViewModelFactory
import com.example.proyectoZapateria.viewmodel.InventarioViewModel
import com.example.proyectoZapateria.viewmodel.InventarioViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto_zapateriaTheme {
                AppRoot()
            }
        }
    }
}
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

    val productoViewModel: ProductoViewModel = viewModel(
        factory = ProductoViewModelFactory(
            productoRepository = repositories.productoRepository
        )
    )

    val inventarioViewModel: InventarioViewModel = viewModel(
        factory = InventarioViewModelFactory(
            productoRepository = repositories.productoRepository
        )
    )

    // ====== NAVEGACIÓN ======
    val navController = rememberNavController()

    AppNavGraph(
        navController = navController,
        authViewModel = authViewModel,
        productoViewModel = productoViewModel,
        inventarioViewModel = inventarioViewModel
    )
}