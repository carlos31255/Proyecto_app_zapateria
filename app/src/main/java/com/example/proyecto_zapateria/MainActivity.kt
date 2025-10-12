package com.example.proyecto_zapateria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyecto_zapateria.navigation.AppNavigation
import com.example.proyecto_zapateria.ui.theme.Proyecto_zapateriaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto_zapateriaTheme {
                AppNavigation()
            }
        }
    }
}
