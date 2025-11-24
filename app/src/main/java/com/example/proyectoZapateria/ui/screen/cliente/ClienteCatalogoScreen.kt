package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.di.NetworkModule
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.utils.ImageHelper
import com.example.proyectoZapateria.viewmodel.cliente.ClienteCatalogoViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteCatalogoScreen(
    navController: NavHostController,
    viewModel: ClienteCatalogoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val modelos by viewModel.modelos.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val clpFormatter = remember { NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-CL")) }

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
                        text = "Catálogo",
                        color = colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${modelos.size} productos disponibles",
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Mostrar animación de carga mientras se obtienen datos
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
            return
        }

        // No usar Scaffold local — el scaffold global maneja la TopAppBar y paddings
        if (modelos.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay productos disponibles")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(modelos) { producto ->
                    ClienteProductoCard(modelo = producto, onClick = {
                        android.util.Log.d("ClienteCatalogoVM", "Producto seleccionado: id=${producto.id}, nombre=${producto.nombre}")
                        val rutaDetalle = Route.ClienteProductoDetail.path.replace("{idModelo}", producto.id.toString())
                        navController.navigate(rutaDetalle)
                    }, colorScheme = colorScheme, context = context, priceFormatter = clpFormatter)
                }
            }
        }
    }
}

@Composable
fun ClienteProductoCard(
    modelo: ProductoDTO,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
    context: android.content.Context,
    priceFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .background(colorScheme.surfaceVariant)) {
                if (modelo.imagenUrl != null) {
                    // Primero intentar cargar desde drawable
                    val drawableId = ImageHelper.getDrawableResourceId(context, modelo.imagenUrl)
                    if (drawableId != null) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = drawableId),
                            contentDescription = "Imagen del producto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Si no está en drawable, buscar en archivos
                        val imageFile = ImageHelper.getFileFromPath(context, modelo.imagenUrl)
                        if (imageFile.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(imageFile),
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Intentar cargar desde endpoint remoto si existe (imagen blob o streaming)
                            val remoteUrl = NetworkModule.INVENTARIO_BASE_URL + "inventario/productos/${modelo.id}/imagen"
                            Image(
                                painter = rememberAsyncImagePainter(remoteUrl),
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // Si imagenUrl es null, intentar cargar desde el endpoint remoto también
                    val remoteUrl = NetworkModule.INVENTARIO_BASE_URL + "inventario/productos/${modelo.id}/imagen"
                    Image(
                        painter = rememberAsyncImagePainter(remoteUrl),
                        contentDescription = "Imagen del producto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
                Text(modelo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                // Mostrar precio en CLP (entero)
                Text(priceFormatter.format(modelo.precioUnitario), color = colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
