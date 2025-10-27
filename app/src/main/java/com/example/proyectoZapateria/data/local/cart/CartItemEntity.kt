package com.example.proyectoZapateria.data.local.cart

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cart_item")
    val idCartItem: Int = 0,

    @ColumnInfo(name = "id_cliente")
    val idCliente: Int,

    @ColumnInfo(name = "id_modelo")
    val idModelo: Int,

    val talla: String,

    val cantidad: Int,

    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Int,

    @ColumnInfo(name = "fecha_agregado")
    val fechaAgregado: Long = System.currentTimeMillis()
)

