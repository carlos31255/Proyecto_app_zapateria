package com.example.proyectoZapateria.data.local.entrega

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaEntity
import com.example.proyectoZapateria.data.local.transportista.TransportistaEntity
import java.time.Instant

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
            parentColumns = ["id_persona"], // Al ser TransportistaEntity usa id_persona como PK
            childColumns = ["id_transportista"] // Referencia a id_transportista en EntregaEntity
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
    val idTransportista: Int?, // Puede ser nulo si no se ha asignado un transportista

    @ColumnInfo(name = "estado_entrega")
    val estadoEntrega: String = "pendiente",

    @ColumnInfo(name = "fecha_asignacion")
    val fechaAsignacion: Instant = Instant.now(),

    @ColumnInfo(name = "fecha_entrega")
    val fechaEntrega: Instant?, // Puede ser nulo si la entrega no se ha completado

    val observacion: String?
)

