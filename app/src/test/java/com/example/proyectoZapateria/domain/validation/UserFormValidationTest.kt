package com.example.proyectoZapateria.domain.validation

import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserFormValidationTest {

    // Tests para validateEmail
    @Test
    fun validateEmail_email_valido() {
        val error = validateEmail("prueba@prueba.cl")
        assertNull(error)
    }

    @Test
    fun validateEmail_formato_valido_complejo() {
        val error = validateEmail("usuario.nombre+tag@empresa.co.cl")
        assertNull(error)
    }

    // Tests para validateNameLettersOnly
    @Test
    fun validateNameLettersOnly_nombre_valido() {
        val error = validateNameLettersOnly("Juan Pérez")
        assertNull(error)
    }

    @Test
    fun validateNameLettersOnly_nombre_con_tildes() {
        val error = validateNameLettersOnly("María José")
        assertNull(error)
    }

    @Test
    fun validateNameLettersOnly_nombre_con_enie() {
        val error = validateNameLettersOnly("Muñoz Peña")
        assertNull(error)
    }

    // Tests para validatePhoneDigitsOnly
    @Test
    fun validatePhoneDigitsOnly_telefono_valido() {
        val error = validatePhoneDigitsOnly("912345678")
        assertNull(error)
    }

    @Test
    fun validatePhoneDigitsOnly_telefono_con_codigo_pais() {
        val error = validatePhoneDigitsOnly("+56912345678")
        assertNull(error)
    }

    // Tests para validateStrongPassword
    @Test
    fun validateStrongPassword_contrasena_valida() {
        val error = validateStrongPassword("Password123!")
        assertNull(error)
    }

    @Test
    fun validateStrongPassword_compleja_valida() {
        val error = validateStrongPassword("MyP@ssw0rd2024!")
        assertNull(error)
    }

    // Tests para validateConfirm
    @Test
    fun validateConfirm_contrasenas_coinciden() {
        val error = validateConfirm("Password123!", "Password123!")
        assertNull(error)
    }

    // Tests para validateStreet
    @Test
    fun validateStreet_calle_valida() {
        val error = validateStreet("Avenida Libertador")
        assertNull(error)
    }

    @Test
    fun validateStreet_calle_con_numeros() {
        val error = validateStreet("Calle 21")
        assertNull(error)
    }

    @Test
    fun validateStreet_calle_con_caracteres_especiales() {
        val error = validateStreet("O'Higgins")
        assertNull(error)
    }

    // Tests para validateHouseNumber
    @Test
    fun validateHouseNumber_numero_valido() {
        val error = validateHouseNumber("123")
        assertNull(error)
    }

    @Test
    fun validateHouseNumber_formato_con_letra() {
        val error = validateHouseNumber("123-A")
        assertNull(error)
    }

    @Test
    fun validateHouseNumber_formato_departamento() {
        val error = validateHouseNumber("Depto 5B")
        assertNull(error)
    }

    @Test
    fun validateHouseNumber_numero_simple() {
        val error = validateHouseNumber("42")
        assertNull(error)
    }
}

