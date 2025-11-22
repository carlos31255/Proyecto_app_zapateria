package com.example.proyectoZapateria.data.remote.inventario.dto

data class InventarioDTO(
    val id: Long? = null,
    val productoId: Long, // id del modelozapato
    val nombre: String,
    val talla: String,
    val cantidad: Int,
    val stockMinimo: Int,
    val modeloId: Long?,
    val tallaId: Long?
)