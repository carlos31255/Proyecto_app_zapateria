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
import androidx.compose.ui.graphics.Color
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
                2 -> "Vendedor"
                3 -> "Transportista"
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
        onGoRegister = onGoRegister
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
    onGoRegister: () -> Unit
) {
    // Colores inspirados en zapatos de cuero y tonos oscuros elegantes
    val darkLeather = Color(0xFF2C2416) // Cuero oscuro
    val brownLeather = Color(0xFF4A3C2A) // Marrón cuero
    val lightBrown = Color(0xFF8B7355) // Marrón claro
    val cream = Color(0xFFD4C5B0) // Crema/beige

    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(darkLeather, brownLeather)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF3D3228).copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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
                    color = cream
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Inicia sesión en tu cuenta",
                    textAlign = TextAlign.Center,
                    color = lightBrown,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))

                // Campo EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email", color = lightBrown) },
                    singleLine = true,
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cream,
                        unfocusedTextColor = cream,
                        focusedBorderColor = lightBrown,
                        unfocusedBorderColor = lightBrown.copy(alpha = 0.5f),
                        cursorColor = lightBrown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(
                        emailError,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Campo CONTRASEÑA
                OutlinedTextField(
                    value = pass,
                    onValueChange = onPassChange,
                    label = { Text("Contraseña", color = lightBrown) },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = lightBrown
                            )
                        }
                    },
                    isError = passError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cream,
                        unfocusedTextColor = cream,
                        focusedBorderColor = lightBrown,
                        unfocusedBorderColor = lightBrown.copy(alpha = 0.5f),
                        cursorColor = lightBrown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (passError != null) {
                    Text(
                        passError,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botón ENTRAR
                Button(
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC4A57B),
                        contentColor = Color(0xFF1A1410),
                        disabledContainerColor = Color(0xFF6B5D4F),
                        disabledContentColor = Color(0xFF3D3228)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF1A1410)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Validando...", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Entrar", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, color = Color(0xFFFF6B6B))
                }

                Spacer(Modifier.height(16.dp))

                // Botón IR A REGISTRO
                OutlinedButton(
                    onClick = onGoRegister,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = cream
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear cuenta", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

