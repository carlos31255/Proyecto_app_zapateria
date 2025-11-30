package com.example.proyectoZapateria.viewmodel.cliente

// ui state para cli
data class ClientePerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val categoria: String = "",
    val calle: String = "",
    val numeroPuerta: String = "",
    val totalPedidos: Int = 0,
    val profileImageUri: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

