package com.example.proyectoZapateria.data.local.detalleboleta

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity

@Entity(
    tableName = "detalleboleta",
    foreignKeys = [
        ForeignKey(
            entity = BoletaVentaEntity::class,
            parentColumns = ["id_boleta"],
            childColumns = ["id_boleta"]
        ),
        ForeignKey(
            entity = InventarioEntity::class,
            parentColumns = ["id_inventario"],
            childColumns = ["id_inventario"]
        )
    ],
    indices = [
        Index(value = ["id_boleta"]),
        Index(value = ["id_inventario"])
    ]
)
data class DetalleBoletaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_detalle")
    val idDetalle: Int = 0,

    @ColumnInfo(name = "id_boleta")
    val idBoleta: Int,

    @ColumnInfo(name = "id_inventario")
    val idInventario: Int,

    val cantidad: Int,

    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Double,

    val subtotal: Double
)

