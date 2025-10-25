
package com.example.proyectoZapateria.ui.screen.transportista

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    navController: NavHostController,
    // Inyectamos el ViewModel usando Hilt
    viewModel: TransportistaEntregasViewModel = hiltViewModel()
) {
    // Colores del tema oscuro de cuero
    val darkLeather = Color(0xFF2C2416)
    val brownLeather = Color(0xFF4A3C2A)
    val lightBrown = Color(0xFF8B7355)
    val cream = Color(0xFFD4C5B0)

    // Observamos el UiState del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(darkLeather, brownLeather)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título principal
            Text(
                text = "Mis Entregas",
                style = MaterialTheme.typography.headlineMedium,
                color = cream,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Gestiona tus entregas pendientes y completadas",
                style = MaterialTheme.typography.bodyMedium,
                color = lightBrown,
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
                    conteo = uiState.pendientesCount.toString(),
                    lightBrown = lightBrown,
                    cream = cream
                )

                ResumenCard(
                    icon = Icons.Default.CheckCircle,
                    titulo = "Completadas",
                    conteo = uiState.completadasCount.toString(),
                    lightBrown = lightBrown,
                    cream = cream
                )
            }

            // Lista de entregas
            Text(
                text = "Entregas de Hoy",
                style = MaterialTheme.typography.titleMedium,
                color = cream,
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
                        color = cream
                    )
                }

                // --- ESTADO DE ERROR  ---
                uiState.error != null -> {
                    Text(
                        text = "Error al cargar entregas: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red.copy(alpha = 0.8f),
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
                        color = lightBrown,
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
                                    //  Navegamos al detalle al hacer click
                                    // (Asegúrate de tener esta ruta en tu NavGraph)
                                    navController.navigate(Route.TransportistaEntregaDetalle.path + "/${entrega.idEntrega}")
                                },
                                darkLeather = darkLeather,
                                lightBrown = lightBrown,
                                cream = cream
                            )
                        }
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
    conteo: String,
    lightBrown: Color,
    cream: Color
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = lightBrown.copy(alpha = 0.3f)
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
                tint = cream,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = conteo, // Dato dinámico
                style = MaterialTheme.typography.headlineSmall,
                color = cream,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = lightBrown
            )
        }
    }
}


@Composable
fun EntregaCard(
    entrega: EntregaConDetalles, // Recibimos el objeto POJO
    onClick: () -> Unit,
    darkLeather: Color,
    lightBrown: Color,
    cream: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // 13. Hacemos la tarjeta clickeable
        colors = CardDefaults.cardColors(
            containerColor = lightBrown.copy(alpha = 0.2f)
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
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Entrega",
                tint = cream,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    // Usamos las funciones auxiliares del POJO
                    text = entrega.getNumeroOrdenFormateado(),
                    style = MaterialTheme.typography.titleMedium,
                    color = cream,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entrega.clienteNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = lightBrown
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entrega.getDireccionCompleta(),
                    style = MaterialTheme.typography.bodySmall,
                    color = lightBrown.copy(alpha = 0.8f)
                )
            }

            // Estado
            Surface(
                //  Usamos el estado dinámico
                color = if (entrega.estadoEntrega == "pendiente") lightBrown else Color(0xFF4CAF50),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    // Capitalizamos el estado (ej: "Pendiente")
                    text = entrega.estadoEntrega.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = darkLeather,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
