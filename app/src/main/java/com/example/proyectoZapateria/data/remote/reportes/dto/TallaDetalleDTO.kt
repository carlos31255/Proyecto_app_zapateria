package com.example.proyectoZapateria.data.remote.reportes.dto

data class TallaDetalleDTO(
    val talla: String?,
    val cantidad: Int,
    val stockMinimo: Int,
    val alerta: Boolean
)

