package com.example.proyectoZapateria.domain.validation

/**
 * Validadores específicos para edición de perfil de usuario (cliente/transportista).
 * Cada función devuelve null si el valor es válido, o una cadena con el mensaje de error.
 */

// Simple validación de email. Devuelve mensaje de error o null.
fun validateProfileEmail(email: String): String? {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return "El email no puede estar vacío"
    // Regex básico para emails
    val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
    return if (!emailRegex.matches(trimmed)) "Formato de email inválido" else null
}

// Validación de nombre (solo letras y espacios, no vacío)
fun validateProfileName(name: String): String? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "Debe ingresar un nombre"
    // Usamos raw string para evitar escapes
    val lettersOnly = """^[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+$""".toRegex()
    return if (!lettersOnly.matches(trimmed)) "El nombre sólo debe contener letras y espacios" else null
}

// Validación de teléfono (solo dígitos, longitud razonable 7-15)
fun validateProfilePhone(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return null // teléfono opcional
    // Permitir un '+' inicial (para el código de país) seguido sólo de dígitos
    val normalized = if (trimmed.startsWith('+')) trimmed.substring(1) else trimmed
    if (!normalized.all { it.isDigit() }) return "El teléfono sólo debe contener dígitos o empezar con '+'"
    if (normalized.length < 7) return "Número de teléfono demasiado corto"
    if (normalized.length > 15) return "Número de teléfono demasiado largo"
    return null
}

// Validación de calle para perfil (basada en direcciones reales chilenas)
fun validateProfileStreet(calle: String): String? {
    val trimmed = calle.trim()
    if (trimmed.isEmpty()) return "La calle es obligatoria"
    if (trimmed.length < 3) return "Nombre de calle demasiado corto"
    if (trimmed.length > 80) return "Máximo 80 caracteres"
    if (!trimmed.any { it.isLetter() }) return "Debe contener al menos letras"
    return null
}

// Validación de número de puerta para perfil (permite formatos como "123", "123-A", "Depto 5B")
fun validateProfileHouseNumber(numero: String): String? {
    val trimmed = numero.trim()
    if (trimmed.isEmpty()) return "El número de puerta es obligatorio"
    if (trimmed.length > 15) return "Máximo 15 caracteres"
    if (!trimmed.any { it.isLetterOrDigit() }) return "Formato inválido"
    return null
}

