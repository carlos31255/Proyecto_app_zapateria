package com.example.proyectoZapateria.data.local.comuna

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.region.RegionEntity

@Entity(
    tableName = "comuna",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id_region"],
            childColumns = ["id_region"]
        )
    ],
    indices = [
        Index(value = ["id_region"]),
        Index(value = ["id_region", "nombre_comuna"], unique = true)
    ]
)
data class ComunaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_comuna")
    val idComuna: Int = 0,

    @ColumnInfo(name = "id_region")
    val idRegion: Int,

    @ColumnInfo(name = "nombre_comuna")
    val nombreComuna: String
)

