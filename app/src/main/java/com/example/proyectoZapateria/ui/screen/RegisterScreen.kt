package com.example.proyectoZapateria.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoZapateria.viewmodel.AuthViewModel

@Composable
fun RegisterScreenVm(
    authViewModel: AuthViewModel,
    onRegisterOkGoLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by authViewModel.register.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) {
            authViewModel.clearRegisterResult()
            onRegisterOkGoLogin()
        }
    }

    RegisterScreen(
        name = state.name,
        email = state.email,
        phone = state.phone,
        pass = state.pass,
        confirm = state.confirm,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        passError = state.passError,
        confirmError = state.confirmPassError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isLoading,
        errorMsg = state.errorMsg,
        onNameChange = authViewModel::onRegisterNameChange,
        onEmailChange = authViewModel::onRegisterEmailChange,
        onPhoneChange = authViewModel::onRegisterPhoneChange,
        onPassChange = authViewModel::onRegisterPassChange,
        onConfirmChange = authViewModel::onConfirmChange,
        onSubmit = authViewModel::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun RegisterScreen(
    name: String,
    email: String,
    phone: String,
    pass: String,
    confirm: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    // Colores inspirados en zapatos de cuero y tonos oscuros elegantes
    val darkLeather = Color(0xFF2C2416)
    val brownLeather = Color(0xFF4A3C2A)
    val lightBrown = Color(0xFF8B7355)
    val cream = Color(0xFFD4C5B0)

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = cream
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Únete a nuestra comunidad",
                    textAlign = TextAlign.Center,
                    color = lightBrown,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))

                // Campo NOMBRE
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre completo", color = lightBrown) },
                    singleLine = true,
                    isError = nameError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cream,
                        unfocusedTextColor = cream,
                        focusedBorderColor = lightBrown,
                        unfocusedBorderColor = lightBrown.copy(alpha = 0.5f),
                        cursorColor = lightBrown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

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
                    Text(emailError, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo TELÉFONO
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono", color = lightBrown) },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cream,
                        unfocusedTextColor = cream,
                        focusedBorderColor = lightBrown,
                        unfocusedBorderColor = lightBrown.copy(alpha = 0.5f),
                        cursorColor = lightBrown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError != null) {
                    Text(phoneError, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.labelSmall)
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
                    Text(passError, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo CONFIRMAR CONTRASEÑA
                OutlinedTextField(
                    value = confirm,
                    onValueChange = onConfirmChange,
                    label = { Text("Confirmar contraseña", color = lightBrown) },
                    singleLine = true,
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showConfirm) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = lightBrown
                            )
                        }
                    },
                    isError = confirmError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cream,
                        unfocusedTextColor = cream,
                        focusedBorderColor = lightBrown,
                        unfocusedBorderColor = lightBrown.copy(alpha = 0.5f),
                        cursorColor = lightBrown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (confirmError != null) {
                    Text(confirmError, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(20.dp))

                // Botón REGISTRAR
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
                        Text("Registrando...", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Registrar", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, color = Color(0xFFFF6B6B))
                }

                Spacer(Modifier.height(16.dp))

                // Botón IR A LOGIN
                OutlinedButton(
                    onClick = onGoLogin,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = cream
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya tengo cuenta", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

