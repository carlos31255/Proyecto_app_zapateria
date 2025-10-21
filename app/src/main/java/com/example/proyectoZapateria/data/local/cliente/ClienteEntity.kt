package com.example.proyectoZapateria.data.local.cliente

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.persona.PersonaEntity

@Entity(
    tableName = "cliente",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_persona"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_persona"], unique = true)]
)
data class ClienteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_persona")
    val idPersona: Int,

    val categoria: String?
)

