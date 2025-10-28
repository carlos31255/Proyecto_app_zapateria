package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.cliente.ClientePedidosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.proyectoZapateria.data.local.detalleboleta.ProductoDetalle

@Composable
fun ClientePedidosScreen(
    navController: NavHostController,
    viewModel: ClientePedidosViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val clpFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")) }

    // Estados
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
            return
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = colorScheme.error)
            }
            return
        }
    }

    // Contenedor principal sin usar Scaffold (hay un global en la app)
    Column(modifier = Modifier.fillMaxSize()) {

        // Header con diseño
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
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onPrimaryContainer
                        )
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

        // Lista de pedidos
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {

            if (uiState.pedidos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes pedidos aún")
                }
                return
            }

            LazyColumn(modifier = Modifier
                .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.pedidos) { boleta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Boleta: ${boleta.numeroBoleta}", style = MaterialTheme.typography.titleMedium, color = colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Fecha: ${dateFormatter.format(java.util.Date(boleta.fecha))}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = if (expanded) "Ocultar productos" else "Ver productos")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Total: ${clpFormatter.format(boleta.montoTotal)}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)

                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Consumir el flow de productos para esta boleta
                            val productosFlow = viewModel.getProductosForBoleta(boleta.idBoleta)
                            val productos by productosFlow.collectAsStateWithLifecycle(initialValue = emptyList())

                            if (productos.isEmpty()) {
                                Text(text = "(Sin detalles de productos)", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    productos.forEach { p: ProductoDetalle ->
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = p.nombreZapato, style = MaterialTheme.typography.bodyMedium)
                                                Text(text = "Talla: ${p.talla} • Marca: ${p.marca}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                            }
                                            Text(text = "x${p.cantidad}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            } // Cierre de LazyColumn
        }
    }
}
