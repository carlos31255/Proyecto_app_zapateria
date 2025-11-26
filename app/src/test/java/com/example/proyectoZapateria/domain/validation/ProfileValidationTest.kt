package com.example.proyectoZapateria.domain.validation

import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileValidationTest {

    // Tests para validateProfileEmail
    @Test
    fun validateProfileEmail_email_valido() {
        val error = validateProfileEmail("prueba@prueba.cl")
        assertNull(error)
    }

    @Test
    fun validateProfileEmail_formato_valido_con_espacios_laterales() {
        val error = validateProfileEmail("  test@test.com  ")
        assertNull(error)
    }

    // Tests para validateProfileName
    @Test
    fun validateProfileName_nombre_valido() {
        val error = validateProfileName("Juan Pérez")
        assertNull(error)
    }

    @Test
    fun validateProfileName_nombre_con_tildes() {
        val error = validateProfileName("María José")
        assertNull(error)
    }

    @Test
    fun validateProfileName_nombre_con_enie() {
        val error = validateProfileName("Nuñez")
        assertNull(error)
    }

    // Tests para validateProfilePhone
    @Test
    fun validateProfilePhone_telefono_valido() {
        val error = validateProfilePhone("912345678")
        assertNull(error)
    }

    @Test
    fun validateProfilePhone_telefono_vacio_opcional() {
        val error = validateProfilePhone("")
        assertNull(error) // teléfono es opcional
    }

    @Test
    fun validateProfilePhone_telefono_con_codigo_pais() {
        val error = validateProfilePhone("+56912345678")
        assertNull(error)
    }

    @Test
    fun validateProfilePhone_telefono_con_codigo_y_longitud_correcta() {
        val error = validateProfilePhone("+1234567")
        assertNull(error)
    }

    // Tests para validateProfileStreet
    @Test
    fun validateProfileStreet_calle_valida() {
        val error = validateProfileStreet("Avenida Libertador Bernardo O'Higgins")
        assertNull(error)
    }

    @Test
    fun validateProfileStreet_calle_con_numeros_y_letras() {
        val error = validateProfileStreet("Calle 123")
        assertNull(error)
    }

    // Tests para validateProfileHouseNumber
    @Test
    fun validateProfileHouseNumber_numero_valido() {
        val error = validateProfileHouseNumber("123")
        assertNull(error)
    }

    @Test
    fun validateProfileHouseNumber_formato_con_letra() {
        val error = validateProfileHouseNumber("123-A")
        assertNull(error)
    }

    @Test
    fun validateProfileHouseNumber_formato_departamento() {
        val error = validateProfileHouseNumber("Depto 5B")
        assertNull(error)
    }
}

