package com.example.proyectoZapateria.data.local.boletaventa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity
import java.time.Instant

@Entity(
    tableName = "boletaventa",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_vendedor"]
        ),
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_cliente"]
        )
    ],
    indices = [
        Index(value = ["id_vendedor"]),
        Index(value = ["id_cliente"])
    ]
)
data class BoletaVentaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_boleta")

    val fecha: Instant = Instant.now(),


    @ColumnInfo(name = "id_vendedor")
    val idVendedor: Int,

    @ColumnInfo(name = "id_cliente")
    val idCliente: Int,

    @ColumnInfo(name = "monto_total")
    val montoTotal: Double
)

