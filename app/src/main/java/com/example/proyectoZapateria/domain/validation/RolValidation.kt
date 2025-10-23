package com.example.proyectoZapateria.domain.validation

// Validaciones para la entidad Rol
object RolValidation {

    // Roles predefinidos del sistema (precargados en la BD, no modificables)
    val SYSTEM_ROLES = listOf(
        "Administrador",
        "Vendedor",
        "Transportista"
    )

    // Valida el nombre del rol (retorna null si es válido, mensaje de error si no lo es)
    fun validateNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre del rol no puede estar vacío"
            nombre.length < 3 -> "El nombre del rol debe tener al menos 3 caracteres"
            nombre.length > 50 -> "El nombre del rol no puede exceder 50 caracteres"
            !nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) ->
                "El nombre del rol solo puede contener letras y espacios"
            else -> null
        }
    }

    // Valida la descripción del rol (campo opcional)
    fun validateDescripcion(descripcion: String): String? {
        return when {
            descripcion.isBlank() -> null
            descripcion.length > 200 -> "La descripción no puede exceder 200 caracteres"
            else -> null
        }
    }

    // Valida todos los campos del rol (retorna map con errores)
    fun validateRol(
        nombre: String,
        descripcion: String = ""
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateNombre(nombre)?.let { errors["nombre"] = it } // nombre del campo que tiene error
        validateDescripcion(descripcion)?.let { errors["descripcion"] = it } // mensaje de error
        return errors
    }

    // Verifica si el nombre corresponde a un rol del sistema
    fun isSystemRole(nombre: String): Boolean {
        return SYSTEM_ROLES.any { it.equals(nombre, ignoreCase = true) }
    }

    // Normaliza el nombre del rol (primera letra mayúscula por palabra)
    fun normalizeNombre(nombre: String): String {
        return nombre.trim()
            .split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    // Verifica si un rol puede ser modificado (los del sistema NO)
    fun canModifyRole(nombre: String): Boolean {
        return !isSystemRole(nombre)
    }

    // Verifica si un rol puede ser eliminado (los del sistema NO)
    fun canDeleteRole(nombre: String): Boolean {
        return !isSystemRole(nombre)
    }

    // Obtiene la descripción predefinida para un rol del sistema
    fun getSystemRoleDescription(nombre: String): String? {
        return when (nombre) {
            "Administrador" -> "Dueño o encargado principal: acceso total al sistema, gestión de usuarios, inventario y configuración"
            "Vendedor" -> "Personal de ventas: gestión de ventas, clientes y boletas de venta"
            "Transportista" -> "Personal de entregas: gestión de despachos y seguimiento de pedidos"
            else -> null
        }
    }
}
