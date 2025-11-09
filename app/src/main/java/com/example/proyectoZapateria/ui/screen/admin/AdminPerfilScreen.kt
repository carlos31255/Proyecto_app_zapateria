package com.example.proyectoZapateria.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.admin.AdminPerfilViewModel

@Composable
fun AdminPerfilScreen(
    navController: NavHostController,
    viewModel: AdminPerfilViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val editNombre by viewModel.editNombre.collectAsStateWithLifecycle()
    val editApellido by viewModel.editApellido.collectAsStateWithLifecycle()
    val editEmail by viewModel.editEmail.collectAsStateWithLifecycle()
    val editTelefono by viewModel.editTelefono.collectAsStateWithLifecycle()
    val editCalle by viewModel.editCalle.collectAsStateWithLifecycle()
    val editNumeroPuerta by viewModel.editNumeroPuerta.collectAsStateWithLifecycle()

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
            return
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = colorScheme.primaryContainer,
                        tonalElevation = 2.dp,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Error: ${uiState.error}",
                        color = colorScheme.error
                    )
                }
            }
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Botón de regreso
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = colorScheme.primaryContainer,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colorScheme.onPrimaryContainer
                )
            }
        }

        // Header con avatar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Avatar Admin",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre completo
                Text(
                    text = viewModel.getNombreCompleto(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )

                // Rol
                Surface(
                    color = colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = uiState.rol,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón editar
                if (!isEditing) {
                    OutlinedButton(
                        onClick = { viewModel.startEdit() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar perfil")
                    }
                }
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isEditing) {
                // Campos editables
                OutlinedTextField(
                    value = editNombre,
                    onValueChange = { viewModel.updateEditField(nombre = it) },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = editApellido,
                    onValueChange = { viewModel.updateEditField(apellido = it) },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { viewModel.updateEditField(email = it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = editTelefono,
                    onValueChange = { viewModel.updateEditField(telefono = it) },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = editCalle,
                    onValueChange = { viewModel.updateEditField(calle = it) },
                    label = { Text("Calle") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, null) }
                )

                OutlinedTextField(
                    value = editNumeroPuerta,
                    onValueChange = { viewModel.updateEditField(numeroPuerta = it) },
                    label = { Text("Número de puerta") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    singleLine = true
                )

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.guardarCambios { success, error ->
                                if (success) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Perfil actualizado exitosamente",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        error ?: "Error al actualizar",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar")
                    }

                    OutlinedButton(
                        onClick = { viewModel.cancelEdit() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar")
                    }
                }
            } else {
                // Información de solo lectura
                AdminInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Usuario",
                    value = uiState.username
                )

                AdminInfoCard(
                    icon = Icons.Default.Badge,
                    label = "RUT",
                    value = uiState.rut
                )

                AdminInfoCard(
                    icon = Icons.Default.Email,
                    label = "Correo Electrónico",
                    value = uiState.email.ifBlank { "No especificado" }
                )

                AdminInfoCard(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = uiState.telefono.ifBlank { "No especificado" }
                )

                AdminInfoCard(
                    icon = Icons.Default.Home,
                    label = "Dirección",
                    value = if (uiState.calle.isNotBlank() || uiState.numeroPuerta.isNotBlank()) {
                        "${uiState.calle}, ${uiState.numeroPuerta}"
                    } else {
                        "No especificada"
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón cerrar sesión
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun AdminInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

