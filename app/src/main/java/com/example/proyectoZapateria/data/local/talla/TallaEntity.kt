package com.example.proyectoZapateria.data.local.talla

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "talla",
    indices = [Index(value = ["numero_talla"], unique = true)]
)
data class TallaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_talla")
    val idTalla: Int = 0,

    @ColumnInfo(name = "numero_talla")
    val numeroTalla: String
)

