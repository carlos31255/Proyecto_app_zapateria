package com.example.proyectoZapateria.data.local.persona

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "persona",
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)

data class PersonaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_persona")
    val idPersona: Int = 0,

    val nombre: String,
    val apellido: String,
    val rut: String, // Número del RUT sin dígito verificador
    val dv: String, // Dígito verificador del RUT
    val telefono: String?,
    val email: String?,
    @ColumnInfo(name = "id_comuna")
    val idComuna: Int?,
    val calle: String?,
    @ColumnInfo(name = "numero_puerta")
    val numeroPuerta: String?,
    val username: String,
    @ColumnInfo(name = "pass_hash")
    val passHash: String,
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Instant = Instant.now(), // Fecha y hora de registro
    val estado: String = "activo"
)