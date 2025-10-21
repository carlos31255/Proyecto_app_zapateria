package com.example.proyectoZapateria.data.local.usuario

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.proyectoZapateria.data.local.persona.PersonaEntity
import com.example.proyectoZapateria.data.local.rol.RolEntity

@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id_persona"],
            childColumns = ["id_persona"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id_rol"],
            childColumns = ["id_rol"]
        )
    ],
    indices = [
        Index(value = ["id_persona"], unique = true),
        Index(value = ["id_rol"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_persona")
    val idPersona: Int,

    @ColumnInfo(name = "id_rol")
    val idRol: Int
)

