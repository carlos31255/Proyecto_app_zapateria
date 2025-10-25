package com.example.proyectoZapateria.ui.screen.transportista

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle
import com.example.proyectoZapateria.data.local.entrega.EntregaConDetalles
import com.example.proyectoZapateria.viewmodel.transportista.DetalleEntregaViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntregaScreen(
    navController: NavHostController,
    viewModel: DetalleEntregaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current // Para el intent de Google Maps

    // Efecto para navegar hacia atrás automáticamente cuando la actualización sea exitosa
    LaunchedEffect(uiState.actualizacionExitosa) {
        if (uiState.actualizacionExitosa) {
            // Vuelve a la pantalla anterior (la lista)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Entrega") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // El botón para marcar como completado va en la BottomBar
            if (uiState.entrega?.estadoEntrega == "pendiente") { // Solo mostrar si está pendiente
                Button(
                    onClick = { viewModel.marcarComoEntregado() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = "Completar",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Marcar como Entregado", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        // Contenido principal de la pantalla
        when {
            // --- ESTADO DE CARGA ---
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
            // --- ESTADO DE ERROR ---
            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    color = colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                )
            }
            // --- ESTADO CON DATOS ---
            uiState.entrega != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- Sección Cliente ---
                    item {
                        DetalleSection(title = "Cliente y Destino") {
                            // Pasamos los detalles de la entrega
                            ClienteInfoCard(entrega = uiState.entrega!!)
                        }
                    }

                    // --- Sección Productos ---
                    item {
                        DetalleSection(title = "Productos a Entregar") {
                            // Pasamos la lista de productos
                            ProductosListCard(productos = uiState.productos)
                        }
                    }

                    // --- Sección Mapa (Botón) ---
                    item {
                        OutlinedButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${uiState.entrega!!.getDireccionCompleta()}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                // Intenta abrir, si falla no hace nada
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Google Maps no está instalado
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FmdGood, contentDescription = "Mapa", modifier = Modifier.padding(end = 8.dp))
                            Text("Ver en Google Maps")
                        }
                    }
                }
            }
        }
    }
}

// --- COMPOSABLES AUXILIARES ---


 //Un contenedor genérico para una sección de la pantalla

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


 //Muestra la información del cliente y la dirección.

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


//Muestra la lista de productos (zapatos) en la entrega.
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
                Text("No se pudieron cargar los productos.")
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Una fila para la lista de productos.
 */
@Composable
fun ProductoRow(producto: ProductoDetalle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombreZapato,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${producto.marca} - Talla: ${producto.talla}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "x${producto.cantidad}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}