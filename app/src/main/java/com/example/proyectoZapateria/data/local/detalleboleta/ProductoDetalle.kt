package com.example.proyectoZapateria.data.local.detalleboleta

data class ProductoDetalle (
    // De ModeloZapato
    val nombreZapato: String,

    // De Talla
    val talla: String,

    // De DetalleBoleta
    val cantidad: Int,

    // De Marca
    val marca: String
)