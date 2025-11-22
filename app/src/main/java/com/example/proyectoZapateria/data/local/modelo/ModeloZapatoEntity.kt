package com.example.proyectoZapateria.data.local.modelo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modelo_zapato")
data class ModeloZapatoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_modelo")
    val idModelo: Long,
    @ColumnInfo(name = "nombre_modelo")
    val nombreModelo: String,
    @ColumnInfo(name = "id_marca")
    val idMarca: Long,
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,
    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Int = 0,
    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String? = null,
    @ColumnInfo(name = "estado")
    val estado: String = "activo"
)
