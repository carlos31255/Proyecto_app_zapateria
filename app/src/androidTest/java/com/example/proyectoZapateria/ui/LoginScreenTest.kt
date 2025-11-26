package com.example.proyectoZapateria.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectoZapateria.ui.screen.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_loginExitosoConCredencialesValidas() {
        // Campos con datos válidos
        composeTestRule.setContent {
            LoginScreen(
                email = "usuario@test.com",
                pass = "Password123!",
                emailError = null,
                passError = null,
                canSubmit = true,
                isSubmitting = false,
                errorMsg = null,
                onEmailChange = {},
                onPassChange = {},
                onSubmit = {},
                onGoRegister = {},
                onTestApi = {}
            )
        }

        // Verificar que los campos principales existen
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Contraseña").assertExists()
        composeTestRule.onNodeWithText("Bienvenido").assertExists()
        composeTestRule.onNodeWithText("Inicia sesión en tu cuenta").assertExists()
        composeTestRule.onNodeWithText("Crear cuenta").assertExists()

        // Verificar que no hay mensajes de error
        composeTestRule.onNodeWithText("El email es obligatorio").assertDoesNotExist()
        composeTestRule.onNodeWithText("La contraseña es obligatoria").assertDoesNotExist()
        composeTestRule.onNodeWithText("Formato de email inválido").assertDoesNotExist()

        // El botón debe estar habilitado
        composeTestRule.onNodeWithText("Entrar").assertIsEnabled()
    }
}

