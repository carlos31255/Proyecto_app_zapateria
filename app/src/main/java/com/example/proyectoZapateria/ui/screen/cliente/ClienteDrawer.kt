package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.ui.components.DrawerItem

// lista de elementos del drawer para el cliente
fun clienteDrawerItems(
    onCatalogo: () -> Unit,
    onPedidos: () -> Unit,
    onPerfil: () -> Unit,
    onLogout: () -> Unit,
    onCart: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem.Header("Explorar"),
        DrawerItem.NavItem(
            label = "Catálogo",
            icon = Icons.Default.ShoppingCart,
            route = Route.ClienteCatalogo.path,
            onClick = onCatalogo
        ),
        DrawerItem.NavItem(
            label = "Mis Pedidos",
            icon = Icons.Default.ShoppingBag,
            route = Route.ClientePedidos.path,
            onClick = onPedidos
        ),
        DrawerItem.NavItem(
            label = "Carrito",
            icon = Icons.Default.ShoppingCart,
            route = Route.ClienteCart.path,
            onClick = onCart
        ),
        DrawerItem.NavItem(
            label = "Mi Perfil",
            icon = Icons.Default.Person,
            route = Route.ClientePerfil.path,
            onClick = onPerfil
        ),
        DrawerItem.Divider,
        DrawerItem.Logout(onClick = onLogout)
    )
}
