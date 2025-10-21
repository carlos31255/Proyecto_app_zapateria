package com.example.proyectoZapateria.data.local.inventario

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
import com.example.proyectoZapateria.data.local.talla.TallaEntity

@Entity(
    tableName = "inventario",
    foreignKeys = [
        ForeignKey(
            entity = ModeloZapatoEntity::class,
            parentColumns = ["id_modelo"],
            childColumns = ["id_modelo"]
        ),
        ForeignKey(
            entity = TallaEntity::class,
            parentColumns = ["id_talla"],
            childColumns = ["id_talla"]
        )
    ],
    indices = [
        Index(value = ["id_modelo"]),
        Index(value = ["id_talla"]),
        Index(value = ["id_modelo", "id_talla"], unique = true)
    ]
)
data class InventarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_inventario")
    val idInventario: Int = 0,

    @ColumnInfo(name = "id_modelo")
    val idModelo: Int,

    @ColumnInfo(name = "id_talla")
    val idTalla: Int,

    @ColumnInfo(name = "stock_actual")
    val stockActual: Int = 0
)

