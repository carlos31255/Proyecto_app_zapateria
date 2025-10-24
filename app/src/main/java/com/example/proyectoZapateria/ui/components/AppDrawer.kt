package com.example.proyectoZapateria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Item del Drawer (menú lateral)
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Lista de items por defecto
fun defaultDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    isAuthenticated: Boolean = false
): List<DrawerItem> {
    return buildList {
        add(DrawerItem("Inicio", Icons.Filled.Home, onHome))
        // Solo agregar Login y Registro si NO está autenticado
        if (!isAuthenticated) {
            add(DrawerItem("Login", Icons.AutoMirrored.Filled.Login, onLogin))
            add(DrawerItem("Registro", Icons.Filled.PersonAdd, onRegister))
        }
    }
}

@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    // Nuevo esquema de colores morado/violeta claro - Material Design 3
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = colorScheme.surface
    ) {
        // Header del Drawer con gradiente morado suave
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Text(
                text = "StepStyle 👟",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onPrimaryContainer
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = colorScheme.outlineVariant
        )

        items.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.label.lowercase(),
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
    }
}
