package com.example.proyectoZapateria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectoZapateria.navigation.Route

sealed interface DrawerItem {

    data class NavItem(
        val label: String,
        val icon: ImageVector,
        val route: String,
        val onClick: () -> Unit
    ) : DrawerItem

    // Un divisor
    data object Divider : DrawerItem

    // Una cabecera de sección
    data class Header(val label: String) : DrawerItem

    // Un item de Logout
    data class Logout(val onClick: () -> Unit) : DrawerItem
}

// Composable para obtener los items de drawer por defecto (público)
@Composable
fun AppDrawer(
    currentRoute: String,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier,
    // Slot para la cabecera (logo, nombre de usuario, etc.)
    header: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = colorScheme.surface
    ) {
        // Renderiza la cabecera personalizada que le pasemos
        header()

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = colorScheme.outlineVariant
        )

        // Itera sobre los items y usa 'when'
        items.forEach { item ->
            when (item) {
                // --- Caso 1: Un item de navegación normal ---
                is DrawerItem.NavItem -> {
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = item.onClick,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = colorScheme.secondaryContainer,
                            unselectedTextColor = colorScheme.onSurface,
                            selectedTextColor = colorScheme.onSecondaryContainer,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            selectedIconColor = colorScheme.secondary
                        )
                    )
                }

                // --- Caso 2: Un divisor ---
                is DrawerItem.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // --- Caso 3: Una cabecera de sección ---
                is DrawerItem.Header -> {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                // --- Caso 4: El item de Logout ---
                is DrawerItem.Logout -> {
                    // Usamos un NavItem normal pero con ícono y color fijos
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión") },
                        label = { Text("Cerrar Sesión") },
                        selected = false,
                        onClick = item.onClick,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

// Cabecera para el drawer PÚBLICO
@Composable
fun PublicDrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        Text(
            text = "StepStyle 👟",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
// Items del drawer PÚBLICO
fun publicDrawerItems(
    isAuthenticated: Boolean,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
): List<DrawerItem> {
    return buildList {
        add(DrawerItem.NavItem("Inicio", Icons.Filled.Home, Route.Home.path, onHome))

        // La misma lógica que tenías antes
        if (!isAuthenticated) {
            add(DrawerItem.NavItem("Login", Icons.AutoMirrored.Filled.Login, Route.Login.path, onLogin))
            add(DrawerItem.NavItem("Registro", Icons.Filled.PersonAdd, Route.Register.path, onRegister))
        }
    }
}
// Cabecera para el drawer AUTENTICADO
@Composable
fun AuthenticatedDrawerHeader(
    username: String,
    role: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}
