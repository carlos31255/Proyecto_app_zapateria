package com.example.proyectoZapateria.ui.screen.transportista

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FmdGood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles
import com.example.proyectoZapateria.viewmodel.transportista.ConfirmarEntregaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarEntregaScreen(
    navController: NavHostController,
    viewModel: ConfirmarEntregaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    // Mantenemos un snapshot del último `entrega` no-nulo para evitar que
    // el bottomBar desaparezca por emisiones transitorias nulas (por ejemplo al abrir Maps)
    val lastEntregaState = remember { mutableStateOf<EntregaConDetalles?>(null) }
    LaunchedEffect(uiState.entrega) {
        uiState.entrega?.let { lastEntregaState.value = it }
    }

    LaunchedEffect(key1 = uiState.actualizacionExitosa) {
        if (uiState.actualizacionExitosa) {
            Toast.makeText(context, "Entrega marcada como entregada", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        bottomBar = {
            // El botón para marcar como completado va en la BottomBar
            // Preferimos el estado en vivo (`uiState.entrega`) y usamos el snapshot como respaldo
            val entregaParaUi: EntregaConDetalles? = uiState.entrega ?: lastEntregaState.value
            if (entregaParaUi?.estadoEntrega == "pendiente") { // Solo mostrar si está pendiente
                Button(
                    onClick = { viewModel.marcarComoEntregado() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isConfirming
                ) {
                    if (uiState.isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = "Completar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Marcar como Entregado", fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        // Contenido principal de la pantalla
        // Mostramos siempre la flecha de regreso en la parte superior, independientemente del estado de carga
        // Root box: overlay back button and content area
        Box(modifier = Modifier.fillMaxSize()) {
            // Content container: apply scaffold paddings (including top) so content is laid out below global topBar
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                )

            // Contenido principal
            Box(modifier = contentModifier) {
                // Barra superior local (botón de regresar visible y contrastado)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // opcional: título o espacio
                }

                when {
                    // --- ESTADO DE CARGA ---
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    // --- ESTADO DE ERROR ---
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    // --- ESTADO CON DATOS ---
                    uiState.entrega != null -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorScheme.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // --- Sección Cliente ---
                            item {
                                DetalleSection(title = "Cliente y Destino") {
                                    ClienteInfoCard(entrega = uiState.entrega!!)
                                }
                            }

                            // --- Sección Productos ---
                            item {
                                DetalleSection(title = "Productos a Entregar") {
                                    ProductosListCard(productos = uiState.productos)
                                }
                            }

                            // --- Sección Mapa (Botón) ---
                            item {
                                OutlinedButton(
                                    onClick = {
                                        val direccion = uiState.entrega!!.getDireccionCompleta()
                                        val gmmIntentUri = direccion.toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            // Mostrar mensaje específico si hay info
                                            Toast.makeText(
                                                context,
                                                e.message ?: "Google Maps no está instalado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.FmdGood,
                                        contentDescription = "Mapa",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Ver en Google Maps")
                                }
                            }

                            // --- Sección Observación (Input que usa el ViewModel) ---
                            item {
                                OutlinedTextField(
                                    value = uiState.observacionInput,
                                    onValueChange = { viewModel.onObservacionChange(it) },
                                    label = { Text("Observación (opcional)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    singleLine = false,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }

            // Overlay del back button: subir unos dp más para no tapar el texto
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp)
                    .offset(y = -(padding.calculateTopPadding() + 8.dp))
            ) {
                Surface(
                    shape = CircleShape,
                    color = colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// --- COMPOSABLES AUXILIARES ---

// Sección genérica con título y contenido
@Composable
fun DetalleSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

// Funcion para mostrar la informacion del cliente en una tarjeta
@Composable
fun ClienteInfoCard(entrega: EntregaConDetalles) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoRow(
                icon = Icons.Default.Person,
                label = "Cliente",
                value = entrega.clienteNombre
            )
            InfoRow(
                icon = Icons.Default.FmdGood,
                label = "Dirección",
                value = entrega.getDireccionCompleta()
            )
            InfoRow(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = "(+56) 9 1234 5678" // TODO: Añadir teléfono a la consulta
            )
        }
    }
}

// Funcion para mostrar la lista de productos en una tarjeta
@Composable
fun ProductosListCard(productos: List<ProductoDetalle>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (productos.isEmpty()) {
                Text(
                    text = "No se pudieron cargar los productos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                productos.forEachIndexed { index, producto ->
                    ProductoRow(producto = producto)
                    if (index < productos.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Una fila de información simple con Icono, Label y Valor.
 */
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Una fila que representa un producto individual.
 */
@Composable
fun ProductoRow(producto: ProductoDetalle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombreZapato,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Marca: ${producto.marca}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Talla: ${producto.talla}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Cant: ${producto.cantidad}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
