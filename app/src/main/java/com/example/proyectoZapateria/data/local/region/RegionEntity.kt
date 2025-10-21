package com.example.proyectoZapateria.data.local.region

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "region",
    indices = [Index(value = ["nombre_region"], unique = true)]
)
data class RegionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_region")
    val idRegion: Int = 0,

    @ColumnInfo(name = "nombre_region")
    val nombreRegion: String,

    val abreviatura: String?
)