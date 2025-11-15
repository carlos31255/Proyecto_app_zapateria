package com.example.proyectoZapateria.data.model

// Modelo unificado que representa un usuario completo combinando datos de Persona, Usuario y Rol
data class UsuarioCompleto(
    val idPersona: Int,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val telefono: String,
    val email: String,
    val idComuna: Int?,
    val calle: String,
    val numeroPuerta: String,
    val username: String,
    val fechaRegistro: Long,
    val estado: String,
    val idRol: Int,
    val nombreRol: String,
    val activo: Boolean = true
)
