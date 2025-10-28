package com.example.proyectoZapateria

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.proyectoZapateria.data.local.database.AppDatabase
import com.example.proyectoZapateria.navigation.AppNavGraph
import com.example.proyectoZapateria.ui.theme.Proyecto_zapateriaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forzar la inicialización de la base de datos al inicio
        try {
            Log.d("MainActivity", "Inicializando base de datos...")
            val db = AppDatabase.getInstance(applicationContext)
            Log.d("MainActivity", "Base de datos inicializada exitosamente")

            // Verificar que la base de datos se creó correctamente
            val personaDao = db.personaDao()
            Log.d("MainActivity", "DAOs accesibles correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar base de datos: ${e.message}", e)
        }

        enableEdgeToEdge()
        setContent {
            Proyecto_zapateriaTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}