package com.example.proyectoZapateria.data.local.marca

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marca",
    indices = [Index(value = ["nombre_marca"], unique = true)]
)
data class MarcaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_marca")
    val idMarca: Int = 0,

    @ColumnInfo(name = "nombre_marca")
    val nombreMarca: String,

    val descripcion: String?,

    val estado: String = "activa"
)

