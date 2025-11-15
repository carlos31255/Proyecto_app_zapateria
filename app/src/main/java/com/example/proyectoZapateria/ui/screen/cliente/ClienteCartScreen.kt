// TODO: Pantalla comentada temporalmente - ClienteCartViewModel usa entidades locales (CartItemEntity, etc.)
// Descomentar cuando se migre el carrito a usar microservicios
/*
package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.utils.ImageHelper
import com.example.proyectoZapateria.viewmodel.cliente.ClienteCartViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ClienteCartScreen(
    navController: NavHostController,
    viewModel: ClienteCartViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val clpFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))

    // Detectar cuando se completa el checkout con éxito para mostrar Toast y navegar
    LaunchedEffect(uiState.shouldNavigateToHome) {
        if (uiState.shouldNavigateToHome) {
            // Mostrar Toast de éxito
            Toast.makeText(context, "Compra finalizada correctamente", Toast.LENGTH_LONG).show()

            // Resetear la bandera
            viewModel.resetNavigationFlag()

            // Navegar al home del cliente
            navController.navigate(Route.ClienteHome.path) {
                popUpTo(Route.ClienteCart.path) { inclusive = true }
            }
        }
    }

    // Mostrar Toast de error cuando hay problemas (no de checkout)
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && !uiState.isLoading && !uiState.isCheckingOut) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
            return
        }
        // NO mostramos pantalla de error completa, solo el mensaje en la UI
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header con diseño
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                        text = "Carrito",
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.items.size} artículos",
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

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
                            // Imagen del producto
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (modelo?.imagenUrl != null) {
                                    // Primero intentar cargar desde drawable
                                    val drawableId = ImageHelper.getDrawableResourceId(context, modelo.imagenUrl)
                                    if (drawableId != null) {
                                        Image(
                                            painter = androidx.compose.ui.res.painterResource(id = drawableId),
                                            contentDescription = "Imagen de ${modelo.nombreModelo}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Si no está en drawable, buscar en archivos
                                        val imageFile = ImageHelper.getFileFromPath(context, modelo.imagenUrl)
                                        if (imageFile.exists()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(imageFile),
                                                contentDescription = "Imagen de ${modelo.nombreModelo}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            // Fallback: Nombre del producto
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Image,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = modelo.nombreModelo,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2,
                                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f,
                                                    color = colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Fallback: Nombre del producto sin imagen
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = modelo?.nombreModelo ?: "Producto",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = modelo?.nombreModelo ?: "#${itemUi.cartItem.idModelo}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
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
            // Mensajes de error destacados
            uiState.error?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Mensajes de checkout exitoso
            uiState.checkoutMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = msg,
                        color = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
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
}
*/
