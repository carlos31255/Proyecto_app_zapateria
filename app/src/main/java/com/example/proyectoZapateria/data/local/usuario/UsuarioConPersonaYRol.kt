package com.example.proyectoZapateria.data.local.usuario

data class UsuarioConPersonaYRol(
    val idPersona: Int,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val dv: String,
    val telefono: String?,
    val email: String?,
    val username: String,
    val estado: String,
    val idRol: Int,
    val nombreRol: String,
    val descripcionRol: String?
) {
    // Función auxiliar para obtener RUT completo formateado
    fun getRutCompleto(): String = "$rut-$dv"

    // Función auxiliar para obtener nombre completo
    fun getNombreCompleto(): String = "$nombre $apellido"
}

