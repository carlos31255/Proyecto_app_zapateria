package com.example.proyectoZapateria.data.local.boletaventa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// BoletaVentaEntity ahora no tiene foreign keys a Usuario y Cliente porque son tablas remotas
// idVendedor e idCliente se mantienen como referencias lógicas al microservicio de usuarios
@Entity(
    tableName = "boletaventa",
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
