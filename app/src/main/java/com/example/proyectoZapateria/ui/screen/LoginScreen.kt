package com.example.proyectoZapateria.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectoZapateria.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoZapateria.viewmodel.AuthViewModel


@Composable
fun LoginScreenVm(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit, // Recibir el rol del usuario autenticado
    onGoRegister: () -> Unit
) {
    val state by authViewModel.login.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Cuando haya un usuario autenticado, redirigir según su rol
    LaunchedEffect(currentUser) {
        currentUser?.let { usuarioConRol ->
            // Mapear el ID del rol a su nombre
            val nombreRol = when(usuarioConRol.idRol) {
                1 -> "Administrador"
                2 -> "Transportista"
                3 -> "Cliente"
                else -> "Usuario"
            }
            onLoginSuccess(nombreRol)
        }
    }

    LoginScreen(
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isLoading,
        errorMsg = state.errorMsg,
        onEmailChange = authViewModel::onLoginEmailChange,
        onPassChange = authViewModel::onLoginPassChange,
        onSubmit = authViewModel::submitLogin,
        onGoRegister = onGoRegister,
        onTestApi = authViewModel::testConexionMicroservicio
    )
}

@Composable
private fun LoginScreen(
    email: String,
    pass: String,
    emailError: String?,
    passError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit,
    onTestApi: () -> Unit
) {
    // Nuevo esquema de colores morado/violeta claro - Material Design 3
    val colorScheme = MaterialTheme.colorScheme

    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.1f),
                        colorScheme.primaryContainer.copy(alpha = 0.3f),
                        colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo StepStyle
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo StepStyle",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(70.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Inicia sesión en tu cuenta",
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))

                // Campo EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(
                        emailError,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Campo CONTRASEÑA
                OutlinedTextField(
                    value = pass,
                    onValueChange = onPassChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = passError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (passError != null) {
                    Text(
                        passError,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botón ENTRAR
                Button(
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        disabledContainerColor = colorScheme.surfaceVariant,
                        disabledContentColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Validando...", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Entrar", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Botón PROBAR API (TEMPORAL)
                Button(
                    onClick = onTestApi,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary
                    )
                ) {
                    Text("🧪 Probar API")
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, color = colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                // Botón IR A REGISTRO
                OutlinedButton(
                    onClick = onGoRegister,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Crear cuenta", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

