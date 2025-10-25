package com.example.proyectoZapateria.navigation

/**
 * Rutas de navegación de la aplicación
 */
sealed class Route(val path: String) {
    // Rutas públicas
    data object Home : Route("home")
    data object Login : Route("login")
    data object Register : Route("register")

    // Rutas del Vendedor
    data object VendedorHome : Route("vendedor/home")
    data object VendedorVentas : Route("vendedor/ventas")
    data object VendedorClientes : Route("vendedor/clientes")
    data object VendedorInventario : Route("vendedor/inventario")
    data object VendedorPerfil : Route("vendedor/perfil")

    // Rutas del Administrador
    data object AdminHome : Route("admin/home")
    data object AdminVentas : Route("admin/ventas")
    data object AdminClientes : Route("admin/clientes")
    data object AdminInventario : Route("admin/inventario")
    data object AdminUsuarios : Route("admin/usuarios")
    data object AdminReportes : Route("admin/reportes")
    data object AdminPerfil : Route("admin/perfil")
    data object AdminAgregarProducto : Route("admin/agregar-producto")

    // Rutas del Transportista
    data object TransportistaHome : Route("transportista/home")
    data object TransportistaEntregas : Route("transportista/entregas")
    data object TransportistaEntregaDetalle : Route("transportista/entregas/detalle")
    data object TransportistaPerfil : Route("transportista/perfil")

    // Rutas del Cliente
    data object ClienteHome : Route("cliente/home")
    data object ClienteCatalogo : Route("cliente/catalogo")
    data object ClientePedidos : Route("cliente/pedidos")
    data object ClienteFavoritos : Route("cliente/favoritos")
    data object ClientePerfil : Route("cliente/perfil")
}

