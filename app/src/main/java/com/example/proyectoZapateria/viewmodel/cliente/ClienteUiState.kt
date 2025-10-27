package com.example.proyectoZapateria.viewmodel.cliente

/**
 * UI state para las pantallas/feature del cliente.
 * Aquí centralizamos las data classes relacionadas con estados de UI
 * (igual que el archivo TransportistaUiState.kt)
 */

data class ClientePerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val categoria: String = "",
    val calle: String = "",
    val numeroPuerta: String = "",
    val totalPedidos: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

