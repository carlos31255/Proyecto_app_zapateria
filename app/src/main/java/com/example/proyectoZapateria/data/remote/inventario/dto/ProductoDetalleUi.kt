package com.example.proyectoZapateria.data.remote.inventario.dto

// Modelo preparado para la UI: contiene el producto (ProductoDTO) y campos de detalle de boleta
data class ProductoDetalleUi(
    val producto: ProductoDTO? = null,
    val cantidad: Int = 0,
    val talla: String = "-",
    val marcaName: String = "Desconocida"
)