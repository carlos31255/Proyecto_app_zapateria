package com.example.proyectoZapateria.data.local.entrega

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity

@Entity(
    tableName = "entrega",
    foreignKeys = [
        ForeignKey(
            entity = BoletaVentaEntity::class,
            parentColumns = ["id_boleta"],
            childColumns = ["id_boleta"]
        ),
        ForeignKey(
            entity = TransportistaEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_transportista"]
        )
    ],
    indices = [
        Index(value = ["id_boleta"]),
        Index(value = ["id_transportista"])
    ]
)
data class EntregaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_entrega")
    val idEntrega: Int = 0,

    @ColumnInfo(name = "id_boleta")
    val idBoleta: Int,

    @ColumnInfo(name = "id_transportista")
    val idTransportista: Int?,

    @ColumnInfo(name = "estado_entrega")
    val estadoEntrega: String = "pendiente",

    @ColumnInfo(name = "fecha_asignacion")
    val fechaAsignacion: Long = System.currentTimeMillis(), // Timestamp en milisegundos

    @ColumnInfo(name = "fecha_entrega")
    val fechaEntrega: Long?, // Timestamp en milisegundos, null si no se ha completado

    val observacion: String?
)

