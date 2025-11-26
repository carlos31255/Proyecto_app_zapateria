package com.example.proyectoZapateria.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoZapateria.data.remote.geografia.dto.CiudadDTO
import com.example.proyectoZapateria.data.remote.geografia.dto.ComunaDTO
import com.example.proyectoZapateria.data.remote.geografia.dto.RegionDTO
import com.example.proyectoZapateria.viewmodel.AuthViewModel

@Composable
fun RegisterScreenVm(
    authViewModel: AuthViewModel,
    onRegisterOkGoLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by authViewModel.register.collectAsStateWithLifecycle()
    val regiones by authViewModel.regiones.collectAsStateWithLifecycle()
    val ciudades by authViewModel.ciudades.collectAsStateWithLifecycle()
    val comunas by authViewModel.comunas.collectAsStateWithLifecycle()
    val loadingRegiones by authViewModel.loadingRegiones.collectAsStateWithLifecycle()
    val loadingCiudades by authViewModel.loadingCiudades.collectAsStateWithLifecycle()
    val loadingComunas by authViewModel.loadingComunas.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Limpiar el formulario cuando se vuelve a la pantalla de registro
    LaunchedEffect(Unit) {
        authViewModel.clearRegisterForm()
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
            authViewModel.clearRegisterResult()
            onRegisterOkGoLogin()
        }
    }

    RegisterScreen(
        name = state.name,
        email = state.email,
        phone = state.phone,
        rut = state.rut,
        pass = state.pass,
        confirm = state.confirm,
        calle = state.calle,
        numeroPuerta = state.numeroPuerta,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        rutError = state.rutError,
        passError = state.passError,
        confirmError = state.confirmPassError,
        calleError = state.calleError,
        numeroPuertaError = state.numeroPuertaError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isLoading,
        errorMsg = state.errorMsg,
        regiones = regiones,
        ciudades = ciudades,
        comunas = comunas,
        loadingRegiones = loadingRegiones,
        loadingCiudades = loadingCiudades,
        loadingComunas = loadingComunas,
        selectedRegionId = state.idRegion,
        selectedCiudadId = state.idCiudad,
        selectedComunaId = state.idComuna,
        onRegionSelect = authViewModel::onSelectRegion,
        onCiudadSelect = authViewModel::onSelectCiudad,
        onComunaSelect = authViewModel::onSelectComuna,
        onNameChange = authViewModel::onRegisterNameChange,
        onEmailChange = authViewModel::onRegisterEmailChange,
        onPhoneChange = authViewModel::onRegisterPhoneChange,
        onRutChange = authViewModel::onRegisterRutChange,
        onPassChange = authViewModel::onRegisterPassChange,
        onConfirmChange = authViewModel::onConfirmChange,
        onCalleChange = authViewModel::onRegisterCalleChange,
        onNumeroPuertaChange = authViewModel::onRegisterNumeroPuertaChange,
        onSubmit = authViewModel::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
internal fun RegisterScreen( // internal por que la funcion es privada
    name: String,
    email: String,
    phone: String,
    rut: String,
    pass: String,
    confirm: String,
    calle: String,
    numeroPuerta: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    rutError: String?,
    passError: String?,
    confirmError: String?,
    calleError: String?,
    numeroPuertaError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    regiones: List<RegionDTO>,
    ciudades: List<CiudadDTO>,
    comunas: List<ComunaDTO>,
    loadingRegiones: Boolean,
    loadingCiudades: Boolean,
    loadingComunas: Boolean,
    selectedRegionId: Long?,
    selectedCiudadId: Long?,
    selectedComunaId: Long?,
    onRegionSelect: (Long?) -> Unit,
    onCiudadSelect: (Long?) -> Unit,
    onComunaSelect: (Long?) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRutChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onCalleChange: (String) -> Unit,
    onNumeroPuertaChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    // Nuevo esquema de colores morado/violeta claro - Material Design 3
    val colorScheme = MaterialTheme.colorScheme

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Únete a nuestra comunidad",
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))

                // Campo NOMBRE
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    isError = nameError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    ),
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
                if (nameError != null) {
                    Text(nameError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

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
                    Text(emailError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo TELÉFONO
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono") },
                    placeholder = { Text("Ej: +56912345678") },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant),
                    modifier = Modifier.fillMaxWidth())
                if (phoneError != null) {
                    Text(phoneError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo RUT
                OutlinedTextField(
                    value = rut,
                    onValueChange = onRutChange,
                    label = { Text("RUT") },
                    placeholder = { Text("12345678-9") },
                    singleLine = true,
                    isError = rutError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant),
                    modifier = Modifier.fillMaxWidth())
                if (rutError != null) {
                    Text(rutError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
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
                    Text(passError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo CONFIRMAR CONTRASEÑA
                OutlinedTextField(
                    value = confirm,
                    onValueChange = onConfirmChange,
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showConfirm) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = confirmError != null,
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
                if (confirmError != null) {
                    Text(confirmError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo CALLE
                OutlinedTextField(
                    value = calle,
                    onValueChange = { if (it.length <= 80) onCalleChange(it) },
                    label = { Text("Calle") },
                    placeholder = { Text("Ej: Av. Libertador Bernardo O'Higgins") },
                    singleLine = true,
                    isError = calleError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${calle.length}/80",
                            color = if (calle.length >= 80) colorScheme.error else colorScheme.onSurfaceVariant
                        )
                    }
                )
                if (calleError != null) {
                    Text(calleError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Campo NÚMERO PUERTA
                OutlinedTextField(
                    value = numeroPuerta,
                    onValueChange = { if (it.length <= 15) onNumeroPuertaChange(it) },
                    label = { Text("N° puerta") },
                    placeholder = { Text("Ej: 1234, 123-A, Depto 5B") },
                    singleLine = true,
                    isError = numeroPuertaError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${numeroPuerta.length}/15",
                            color = if (numeroPuerta.length >= 15) colorScheme.error else colorScheme.onSurfaceVariant
                        )
                    }
                )
                if (numeroPuertaError != null) {
                    Text(numeroPuertaError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // Region / Ciudad / Comuna selectors
                var expandedRegion by remember { mutableStateOf(false) }
                var expandedCiudad by remember { mutableStateOf(false) }
                var expandedComuna by remember { mutableStateOf(false) }

                val selectedRegionName = regiones.firstOrNull { it.id == selectedRegionId }?.nombre ?: "Seleccionar región"
                val selectedCiudadName = ciudades.firstOrNull { it.id == selectedCiudadId }?.nombre ?: "Seleccionar ciudad"
                val selectedComunaName = comunas.firstOrNull { it.id == selectedComunaId }?.nombre ?: "Seleccionar comuna"

                // Region
                OutlinedTextField(
                    value = selectedRegionName,
                    onValueChange = { },
                    label = { Text("Región") },
                    readOnly = true,
                    trailingIcon = {
                        if (loadingRegiones) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = { expandedRegion = !expandedRegion }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!loadingRegiones) expandedRegion = true },
                )
                DropdownMenu(expanded = expandedRegion, onDismissRequest = { expandedRegion = false }) {
                    DropdownMenuItem(text = { Text("No especificar") }, onClick = {
                        onRegionSelect(null)
                        expandedRegion = false
                    })
                    regiones.forEach { r ->
                        DropdownMenuItem(text = { Text(r.nombre) }, onClick = {
                            onRegionSelect(r.id)
                            expandedRegion = false
                        })
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Ciudad
                OutlinedTextField(
                    value = selectedCiudadName,
                    onValueChange = { },
                    label = { Text("Ciudad") },
                    readOnly = true,
                    trailingIcon = {
                        if (loadingCiudades) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = { if (ciudades.isNotEmpty()) expandedCiudad = !expandedCiudad }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (ciudades.isNotEmpty() && !loadingCiudades) expandedCiudad = true },
                    enabled = ciudades.isNotEmpty() && !loadingCiudades
                )
                DropdownMenu(expanded = expandedCiudad, onDismissRequest = { expandedCiudad = false }) {
                    DropdownMenuItem(text = { Text("No especificar") }, onClick = {
                        onCiudadSelect(null)
                        expandedCiudad = false
                    })
                    ciudades.forEach { c ->
                        DropdownMenuItem(text = { Text(c.nombre) }, onClick = {
                            onCiudadSelect(c.id)
                            expandedCiudad = false
                        })
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Comuna
                OutlinedTextField(
                    value = selectedComunaName,
                    onValueChange = { },
                    label = { Text("Comuna") },
                    readOnly = true,
                    trailingIcon = {
                        if (loadingComunas) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = { if (comunas.isNotEmpty()) expandedComuna = !expandedComuna }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (comunas.isNotEmpty() && !loadingComunas) expandedComuna = true },
                    enabled = comunas.isNotEmpty() && !loadingComunas
                )
                DropdownMenu(expanded = expandedComuna, onDismissRequest = { expandedComuna = false }) {
                    DropdownMenuItem(text = { Text("No especificar") }, onClick = {
                        onComunaSelect(null)
                        expandedComuna = false
                    })
                    comunas.forEach { cm ->
                        DropdownMenuItem(text = { Text(cm.nombre) }, onClick = {
                            onComunaSelect(cm.id)
                            expandedComuna = false
                        })
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Botón REGISTRAR
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
                        Text("Registrando...", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Registrar", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, color = colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                // Botón IR A LOGIN
                OutlinedButton(
                    onClick = onGoLogin,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ya tengo cuenta", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
