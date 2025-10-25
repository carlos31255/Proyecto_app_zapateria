package com.example.proyectoZapateria.ui.screen.transportista

import android.annotation.SuppressLint
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
import com.example.proyectoZapateria.ui.components.AppDrawer
import com.example.proyectoZapateria.ui.components.AuthenticatedDrawerHeader
import com.example.proyectoZapateria.ui.components.DrawerItem
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

data class TransportistaMenuItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val route: String
)


val transportistaMenuItems = listOf(
    TransportistaMenuItem(
        icon = Icons.Default.LocalShipping,
        title = "Entregas",
        description = "Ver mis entregas",
        route = Route.TransportistaEntregas.path
    ),
    TransportistaMenuItem(
        icon = Icons.Default.Person,
        title = "Mi Perfil",
        description = "Ver mi información",
        route = Route.TransportistaPerfil.path
    )
)

@Composable
fun TransportistaMenuCard(
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
                modifier = Modifier.size(48.dp),
                tint = contentColor
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
                color = contentColor.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
@SuppressLint("SuspiciousIndentation")
@Composable
fun TransportistaHomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val colorScheme = MaterialTheme.colorScheme

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "¿Qué deseas hacer?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de opciones del transportista
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(transportistaMenuItems) { menuItem ->
                    TransportistaMenuCard(
                        icon = menuItem.icon,
                        title = menuItem.title,
                        description = menuItem.description,
                        onClick = {
                            val currentUserId = authViewModel.currentUser.value?.idPersona

                            if (currentUserId != null) {
                                val rutaCompleta = when (menuItem.route) {
                                    // Para rutas que requieren el ID del transportista
                                    Route.TransportistaEntregas.path -> {
                                        menuItem.route.replace("{transportistaId}", currentUserId.toString())
                                    }
                                    Route.TransportistaPerfil.path -> {
                                        menuItem.route.replace("{transportistaId}", currentUserId.toString())
                                    }
                                    else -> menuItem.route
                                }
                                navController.navigate(rutaCompleta)
                            } else {
                                // Para "Mi Perfil" (o cualquier otra ruta), navegamos normal
                                navController.navigate(menuItem.route)
                            }
                        },
                        backgroundColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }







