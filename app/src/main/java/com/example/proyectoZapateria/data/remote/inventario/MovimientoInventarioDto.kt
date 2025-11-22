package com.example.proyectoZapateria.data.remote.inventario

import com.google.gson.annotations.SerializedName


data class MovimientoInventarioDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("id_inventario")
    val inventarioId: Long, // ID del inventario relacionado

    val tipo: String, // "entrada" o "salida"

    val cantidad: Int,

    val motivo: String, // motivo del movimiento (venta, ajuste, devolución, etc.)

    @SerializedName("id_usuario")
    val usuarioId: Long?,

    val fecha: Long? // Fecha/timestamp en epoch millis
)

