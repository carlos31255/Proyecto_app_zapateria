package com.example.proyectoZapateria.viewmodel.transportista

import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles

data class EntregasUiState (
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