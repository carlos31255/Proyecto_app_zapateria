package com.example.proyectoZapateria.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    isAuthenticated: Boolean = false
) {
    // Nuevo esquema de colores morado/violeta claro - Material Design 3
    val colorScheme = MaterialTheme.colorScheme

    TopAppBar(
        title = { Text("Zapateria StepStyle") },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
            }
        },
        actions = {
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Inicio")
            }
            // Solo mostrar Login y Registro si NO está autenticado
            if (!isAuthenticated) {
                IconButton(onClick = onLogin) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Login")
                }
                IconButton(onClick = onRegister) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Registro")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.primaryContainer,
            titleContentColor = colorScheme.onPrimaryContainer,
            navigationIconContentColor = colorScheme.onPrimaryContainer,
            actionIconContentColor = colorScheme.onPrimaryContainer
        )
    )
}
