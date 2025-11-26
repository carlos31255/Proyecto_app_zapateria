package com.example.proyectoZapateria.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RolValidationTest {

    // Tests para validateNombre
    @Test
    fun validateNombre_nombre_valido() {
        val error = RolValidation.validateNombre("Vendedor")
        assertNull(error)
    }

    @Test
    fun validateNombre_nombre_con_tildes() {
        val error = RolValidation.validateNombre("Administración")
        assertNull(error)
    }

    @Test
    fun validateNombre_nombre_con_enie() {
        val error = RolValidation.validateNombre("Diseñador")
        assertNull(error)
    }

    @Test
    fun validateNombre_nombre_con_espacios_validos() {
        val error = RolValidation.validateNombre("Jefe de Ventas")
        assertNull(error)
    }

    // Tests para validateDescripcion
    @Test
    fun validateDescripcion_descripcion_valida() {
        val error = RolValidation.validateDescripcion("Encargado de gestionar las ventas")
        assertNull(error)
    }

    @Test
    fun validateDescripcion_descripcion_vacia() {
        val error = RolValidation.validateDescripcion("")
        assertNull(error) // La descripción es opcional
    }

    @Test
    fun validateDescripcion_descripcion_exactamente_200_caracteres() {
        val error = RolValidation.validateDescripcion("a".repeat(200))
        assertNull(error)
    }

    // Tests para validateRol
    @Test
    fun validateRol_todos_campos_validos() {
        val errors = RolValidation.validateRol(
            nombre = "Vendedor",
            descripcion = "Encargado de ventas"
        )
        assertTrue(errors.isEmpty())
    }

    // Tests para isSystemRole
    @Test
    fun isSystemRole_administrador() {
        assertTrue(RolValidation.isSystemRole("Administrador"))
    }

    @Test
    fun isSystemRole_transportista() {
        assertTrue(RolValidation.isSystemRole("Transportista"))
    }

    @Test
    fun isSystemRole_cliente() {
        assertTrue(RolValidation.isSystemRole("Cliente"))
    }

    @Test
    fun isSystemRole_administrador_minusculas() {
        assertTrue(RolValidation.isSystemRole("administrador"))
    }

    @Test
    fun isSystemRole_administrador_mayusculas() {
        assertTrue(RolValidation.isSystemRole("ADMINISTRADOR"))
    }

    // Tests para normalizeNombre
    @Test
    fun normalizeNombre_minusculas() {
        val result = RolValidation.normalizeNombre("vendedor")
        assertEquals("Vendedor", result)
    }

    @Test
    fun normalizeNombre_mayusculas() {
        val result = RolValidation.normalizeNombre("VENDEDOR")
        assertEquals("Vendedor", result)
    }

    @Test
    fun normalizeNombre_varias_palabras() {
        val result = RolValidation.normalizeNombre("jefe de ventas")
        assertEquals("Jefe De Ventas", result)
    }

    @Test
    fun normalizeNombre_con_espacios_extra() {
        val result = RolValidation.normalizeNombre("  jefe de ventas  ")
        assertEquals("Jefe De Ventas", result)
    }

    @Test
    fun normalizeNombre_mixto() {
        val result = RolValidation.normalizeNombre("JeFe De VeNtAs")
        assertEquals("Jefe De Ventas", result)
    }

    // Tests para canModifyRole
    @Test
    fun canModifyRole_rol_personalizado() {
        assertTrue(RolValidation.canModifyRole("Vendedor"))
    }

    // Tests para canDeleteRole
    @Test
    fun canDeleteRole_rol_personalizado() {
        assertTrue(RolValidation.canDeleteRole("Vendedor"))
    }

    // Tests para getSystemRoleDescription
    @Test
    fun getSystemRoleDescription_administrador() {
        val description = RolValidation.getSystemRoleDescription("Administrador")
        assertNotNull(description)
        assertTrue(description!!.contains("acceso total"))
    }

    @Test
    fun getSystemRoleDescription_transportista() {
        val description = RolValidation.getSystemRoleDescription("Transportista")
        assertNotNull(description)
        assertTrue(description!!.contains("entregas"))
    }

    @Test
    fun getSystemRoleDescription_cliente() {
        val description = RolValidation.getSystemRoleDescription("Cliente")
        assertNotNull(description)
        assertTrue(description!!.contains("catálogo"))
    }

    // Tests para SYSTEM_ROLES
    @Test
    fun systemRoles_contiene_tres_roles() {
        assertEquals(3, RolValidation.SYSTEM_ROLES.size)
    }

    @Test
    fun systemRoles_contiene_administrador() {
        assertTrue(RolValidation.SYSTEM_ROLES.contains("Administrador"))
    }

    @Test
    fun systemRoles_contiene_transportista() {
        assertTrue(RolValidation.SYSTEM_ROLES.contains("Transportista"))
    }

    @Test
    fun systemRoles_contiene_cliente() {
        assertTrue(RolValidation.SYSTEM_ROLES.contains("Cliente"))
    }
}

