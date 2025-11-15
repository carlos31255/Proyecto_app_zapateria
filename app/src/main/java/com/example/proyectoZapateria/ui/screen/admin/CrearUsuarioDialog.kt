package com.example.proyectoZapateria.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoZapateria.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioDialog(
    viewModel: UsuarioViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.crearUsuarioState.collectAsStateWithLifecycle()
    val roles by viewModel.roles.collectAsStateWithLifecycle()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Cerrar diálogo si se creó exitosamente
    LaunchedEffect(state.success) {
        if (state.success) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Crear Nuevo Usuario")
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error general
                if (state.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Nombre
                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = { viewModel.actualizarNombre(it) },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.nombreError != null,
                    supportingText = state.nombreError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    enabled = !state.isLoading,
                    singleLine = true
                )

                // Apellido
                OutlinedTextField(
                    value = state.apellido,
                    onValueChange = { viewModel.actualizarApellido(it) },
                    label = { Text("Apellido *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.apellidoError != null,
                    supportingText = state.apellidoError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    enabled = !state.isLoading,
                    singleLine = true
                )

                // RUT
                OutlinedTextField(
                    value = state.rut,
                    onValueChange = { viewModel.actualizarRut(it) },
                    label = { Text("RUT *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.rutError != null,
                    supportingText = state.rutError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    enabled = !state.isLoading,
                    singleLine = true,
                    placeholder = { Text("12345678-9") }
                )

                // Teléfono
                OutlinedTextField(
                    value = state.telefono,
                    onValueChange = { viewModel.actualizarTelefono(it) },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.telefonoError != null,
                    supportingText = state.telefonoError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    placeholder = { Text("+56912345678") }
                )

                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.actualizarEmail(it) },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                // Username
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.actualizarUsername(it) },
                    label = { Text("Usuario *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.usernameError != null,
                    supportingText = state.usernameError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                    enabled = !state.isLoading,
                    singleLine = true
                )

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.actualizarPassword(it) },
                    label = { Text("Contraseña *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { { Text(it) } } ?: {
                        Text(
                            "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número, 1 símbolo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !state.isLoading,
                    singleLine = true
                )

                // Confirm Password
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.actualizarConfirmPassword(it) },
                    label = { Text("Confirmar Contraseña *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.confirmPasswordError != null,
                    supportingText = state.confirmPasswordError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !state.isLoading,
                    singleLine = true
                )

                // Rol Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded && !state.isLoading }
                ) {
                    OutlinedTextField(
                        value = state.rolSeleccionado?.nombreRol ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = !state.isLoading),
                        isError = state.rolError != null,
                        supportingText = state.rolError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Shield, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        enabled = !state.isLoading,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.nombreRol ?: "Sin nombre") },
                                onClick = {
                                    viewModel.actualizarRolSeleccionado(rol)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Campos específicos para Transportista (idRol = 3)
                if (state.rolSeleccionado?.idRol == 3) {
                    // Licencia
                    OutlinedTextField(
                        value = state.licencia,
                        onValueChange = { viewModel.actualizarLicencia(it) },
                        label = { Text("Licencia *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.licenciaError != null,
                        supportingText = state.licenciaError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        enabled = !state.isLoading,
                        singleLine = true,
                        placeholder = { Text("Ej: Clase B") }
                    )

                    // Vehículo
                    OutlinedTextField(
                        value = state.vehiculo,
                        onValueChange = { viewModel.actualizarVehiculo(it) },
                        label = { Text("Vehículo *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.vehiculoError != null,
                        supportingText = state.vehiculoError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, null) },
                        enabled = !state.isLoading,
                        singleLine = true,
                        placeholder = { Text("Ej: Camioneta Toyota") }
                    )
                }

                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.crearUsuario() },
                enabled = !state.isLoading
            ) {
                Text("Crear Usuario")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

