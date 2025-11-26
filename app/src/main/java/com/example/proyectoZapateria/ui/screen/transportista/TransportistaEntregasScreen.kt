@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.proyectoZapateria.ui.screen.transportista

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.viewmodel.transportista.TransportistaEntregasViewModel

@Composable
fun TransportistaEntregasScreen(
    navController: NavHostController

) {
    // Inyectamos el ViewModel usando Hilt (viewmodel de entregas)
    val viewModel: TransportistaEntregasViewModel = hiltViewModel()
    // Inyectamos AuthViewModel para obtener el usuario actual y permitir logout
    val authViewModel: com.example.proyectoZapateria.viewmodel.AuthViewModel = hiltViewModel()

    // Observamos el UiState del ViewModel de entregas
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Observamos usuario actual
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Colores del MaterialTheme
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            // Top bar simple con flecha de regreso y título
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Mis entregas",
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Opcional: botón de cerrar sesión si se desea mantener
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Route.Login.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Gestiona tus entregas pendientes y completadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tarjetas de resumen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Usamos los datos dinámicos del uiState
                    ResumenCard(
                        icon = Icons.Default.Schedule,
                        titulo = "Pendientes",
                        conteo = uiState.pendientesCount.toString()
                    )

                    ResumenCard(
                        icon = Icons.Default.CheckCircle,
                        titulo = "Completadas",
                        conteo = uiState.completadasCount.toString()
                    )
                }

                Text(
                    text = "Entregas de Hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Contenedor que muestra el loader y el contenido
                Box(modifier = Modifier.fillMaxSize()) {
                    // Lista / Empty / Error
                    when {
                        // --- ESTADO DE ERROR  ---
                        uiState.error != null -> {
                            Text(
                                text = "Error al cargar entregas: ${uiState.error}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(top = 32.dp)
                            )
                        }

                        // --- ESTADO VACÍO ---
                        uiState.entregas.isEmpty() && !uiState.isLoading -> {
                            Text(
                                text = "No tienes entregas asignadas por ahora.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(top = 32.dp)
                            )
                        }

                        // --- ESTADO CON DATOS ---
                        else -> {
                            // Si hay datos, mostrar la lista (si está vacía y loading=true, no entra aquí)
                            if (uiState.entregas.isNotEmpty()) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.entregas, key = { it.idEntrega ?: 0 }) { entrega ->
                                        EntregaCard(
                                            entrega = entrega,
                                            onClick = {
                                                //  Navegamos a confirmar/completar entrega al hacer click
                                                navController.navigate(
                                                    Route.TransportistaConfirmarEntrega.path.replace("{idEntrega}", (entrega.idEntrega ?: 0).toString())
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- INDICADOR DE CARGA (siempre centrado) ---
                    if (uiState.isLoading) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center),
                            shape = CircleShape,
                            color = colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}


// (Composable auxiliar para no duplicar código en las tarjetas de resumen)
@Composable
fun RowScope.ResumenCard(
    icon: ImageVector,
    titulo: String,
    conteo: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
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
                contentDescription = titulo,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = conteo,
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
fun EntregaCard(
    entrega: EntregaDTO,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Entrega",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entrega.numeroBoleta ?: "Orden #${entrega.idBoleta}",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entrega.nombreCliente ?: "Cliente #${entrega.idBoleta}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entrega.direccionEntrega ?: "Sin dirección",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Estado
                Surface(
                    color = if (entrega.estadoEntrega.lowercase() == "pendiente")
                        colorScheme.secondaryContainer
                    else
                        colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entrega.estadoEntrega.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entrega.estadoEntrega.lowercase() == "pendiente")
                            colorScheme.onSecondaryContainer
                        else
                            colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Botón para abrir Google Maps
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    val direccion = entrega.direccionEntrega ?: "Sin dirección"

                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(direccion)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
                        )
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ver ubicación",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver ubicación en Maps")
            }
        }
    }
}
