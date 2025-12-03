package com.example.proyectoZapateria.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectoZapateria.ui.screen.RegisterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registerScreen_registroExitosoConDatosValidos() {
        // Todos los campos con datos válidos que cumplen las validaciones
        composeTestRule.setContent {
            RegisterScreen(
                name = "Juan Pérez González",
                email = "juan.perez@example.com",
                phonePrefix = "+56",
                phone = "912345678",
                rut = "12345678-9",
                pass = "Password123!",
                confirm = "Password123!",
                calle = "Avenida Principal",
                numeroPuerta = "456",
                nameError = null,
                emailError = null,
                phoneError = null,
                rutError = null,
                passError = null,
                confirmError = null,
                calleError = null,
                numeroPuertaError = null,
                canSubmit = true,
                isSubmitting = false,
                errorMsg = null,
                regiones = emptyList(),
                ciudades = emptyList(),
                comunas = emptyList(),
                loadingRegiones = false,
                loadingCiudades = false,
                loadingComunas = false,
                selectedRegionId = 1L,
                selectedCiudadId = 1L,
                selectedComunaId = 1L,
                onRegionSelect = {},
                onCiudadSelect = {},
                onComunaSelect = {},
                onNameChange = {},
                onEmailChange = {},
                onPhonePrefixChange = {},
                onPhoneChange = {},
                onRutChange = {},
                onPassChange = {},
                onConfirmChange = {},
                onCalleChange = {},
                onNumeroPuertaChange = {},
                onSubmit = {},
                onGoLogin = {}
            )
        }

        // Verificar que los campos principales existen con sus valores
        composeTestRule.onNodeWithText("Nombre completo").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Teléfono").assertExists()
        composeTestRule.onNodeWithText("Contraseña").assertExists()
        composeTestRule.onNodeWithText("Confirmar contraseña").assertExists()

        // Verificar que no hay errores visibles
        composeTestRule.onNodeWithText("Debe incluir una mayúscula").assertDoesNotExist()
        composeTestRule.onNodeWithText("El email es obligatorio").assertDoesNotExist()
        composeTestRule.onNodeWithText("Formato de email inválido").assertDoesNotExist()
        composeTestRule.onNodeWithText("Las contraseñas no coinciden").assertDoesNotExist()

        // El botón debe estar habilitado
        composeTestRule.onNodeWithText("Registrar").assertIsEnabled()
    }
}

