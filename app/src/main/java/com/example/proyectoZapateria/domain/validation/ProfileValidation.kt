package com.example.proyectoZapateria.domain.validation

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

// Validación de teléfono (permite '+' al inicio, espacios y guiones para legibilidad). Teléfono opcional.
fun validateProfilePhone(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return null // teléfono opcional

    val startsWithPlus = trimmed.startsWith('+')
    val digitsOnly = trimmed.filter { it.isDigit() }

    val allowedExtra = trimmed.filter { !it.isDigit() }
    val invalidChars = allowedExtra.filter { it != '+' && it != ' ' && it != '-' }
    if (invalidChars.isNotEmpty()) return "El teléfono sólo debe contener dígitos, espacios, guiones o un '+' al inicio"

    if (trimmed.contains('+') && !startsWithPlus) return "El '+' sólo puede estar al inicio"

    if (digitsOnly.length < 7) return "Número de teléfono demasiado corto"
    if (digitsOnly.length > 15) return "Número de teléfono demasiado largo"

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
