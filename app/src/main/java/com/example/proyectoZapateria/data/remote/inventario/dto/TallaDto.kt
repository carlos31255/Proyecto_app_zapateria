package com.example.proyectoZapateria.data.remote.inventario.dto

data class TallaDTO(
    val id: Long,
    val valor: String, // Ej: "42", "M"
    val descripcion: String?
)