package com.example.proyectoZapateria.data.remote.inventario.dto

data class ModeloZapatoDTO(
    val id: Long,
    val nombre: String,
    val marcaId: Long,
    val descripcion: String?,
    val precioUnitario: Int = 0,
    val imagenUrl: String? = null
)