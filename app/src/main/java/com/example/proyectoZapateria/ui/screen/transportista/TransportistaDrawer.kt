package com.example.proyectoZapateria.ui.screen.transportista

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.ui.components.DrawerItem

// lista de elementos del drawer para el transportista
fun transportistaDrawerItems(
    onEntregas: () -> Unit,
    onPerfil: () -> Unit,
    onLogout: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem.Header("Gestión"),
        DrawerItem.NavItem(
            label = "Mis Entregas",
            icon = Icons.Default.LocalShipping,
            route = Route.TransportistaListaEntregas.path,
            onClick = onEntregas
        ),
        DrawerItem.NavItem(
            label = "Mi Perfil",
            icon = Icons.Default.Person,
            route = Route.TransportistaPerfil.path,
            onClick = onPerfil
        ),
        DrawerItem.Divider,
        DrawerItem.Logout(onClick = onLogout)
    )
}