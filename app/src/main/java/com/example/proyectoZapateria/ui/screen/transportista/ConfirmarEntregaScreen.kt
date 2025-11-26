package com.example.proyectoZapateria.ui.screen.transportista

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.data.remote.entregas.dto.EntregaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDetalleUi
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

    val lastEntregaState = remember { mutableStateOf<EntregaDTO?>(null) }

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
        topBar = {
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Confirmar entrega",
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

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

                    uiState.entrega != null -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .background(colorScheme.background),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                item {
                                    // Encabezado de sección - ya tenemos topBar, así que no dejamos icono duplicado
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                item {
                                    DetalleSection(title = "Cliente y Destino") {
                                        ClienteInfoCard(entrega = uiState.entrega!!)
                                    }
                                }

                                item {
                                    DetalleSection(title = "Productos a Entregar") {
                                        ProductosListCard(productos = uiState.productos)
                                    }
                                }

                                item {
                                    OutlinedButton(
                                        onClick = {
                                            val direccion = uiState.entrega!!.direccionEntrega ?: "Sin dirección"

                                            val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(direccion)}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")

                                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(mapIntent)
                                            } else {
                                                val browserIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode(direccion)}")
                                                )
                                                context.startActivity(browserIntent)
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

                            val entregaParaUi: EntregaDTO? = uiState.entrega ?: lastEntregaState.value
                            // Mostrar siempre el botón si existe la entrega; deshabilitar si ya está completada o cancelada
                            if (entregaParaUi != null) {
                                val estado = entregaParaUi.estadoEntrega?.lowercase()
                                val entregada = estado == "completada" || estado == "cancelada"
                                Button(
                                    onClick = { if (!entregada) viewModel.marcarComoEntregado() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isConfirming && !entregada
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
                                        Text(if (entregada) "Entrega: ${estado ?: "desconocido"}" else "Marcar como Entregado", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

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

@Composable
fun ClienteInfoCard(entrega: EntregaDTO) {
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
                value = entrega.nombreCliente ?: "Cliente #${entrega.idBoleta}"
            )
            InfoRow(
                icon = Icons.Default.FmdGood,
                label = "Dirección",
                value = entrega.direccionEntrega ?: "Sin dirección"
            )
            InfoRow(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = "(+56) 9 1234 5678"
            )
        }
    }
}

@Composable
fun ProductosListCard(productos: List<ProductoDetalleUi>) {
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

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
fun ProductoRow(producto: ProductoDetalleUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.producto?.nombre ?: producto.producto?.nombre ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (producto.talla != null) {
                Text(
                    text = "Talla: ${producto.talla}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
