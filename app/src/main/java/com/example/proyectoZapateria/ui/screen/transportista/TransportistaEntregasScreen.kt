package com.example.proyectoZapateria.ui.screen.transportista

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.viewmodel.transportista.TransportistaEntregasViewModel

@Composable
fun TransportistaEntregasScreen(
    navController: NavHostController

) {
    // Inyectamos el ViewModel usando Hilt (ahora tiene acceso al transportistaId)
    val viewModel: TransportistaEntregasViewModel = hiltViewModel()

    // Observamos el UiState del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Colores del MaterialTheme
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        // Botón de regreso dentro de un círculo (igual que en ConfirmarEntregaScreen)
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = colorScheme.primaryContainer,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colorScheme.onPrimaryContainer
                )
            }
        }

        // Título principal
        Text(
            text = "Mis Entregas",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

            Text(
                text = "Gestiona tus entregas pendientes y completadas",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjetas de resumen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //  Usamos los datos dinámicos del uiState
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

            // Lista de entregas
            Text(
                text = "Entregas de Hoy",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Manejamos los 3 estados: Carga, Vacío y Datos
            when {
                // --- ESTADO DE CARGA ---
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.Center)
                            .padding(top = 32.dp),
                        color = colorScheme.primary
                    )
                }

                // --- ESTADO DE ERROR  ---
                uiState.error != null -> {
                    Text(
                        text = "Error al cargar entregas: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                    )
                }

                // --- ESTADO VACÍO ---
                uiState.entregas.isEmpty() -> {
                    Text(
                        text = "No tienes entregas asignadas por ahora.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                    )
                }

                // --- ESTADO CON DATOS ---
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Usamos la lista de entregas del uiState
                        items(uiState.entregas, key = { it.idEntrega }) { entrega ->
                            EntregaCard(
                                entrega = entrega,
                                onClick = {
                                    //  Navegamos a confirmar/completar entrega al hacer click
                                    navController.navigate(
                                        Route.TransportistaConfirmarEntrega.path.replace("{idEntrega}", entrega.idEntrega.toString())
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
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
    entrega: EntregaConDetalles,
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
                        text = entrega.getNumeroOrdenFormateado(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entrega.clienteNombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entrega.getDireccionCompleta(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Estado
                Surface(
                    color = if (entrega.estadoEntrega == "pendiente")
                        colorScheme.secondaryContainer
                    else
                        colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entrega.estadoEntrega.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entrega.estadoEntrega == "pendiente")
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
                    // Construir la dirección completa
                    val direccion = entrega.getDireccionCompleta()

                    // Crear intent para abrir Google Maps
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(direccion)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    // Verificar si Google Maps está instalado
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Si no está instalado, abrir en el navegador
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
