package com.example.proyectoZapateria.data.local.transportista

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TransportistaEntity ahora no tiene foreign key a Persona porque Persona es una tabla remota
// idPersona se mantiene como referencia lógica al microservicio de usuarios
@Entity(tableName = "transportista")
data class TransportistaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_persona")
    val idPersona: Int,

    val licencia: String?,
    val vehiculo: String?
)

