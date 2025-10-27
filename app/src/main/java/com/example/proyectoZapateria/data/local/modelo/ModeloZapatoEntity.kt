package com.example.proyectoZapateria.data.local.modelo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.marca.MarcaEntity

@Entity(
    tableName = "modelozapato",
    foreignKeys = [
        ForeignKey(
            entity = MarcaEntity::class,
            parentColumns = ["id_marca"],
            childColumns = ["id_marca"]
        )
    ],
    indices = [
        Index(value = ["id_marca"]),
        Index(value = ["id_marca", "nombre_modelo"], unique = true)
    ]
)
data class ModeloZapatoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_modelo")
    val idModelo: Int = 0,

    @ColumnInfo(name = "id_marca")
    val idMarca: Int,

    @ColumnInfo(name = "nombre_modelo")
    val nombreModelo: String,

    val descripcion: String?,

    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Int,  // Cambiado a Int (precio en CLP, sin decimales)

    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String? = null,  // Ruta de la imagen del producto

    val estado: String = "activo"
)
