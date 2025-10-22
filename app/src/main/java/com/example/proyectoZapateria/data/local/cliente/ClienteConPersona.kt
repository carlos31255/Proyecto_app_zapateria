package com.example.proyectoZapateria.data.local.cliente

data class ClienteConPersona(
    val idPersona: Int,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val dv: String, // Dígito verificador del RUT
    val telefono: String?,
    val email: String?,
    val categoria: String?,
    val username: String,
    val calle: String?,
    val numeroPuerta: String?,
    val idComuna: Int?
) {
    // Función auxiliar para obtener RUT completo formateado
    fun getRutCompleto(): String = "$rut-$dv"
}

