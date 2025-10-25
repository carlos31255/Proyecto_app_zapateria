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