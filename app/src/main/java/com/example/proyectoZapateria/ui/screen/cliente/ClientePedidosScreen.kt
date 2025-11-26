package com.example.proyectoZapateria.ui.screen.cliente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.cliente.ClientePedidosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.Instant
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CheckCircle
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDetalleUi
import java.util.Date

@Composable
fun ClientePedidosScreen(
    navController: NavHostController,
    viewModel: ClientePedidosViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val clpFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")) }

    // Mostrar siempre el header (scaffold parcial) y el contenido debajo
    Column(modifier = Modifier.fillMaxSize()) {

        // Header con diseño (mostrado inmediatamente)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Mis Pedidos",
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.pedidos.size} pedidos",
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                IconButton(onClick = { viewModel.loadPedidos() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refrescar",
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Contenido: spinner / error / lista
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${uiState.error}", color = colorScheme.error)
                    }
                }
                else -> {
                    if (uiState.pedidos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes pedidos aún")
                        }
                    } else {
                        LazyColumn(modifier = Modifier
                            .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.pedidos) { pedidoConEntrega ->
                                val boleta = pedidoConEntrega.boleta
                                val entrega = pedidoConEntrega.entrega

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
                                ) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "Boleta: ${boleta.id ?: "#?"}", style = MaterialTheme.typography.titleMedium, color = colorScheme.onSurface)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                val fechaTexto = runCatching {
                                                    Instant.parse(boleta.fechaVenta).let { dateFormatter.format(Date.from(it)) }
                                                }.getOrDefault("-")

                                                Text(text = "Fecha: $fechaTexto", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)

                                                // Mostrar estado de entrega
                                                if (entrega != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Surface(
                                                        color = when (entrega.estadoEntrega) {
                                                            "pendiente" -> colorScheme.secondaryContainer
                                                            "entregada" -> colorScheme.tertiaryContainer
                                                            "completada" -> colorScheme.primaryContainer
                                                            else -> colorScheme.surfaceVariant
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = when (entrega.estadoEntrega) {
                                                                "pendiente" -> "En camino"
                                                                "entregada" -> "Entregado - Confirmar recepción"
                                                                "completada" -> "Completado"
                                                                else -> entrega.estadoEntrega
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = when (entrega.estadoEntrega) {
                                                                "pendiente" -> colorScheme.onSecondaryContainer
                                                                "entregada" -> colorScheme.onTertiaryContainer
                                                                "completada" -> colorScheme.onPrimaryContainer
                                                                else -> colorScheme.onSurfaceVariant
                                                            },
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            IconButton(onClick = { expanded = !expanded }) {
                                                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = if (expanded) "Ocultar productos" else "Ver productos")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Total: ${clpFormatter.format(boleta.total)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)

                                        if (expanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            // Consumir el flow de productos para esta boleta (ProductoDetalleUi)
                                            val productosFlow = viewModel.getProductosForBoleta(boleta.id ?: 0L)
                                            val productos by productosFlow.collectAsStateWithLifecycle(initialValue = emptyList<ProductoDetalleUi>())

                                            if (productos.isEmpty()) {
                                                Text(text = "(Sin detalles de productos)", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    productos.forEach { p ->
                                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(text = p.producto?.nombre ?: "-", style = MaterialTheme.typography.bodyMedium)
                                                                Text(text = "Talla: ${p.talla} • Marca: ${p.marcaName}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                                            }
                                                            Text(text = "x${p.cantidad}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Botón para confirmar como completado (solo si está "entregada")
                                        if (entrega != null && entrega.estadoEntrega == "entregada") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val entregaId = entrega.idEntrega
                                                    if (entregaId != null) {
                                                        viewModel.confirmarPedidoCompletado(entregaId) { success, error ->
                                                            if (success) {
                                                                Toast.makeText(context, "Pedido confirmado como completado", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, error ?: "Error al confirmar", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "ID de entrega inválido", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                 modifier = Modifier.fillMaxWidth(),
                                                 shape = RoundedCornerShape(8.dp)
                                             ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Confirmar",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Confirmar Recepción")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
