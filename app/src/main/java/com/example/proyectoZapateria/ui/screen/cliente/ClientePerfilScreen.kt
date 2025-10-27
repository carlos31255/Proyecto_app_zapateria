package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.cliente.ClientePerfilViewModel
import com.example.proyectoZapateria.navigation.Route
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext

@Composable
fun ClientePerfilScreen(
    navController: NavHostController,
    viewModel: ClientePerfilViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiStateState = viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = uiStateState.value

    val isEditingState = viewModel.isEditing.collectAsStateWithLifecycle()
    val isEditing = isEditingState.value

    val editNombreState = viewModel.editNombre.collectAsStateWithLifecycle()
    val editNombre = editNombreState.value

    val editApellidoState = viewModel.editApellido.collectAsStateWithLifecycle()
    val editApellido = editApellidoState.value

    val editEmailState = viewModel.editEmail.collectAsStateWithLifecycle()
    val editEmail = editEmailState.value

    val editTelefonoState = viewModel.editTelefono.collectAsStateWithLifecycle()
    val editTelefono = editTelefonoState.value

    val editCalleState = viewModel.editCalle.collectAsStateWithLifecycle()
    val editCalle = editCalleState.value

    val editNumeroPuertaState = viewModel.editNumeroPuerta.collectAsStateWithLifecycle()
    val editNumeroPuerta = editNumeroPuertaState.value

    val currentUserState = authViewModel.currentUser.collectAsStateWithLifecycle()
    val currentUser = currentUserState.value
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
            return
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.Start)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Error: ${uiState.error}", color = colorScheme.error)
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
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = colorScheme.onBackground
            )
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
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = currentUser?.nombreRol ?: "Cliente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                // Botón editar
                if (isEditing == false) {
                    OutlinedButton(onClick = { viewModel.startEdit() }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar perfil")
                    }
                }
            }
        }

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
                OutlinedTextField(value = editNombre, onValueChange = { new -> viewModel.updateEditField(nombre = new) }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editApellido, onValueChange = { new -> viewModel.updateEditField(apellido = new) }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editEmail, onValueChange = { new -> viewModel.updateEditField(email = new) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = editTelefono, onValueChange = { new -> viewModel.updateEditField(telefono = new) }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = editCalle, onValueChange = { new -> viewModel.updateEditField(calle = new) }, label = { Text("Calle") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editNumeroPuerta, onValueChange = { new -> viewModel.updateEditField(numeroPuerta = new) }, label = { Text("Número puerta") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        viewModel.guardarCambios { ok, err ->
                            if (ok) {
                                android.widget.Toast.makeText(context, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, err ?: "Error", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Guardar")
                    }

                    OutlinedButton(onClick = { viewModel.cancelEdit() }, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                }
            } else {
                InfoCard(
                    icon = Icons.Default.Email,
                    label = "Correo Electrónico",
                    value = uiState.email
                )

                InfoCard(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = uiState.telefono
                )

                InfoCard(
                    icon = Icons.Default.Home,
                    label = "Dirección",
                    value = if (uiState.calle.isNotBlank() || uiState.numeroPuerta.isNotBlank()) "${uiState.calle}, ${uiState.numeroPuerta}" else "No especificada"
                )

                InfoCard(
                    icon = Icons.Default.Badge,
                    label = "Categoría",
                    value = uiState.categoria.ifBlank { "-" }
                )

                InfoCard(
                    icon = Icons.Default.Receipt,
                    label = "Total Pedidos",
                    value = uiState.totalPedidos.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
