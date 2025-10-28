package com.example.proyectoZapateria.data.local.boletaventa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.cliente.ClienteEntity
import com.example.proyectoZapateria.data.local.usuario.UsuarioEntity

@Entity(
    tableName = "boletaventa",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_vendedor"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_cliente"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_vendedor"]),
        Index(value = ["id_cliente"]),
        Index(value = ["numero_boleta"], unique = true)
    ]
)
data class BoletaVentaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_boleta")
    val idBoleta: Int = 0,

    @ColumnInfo(name = "numero_boleta")
    val numeroBoleta: String,

    val fecha: Long = System.currentTimeMillis(), // Timestamp en milisegundos

    @ColumnInfo(name = "id_vendedor")
    val idVendedor: Int? = null,

    @ColumnInfo(name = "id_cliente")
    val idCliente: Int,

    @ColumnInfo(name = "monto_total")
    val montoTotal: Int,

    @ColumnInfo(name = "estado")
    val estado: String = "COMPLETADA" // COMPLETADA, CANCELADA
)
