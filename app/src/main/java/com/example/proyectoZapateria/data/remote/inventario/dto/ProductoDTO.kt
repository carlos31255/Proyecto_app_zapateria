package com.example.proyectoZapateria.data.remote.inventario.dto

import com.google.gson.annotations.SerializedName

data class ProductoDTO(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("marcaId")
    val marcaId: Long,
    @SerializedName("descripcion")
    val descripcion: String? = null,
    @SerializedName("precioUnitario")
    val precioUnitario: Int = 0,
    @SerializedName("imagenUrl")
    val imagenUrl: String? = null
)
