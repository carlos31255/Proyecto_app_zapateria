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
    onRegister: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem("Inicio", Icons.Filled.Home, onHome),
        DrawerItem("Login", Icons.AutoMirrored.Filled.Login, onLogin),
        DrawerItem("Registro", Icons.Filled.PersonAdd, onRegister)
    )
}

@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    // Colores del tema oscuro
    val darkLeather = Color(0xFF2C2416)
    val cream = Color(0xFFD4C5B0)
    val lightBrown = Color(0xFF8B7355)

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = Color(0xFF3D3228)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "StepStyle 👟",
            style = MaterialTheme.typography.titleLarge,
            color = cream,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = lightBrown.copy(alpha = 0.3f)
        )

        items.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.label.lowercase(),
                onClick = item.onClick,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    selectedContainerColor = lightBrown.copy(alpha = 0.3f),
                    unselectedTextColor = cream,
                    selectedTextColor = cream,
                    unselectedIconColor = lightBrown,
                    selectedIconColor = cream
                )
            )
        }
    }
}
