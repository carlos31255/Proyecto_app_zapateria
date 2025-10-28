package com.example.proyectoZapateria.ui.screen.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.ui.components.DrawerItem

/**
 * Lista de elementos del drawer para el administrador
 */
fun adminDrawerItems(
    onAgregarProducto: () -> Unit,
    onVentas: () -> Unit,
    onClientes: () -> Unit,
    onInventario: () -> Unit,
    onUsuarios: () -> Unit,
    onReportes: () -> Unit,
    onPerfil: () -> Unit,
    onLogout: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem.Header("Gestión de Productos"),
        DrawerItem.NavItem(
            label = "Agregar Producto",
            icon = Icons.Default.AddPhotoAlternate,
            route = Route.AdminAgregarProducto.path,
            onClick = onAgregarProducto
        ),
        DrawerItem.NavItem(
            label = "Inventario",
            icon = Icons.Default.Inventory,
            route = Route.AdminInventario.path,
            onClick = onInventario
        ),

        DrawerItem.Header("Gestión Comercial"),
        DrawerItem.NavItem(
            label = "Ventas",
            icon = Icons.Default.ShoppingCart,
            route = Route.AdminVentas.path,
            onClick = onVentas
        ),
        DrawerItem.NavItem(
            label = "Clientes",
            icon = Icons.Default.People,
            route = Route.AdminClientes.path,
            onClick = onClientes
        ),

        DrawerItem.Header("Administración"),
        DrawerItem.NavItem(
            label = "Usuarios",
            icon = Icons.Default.ManageAccounts,
            route = Route.AdminUsuarios.path,
            onClick = onUsuarios
        ),
        DrawerItem.NavItem(
            label = "Reportes",
            icon = Icons.Default.Assessment,
            route = Route.AdminReportes.path,
            onClick = onReportes
        ),

        DrawerItem.Divider,

        DrawerItem.NavItem(
            label = "Mi Perfil",
            icon = Icons.Default.Person,
            route = Route.AdminPerfil.path,
            onClick = onPerfil
        ),

        DrawerItem.Divider,
        DrawerItem.Logout(onClick = onLogout)
    )
}

