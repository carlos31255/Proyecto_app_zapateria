package com.example.proyectoZapateria.viewmodel.transportista

import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles

data class DetalleEntregaUiState(
    // Los detalles del cliente/dirección
    val entrega: EntregaConDetalles? = null,

    // La lista de productos (zapatos)
    val productos: List<ProductoDetalle> = emptyList(),

    val isLoading: Boolean = true,
    val error: String? = null,

    // Se pone en true cuando la actualización es exitosa,
    // para que la UI pueda navegar hacia atrás.
    val actualizacionExitosa: Boolean = false
)

data class TransportistaEntregasUiState (
    // La lista de entregas que viene del DAO
    val entregas: List<EntregaConDetalles> = emptyList(),

    // El conteo de entregas pendientes
    val pendientesCount: Int = 0,

    // El conteo de entregas completadas
    val completadasCount: Int = 0,

    // true mientras se cargan los datos por primera vez
    val isLoading: Boolean = true,

    // Contiene cualquier mensaje de error si la carga falla
    val error: String? = null
)

data class TransportistaPerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val totalEntregas: Int = 0,
    val entregasCompletadas: Int = 0,
    val entregasPendientes: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)