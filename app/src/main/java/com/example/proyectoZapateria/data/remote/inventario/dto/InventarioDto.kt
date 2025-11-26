package com.example.proyectoZapateria.data.remote.inventario.dto

data class InventarioDTO(
    val id: Long? = null,
    val productoId: Long,
    val nombre: String,
    val tallaId: Long?,
    val talla: String,
    val cantidad: Int,
    val stockMinimo: Int
)