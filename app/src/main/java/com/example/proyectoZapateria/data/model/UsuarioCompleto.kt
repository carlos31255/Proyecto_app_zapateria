package com.example.proyectoZapateria.data.model

// Modelo unificado que representa un usuario completo combinando datos de Persona, Usuario y Rol
data class UsuarioCompleto(
    val idPersona: Long,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val telefono: String,
    val email: String,
    val idComuna: Long?,
    val calle: String,
    val numeroPuerta: String,
    val username: String,
    val fechaRegistro: Long,
    val estado: String,
    val idRol: Long,
    val nombreRol: String,
    val activo: Boolean = true
)
