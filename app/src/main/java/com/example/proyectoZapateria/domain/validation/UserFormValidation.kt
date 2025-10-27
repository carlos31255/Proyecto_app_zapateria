package com.example.proyectoZapateria.domain.validation


import android.util.Patterns  //patron estandar para emails

// validar que el email no este vacio y cumpla el patrón de email
fun validateEmail(email: String): String? {
    if (email.isBlank()) return "El email es obligatorio" // no vacio
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches() //coincide con patrón de email
    return if (!ok) "Formato de email inválido" else null //si no cumple, devolvemos mensaje
}

// validar que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio" // no vacio
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$") // solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null //mensaje si
}

// validar que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {
    if (phone.isBlank()) return "El teléfono es obligatorio" // no vacio
    if (!phone.all { it.isDigit() }) return "Solo números" // todos dígitos
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" //tamaño razonable
    return null //o
}

// validar seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria" // no vacio
    if (pass.length < 8) return "Mínimo 8 caracteres" //largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número" //  al menos 1 numero
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios" // sin espacios
    return null //o
}

// validar que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {
    if (confirm.isBlank()) return "Confirma tu contraseña" // no vacio
    return if (pass != confirm) "Las contraseñas no coinciden" else null //deben ser iguales
}

// validar que la calle no esté vacía
fun validateStreet(calle: String): String? {
    if (calle.isBlank()) return "La calle es obligatoria"
    if (calle.length < 3) return "Nombre de calle demasiado corto"
    return null
}

// validar que el número de puerta no esté vacío y sea razonablemente corto
fun validateHouseNumber(numero: String): String? {
    if (numero.isBlank()) return "El número de puerta es obligatorio"
    if (numero.length > 10) return "Número de puerta inválido"
    return null
}
