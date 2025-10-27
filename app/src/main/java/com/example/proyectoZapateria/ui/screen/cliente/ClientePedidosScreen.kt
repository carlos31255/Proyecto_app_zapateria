package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.cliente.ClientePedidosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
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
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)) {

        // Header con acción de refrescar
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Mis pedidos",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { viewModel.loadPedidos() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refrescar",
                    tint = colorScheme.primary
                )
            }
        }

        // Lista de pedidos
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
        }
    }
}
