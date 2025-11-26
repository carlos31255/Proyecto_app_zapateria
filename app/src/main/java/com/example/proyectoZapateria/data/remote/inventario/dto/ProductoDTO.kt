package com.example.proyectoZapateria.data.remote.inventario.dto

data class ProductoDTO(
    val id: Long? = null,
    val nombre: String,
    val marcaId: Long,
    val descripcion: String? = null,
    val precioUnitario: Int = 0,
    val imagenUrl: String? = null
)
