package com.example.proyectoZapateria.data.local.rol

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rol",
    indices = [Index(value = ["nombre_rol"], unique = true)]
)
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_rol")
    val idRol: Int = 0,

    @ColumnInfo(name = "nombre_rol")
    val nombreRol: String,

    val descripcion: String?
)