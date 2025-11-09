package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import androidx.navigation.NavHostController
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.viewmodel.cliente.ClienteProductoDetailViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteProductoDetailScreen(
    viewModel: ClienteProductoDetailViewModel = hiltViewModel(),
    authViewModel: com.example.proyectoZapateria.viewmodel.AuthViewModel = hiltViewModel(),
    navController: NavHostController? = null
) {
    var cantidadSeleccionada by remember { mutableStateOf(1) }
    var idInventarioSeleccionado by remember { mutableStateOf<Int?>(null) }

    val modelo by viewModel.modelo.collectAsStateWithLifecycle()
    val inventario by viewModel.inventario.collectAsStateWithLifecycle()
    val comprando by viewModel.comprando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()

    // Usuario actual
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Obtener el stock máximo de la talla seleccionada
    val stockMaximo = remember(idInventarioSeleccionado, inventario) {
        idInventarioSeleccionado?.let { idInv ->
            inventario.find { it.idInventario == idInv }?.stockActual ?: 0
        } ?: 0
    }

    // Resetear cantidad cuando se cambia de talla
    LaunchedEffect(idInventarioSeleccionado) {
        if (idInventarioSeleccionado != null) {
            cantidadSeleccionada = 1
        }
    }

    // Refrescar contador del carrito cuando la pantalla se abre o cambia la sesión
    LaunchedEffect(currentUser?.idPersona, modelo?.idModelo) {
        currentUser?.let { viewModel.refreshCartCount(it.idPersona) }
    }

    val clpFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }

    // No usar Scaffold local — el scaffold global maneja la TopAppBar y paddings
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

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
                    IconButton(onClick = { navController?.navigateUp() }) {
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
                        text = "Detalle del Producto",
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = modelo?.nombreModelo ?: "Cargando...",
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            Column(modifier = Modifier
                .fillMaxSize()) {

                if (modelo == null) {
                Text("Producto no encontrado")
                return@Column
            }

            Text(text = modelo!!.nombreModelo, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            // Mostrar precio en CLP
            Text(text = "Precio: ${clpFormatter.format(modelo!!.precioUnitario)}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Tallas disponibles:")
            Spacer(modifier = Modifier.height(8.dp))

            // Chips horizontales con tallas
            val tallasMap by viewModel.tallasMap.collectAsStateWithLifecycle()

            if (inventario.isEmpty()) {
                // Mostrar mensaje claro si no hay tallas/inventario
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("No hay tallas disponibles")
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (inv in inventario) {
                        val tallaLabel = tallasMap[inv.idTalla] ?: inv.idTalla.toString()
                        val enabled = inv.stockActual > 0
                        FilterChip(
                            selected = (idInventarioSeleccionado == inv.idInventario),
                            onClick = { if (enabled) idInventarioSeleccionado = inv.idInventario },
                            enabled = enabled,
                            label = { Text("$tallaLabel (${inv.stockActual})") },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cantidad:")
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { if (cantidadSeleccionada > 1) cantidadSeleccionada-- },
                    enabled = cantidadSeleccionada > 1
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Disminuir")
                }
                OutlinedTextField(
                    value = cantidadSeleccionada.toString(),
                    onValueChange = { v ->
                        val nuevaCantidad = v.filter { it.isDigit() }.toIntOrNull() ?: 1
                        // Limitar al stock máximo disponible
                        cantidadSeleccionada = when {
                            stockMaximo > 0 -> nuevaCantidad.coerceIn(1, stockMaximo)
                            else -> nuevaCantidad.coerceAtLeast(1)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.width(80.dp),
                    enabled = idInventarioSeleccionado != null && stockMaximo > 0
                )
                IconButton(
                    onClick = {
                        if (stockMaximo > 0 && cantidadSeleccionada < stockMaximo) {
                            cantidadSeleccionada++
                        } else if (stockMaximo > 0) {
                            Toast.makeText(
                                context,
                                "Stock máximo: $stockMaximo unidades",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = idInventarioSeleccionado != null && cantidadSeleccionada < stockMaximo
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Aumentar")
                }
            }

            // Mostrar stock disponible de la talla seleccionada
            if (idInventarioSeleccionado != null && stockMaximo > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stock disponible: $stockMaximo unidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stockMaximo <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón agregar al carrito
            val agregarEnabled = !comprando && idInventarioSeleccionado != null && currentUser != null && stockMaximo > 0
            Button(
                onClick = {
                    val usuarioId = currentUser?.idPersona ?: -1
                    val idInv = idInventarioSeleccionado
                    if (idInv == null) {
                        Toast.makeText(context, "Seleccione una talla", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (usuarioId == -1) {
                        Toast.makeText(context, "Inicie sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addToCart(idInv, cantidadSeleccionada, usuarioId)
                },
                enabled = agregarEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (comprando) "Agregando..." else "Agregar al carrito")
            }

            mensaje?.let { m ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = m, color = MaterialTheme.colorScheme.primary)
            }
            // Floating cart button
            Box(modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                if (cartCount > 0) {
                    FloatingActionButton(onClick = {
                        // Navegar al carrito
                        navController?.navigate(Route.ClienteCart.path)
                    }, modifier = Modifier.padding(16.dp)) {
                        BadgedBox(badge = {
                            Badge { Text(cartCount.toString()) }
                        }) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            }
            } // Cierre de Column interno
        } // Cierre de Box
    } // Cierre de Column principal
}
