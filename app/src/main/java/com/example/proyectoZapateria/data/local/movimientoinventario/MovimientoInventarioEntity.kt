package com.example.proyectoZapateria.data.local.movimientoinventario

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.inventario.InventarioEntity
import com.example.proyectoZapateria.data.local.tipomovimiento.TipoMovimientoEntity
import java.time.Instant

@Entity(
    tableName = "movimientoinventario",
    foreignKeys = [
        ForeignKey(
            entity = InventarioEntity::class,
            parentColumns = ["id_inventario"],
            childColumns = ["id_inventario"]
        ),
        ForeignKey(
            entity = TipoMovimientoEntity::class,
            parentColumns = ["id_tipo_movimiento"],
            childColumns = ["id_tipo_movimiento"]
        )
    ],
    indices = [
        Index(value = ["id_inventario"]),
        Index(value = ["id_tipo_movimiento"])
    ]
)
data class MovimientoInventarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_movimiento")
    val idMovimiento: Int = 0,

    @ColumnInfo(name = "id_inventario")
    val idInventario: Int,

    @ColumnInfo(name = "id_tipo_movimiento")
    val idTipoMovimiento: Int,

    val cantidad: Int,

    val fechaMovimiento: Instant = Instant.now(),
    val fechaMovimiento: Long = System.currentTimeMillis(),

    val observacion: String?
)

