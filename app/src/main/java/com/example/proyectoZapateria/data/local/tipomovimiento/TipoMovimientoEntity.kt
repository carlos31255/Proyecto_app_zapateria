package com.example.proyectoZapateria.data.local.tipomovimiento

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tipo_movimiento",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class TipoMovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tipo_movimiento")
    val idTipoMovimiento: Int = 0,

    val codigo: String,
    val descripcion: String,
    val signo: Int // -1 o 1
)

