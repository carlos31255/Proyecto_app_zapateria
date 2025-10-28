package com.example.proyectoZapateria.ui.screen.cliente

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
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
import com.example.proyectoZapateria.data.local.modelo.ModeloZapatoEntity
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

    val clpFormatter = remember { NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-CL")) }

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
            items(modelos) { modelo ->
                ClienteProductoCard(modelo = modelo, onClick = {
                    val rutaDetalle = Route.ClienteProductoDetail.path.replace("{idModelo}", modelo.idModelo.toString())
                    navController.navigate(rutaDetalle)
                }, colorScheme = colorScheme, context = context, priceFormatter = clpFormatter)
            }
        }
    }
}

@Composable
fun ClienteProductoCard(
    modelo: ModeloZapatoEntity,
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
                            Icon(Icons.Default.Image, contentDescription = null,
                                modifier = Modifier.size(48.dp).align(Alignment.Center), tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        }
                    }
                } else {
                    Icon(Icons.Default.Image, contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.Center), tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                }
            }
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
                Text(modelo.nombreModelo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                // Mostrar precio en CLP (entero)
                Text(priceFormatter.format(modelo.precioUnitario), color = colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
