package com.example.proyectoZapateria.data.remote.entregas.dto

import com.google.gson.annotations.SerializedName

data class CrearEntregaRequest(
    @SerializedName("idBoleta")
    val idBoleta: Long,

    @SerializedName("idTransportista")
    val idTransportista: Long? = null,

    @SerializedName("estadoEntrega")
    val estadoEntrega: String = "pendiente",

    @SerializedName("observacion")
    val observacion: String? = null
)

