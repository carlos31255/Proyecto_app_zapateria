package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.viewmodel.cliente.ClienteCartViewModel
import java.text.NumberFormat
import java.util.Locale
import com.example.proyectoZapateria.navigation.Route

@Composable
fun ClienteCartScreen(
    navController: NavHostController,
    viewModel: ClienteCartViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val clpFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))

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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "Carrito", style = MaterialTheme.typography.titleLarge, color = colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
            return
        }

        LazyColumn(modifier = Modifier
            .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.items) { itemUi ->
                val modelo = itemUi.modelo
                Card(
                    onClick = {
                        // Navegar al detalle del modelo si está disponible
                        val idModelo = itemUi.cartItem.idModelo
                        navController.navigate(Route.ClienteProductoDetail.path.replace("{idModelo}", idModelo.toString()))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Imagen si existe, fallback a recurso local
                            if (modelo?.imagenUrl != null) {
                                // En este proyecto no cargamos imágenes remotas por defecto; usar placeholder
                                Image(
                                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                    contentDescription = modelo.nombreModelo,
                                    modifier = Modifier.size(64.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                    contentDescription = modelo?.nombreModelo ?: "Producto",
                                    modifier = Modifier.size(64.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(text = modelo?.nombreModelo ?: "#${itemUi.cartItem.idModelo}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Talla: ${itemUi.cartItem.talla}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Precio: ${clpFormatter.format((modelo?.precioUnitario ?: itemUi.cartItem.precioUnitario).toDouble())}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Controles y subtotal
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.decrementQuantity(itemUi) }) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Quitar")
                                }
                                Text(text = "${itemUi.cartItem.cantidad}")
                                IconButton(onClick = { viewModel.incrementQuantity(itemUi) }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = "Subtotal: ${clpFormatter.format(((modelo?.precioUnitario ?: itemUi.cartItem.precioUnitario) * itemUi.cartItem.cantidad).toDouble())}", style = MaterialTheme.typography.bodySmall, color = colorScheme.primary)

                            IconButton(onClick = { viewModel.removeItem(itemUi) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Total y acciones
        Column(modifier = Modifier.fillMaxWidth()) {
            // Mensajes de checkout/errores
            uiState.checkoutMessage?.let { msg ->
                Text(text = msg, color = colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
            }
            uiState.error?.let { err ->
                Text(text = err, color = colorScheme.error)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(text = "Total: ${clpFormatter.format(uiState.total.toDouble())}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Vaciar carrito")
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (uiState.isCheckingOut) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), color = colorScheme.primary)
                } else {
                    Button(onClick = { viewModel.checkout() }) {
                        Text("Pagar ${clpFormatter.format(uiState.total.toDouble())}")
                    }
                }
            }
        }
    }
}
