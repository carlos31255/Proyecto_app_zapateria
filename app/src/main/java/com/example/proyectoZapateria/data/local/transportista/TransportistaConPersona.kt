package com.example.proyectoZapateria.data.local.transportista

data class TransportistaConPersona(
    val idPersona: Int,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val dv: String,
    val telefono: String?,
    val email: String?,
    val username: String,
    val estado: String,
    val licencia: String?,
    val vehiculo: String?
) {
    // Función auxiliar para obtener RUT completo formateado
    fun getRutCompleto(): String = "$rut-$dv"

    // Función auxiliar para obtener nombre completo
    fun getNombreCompleto(): String = "$nombre $apellido"
}

