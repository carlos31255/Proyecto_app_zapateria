package com.example.proyectoZapateria.ui.model


data class InventarioUi(
    val idRemote: Long? = null,
    val idModelo: Long,
    val talla: String,
    val tallaIdLocal: Long? = null,
    val stock: Int = 0
)
