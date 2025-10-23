package com.example.proyectoZapateria.data.local.persona

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val rut: String, // RUT completo con dígito verificador (ej: "12345678-9")
    val telefono: String?,
    val email: String?,
    @ColumnInfo(name = "id_comuna")
    val idComuna: Int?,
    val calle: String?,
    @ColumnInfo(name = "numero_puerta")
    val numeroPuerta: String?,
    val username: String,
    @ColumnInfo(name = "password_hash")
    val passHash: String,
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis(),
    val estado: String = "activo"
)