package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.viewmodel.AuthViewModel

@Composable
fun ClienteHomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Colores de Material Design
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Texto StepStyle con colores de Material Design
                        Row {
                            Text(
                                text = "Step",
                                color = colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Style",
                                color = colorScheme.error,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Panel de Cliente",
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                        currentUser?.let {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Bienvenido, ${it.nombre}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Route.Login.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "¿Qué deseas hacer?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de opciones del cliente
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(clienteMenuItems) { menuItem ->
                    ClienteMenuCard(
                        icon = menuItem.icon,
                        title = menuItem.title,
                        description = menuItem.description,
                        onClick = { navController.navigate(menuItem.route) },
                        backgroundColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ClienteMenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

// Data class para items del menú
data class ClienteMenuItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val route: String
)

// Lista de opciones disponibles para el cliente
private val clienteMenuItems = listOf(
    ClienteMenuItem(
        icon = Icons.Filled.ShoppingCart,
        title = "Catálogo",
        description = "Ver productos",
        route = "catalogo" // TODO: Crear esta ruta
    ),
    ClienteMenuItem(
        icon = Icons.Filled.ShoppingBag,
        title = "Mis Pedidos",
        description = "Ver mis compras",
        route = "mis_pedidos" // TODO: Crear esta ruta
    ),
    ClienteMenuItem(
        icon = Icons.Filled.Favorite,
        title = "Favoritos",
        description = "Productos guardados",
        route = "favoritos" // TODO: Crear esta ruta
    ),
    ClienteMenuItem(
        icon = Icons.Filled.Person,
        title = "Mi Perfil",
        description = "Datos personales",
        route = "perfil" // TODO: Crear esta ruta
    )
)

