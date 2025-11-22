package com.example.proyectoZapateria.ui.model

/**
 * Modelo UI para representar inventario en las pantallas (mezcla información remota y mapeo local de talla)
 */
data class InventarioUi(
    val idRemote: Long? = null,
    val idModelo: Long,
    val talla: String,
    val tallaIdLocal: Long? = null,
    val stock: Int = 0
)
