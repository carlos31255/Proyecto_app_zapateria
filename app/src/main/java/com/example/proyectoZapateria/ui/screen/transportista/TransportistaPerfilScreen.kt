package com.example.proyectoZapateria.ui.screen.transportista

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.transportista.TransportistaPerfilViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun TransportistaPerfilScreen(
    navController: NavHostController,
    viewModel: TransportistaPerfilViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // Estado de carga y errores
    val uiStateState = viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = uiStateState.value

    // Estado de edición
    val isEditingState = viewModel.isEditing.collectAsStateWithLifecycle()
    val isEditing = isEditingState.value

    // Campos editables (Asumiendo que existen en el ViewModel igual que en Cliente)
    val editNombreState = viewModel.editNombre.collectAsStateWithLifecycle()
    val editNombre = editNombreState.value
    val editApellidoState = viewModel.editApellido.collectAsStateWithLifecycle()
    val editApellido = editApellidoState.value
    val editTelefonoState = viewModel.editTelefono.collectAsStateWithLifecycle()
    val editTelefono = editTelefonoState.value
    val editLicenciaState = viewModel.editLicencia.collectAsStateWithLifecycle()
    val editLicencia = editLicenciaState.value
    val editVehiculoState = viewModel.editVehiculo.collectAsStateWithLifecycle()
    val editVehiculo = editVehiculoState.value

    val currentUserState = authViewModel.currentUser.collectAsStateWithLifecycle()
    val currentUser = currentUserState.value

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    // Altura aproximada del header para posicionar el loader
    val headerTopOffset: Dp = 120.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mostrar header solo cuando no este cargando ni en error
            if (!uiState.isLoading && uiState.error == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(shape = CircleShape, color = colorScheme.primaryContainer, tonalElevation = 2.dp) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Mi perfil", color = colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Botón para entrar a editar (alineado a la derecha en el header)
                        IconButton(onClick = { if (isEditing == false) viewModel.startEdit() else viewModel.cancelEdit() }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = if (isEditing == false) "Editar" else "Cancelar", tint = colorScheme.onPrimaryContainer)
                        }
                    }

                    // Avatar y nombre (ahora dentro del header Column)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = colorScheme.primary,
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentUser?.nombre ?: uiState.nombre.ifBlank { "Transportista" },
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Transportista",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Contenido principal
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)) {

                when {
                    uiState.error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${uiState.error}", color = colorScheme.error)
                        }
                    }
                    else -> {
                        val scroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scroll)
                                .imePadding()
                                .navigationBarsPadding()
                                .padding(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            if (isEditing) {
                                OutlinedTextField(value = editNombre, onValueChange = { new -> viewModel.updateEditField(nombre = new) }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editApellido, onValueChange = { new -> viewModel.updateEditField(apellido = new) }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editTelefono, onValueChange = { new -> viewModel.updateEditField(telefono = new) }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(value = editLicencia, onValueChange = { new -> viewModel.updateEditField(licencia = new) }, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editVehiculo, onValueChange = { new -> viewModel.updateEditField(vehiculo = new) }, label = { Text("Vehículo") }, modifier = Modifier.fillMaxWidth())

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        viewModel.guardarCambios { ok, err ->
                                            if (ok) android.widget.Toast.makeText(context, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show() else android.widget.Toast.makeText(context, err ?: "Error", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                                    OutlinedButton(onClick = { viewModel.cancelEdit() }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                                }

                            } else {
                                InfoCard(icon = Icons.Default.Email, label = "Correo Electrónico", value = uiState.email)
                                InfoCard(icon = Icons.Default.Phone, label = "Teléfono", value = uiState.telefono)
                                InfoCard(icon = Icons.Default.Badge, label = "Licencia", value = uiState.licencia)
                                InfoCard(icon = Icons.Default.DirectionsCar, label = "Vehículo", value = uiState.vehiculo)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Estadísticas de Entregas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    EstadisticaCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.LocalShipping,
                                        label = "Total",
                                        value = uiState.totalEntregas.toString(),
                                        containerColor = colorScheme.primaryContainer,
                                        contentColor = colorScheme.onPrimaryContainer
                                    )

                                    EstadisticaCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.CheckCircle,
                                        label = "Completadas",
                                        value = uiState.entregasCompletadas.toString(),
                                        containerColor = colorScheme.tertiaryContainer,
                                        contentColor = colorScheme.onTertiaryContainer
                                    )
                                }

                                EstadisticaCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Default.Schedule,
                                    label = "Pendientes",
                                    value = uiState.entregasPendientes.toString(),
                                    containerColor = colorScheme.secondaryContainer,
                                    contentColor = colorScheme.onSecondaryContainer
                                )

                            }

                        }
                    }
                }
            }
        }

        // Overlay del loader que cubre TODO cuando está cargando
        if (uiState.isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surface.copy(alpha = 0.6f))) {
                // posicionar el loader cerca de la parte superior donde está el avatar
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = headerTopOffset), contentAlignment = Alignment.TopCenter) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = colorScheme.primary, modifier = Modifier.size(44.dp))
                        }
                    }
                }
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

@Composable
fun EstadisticaCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
