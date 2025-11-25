package com.example.proyectoZapateria.ui.screen.admin

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoZapateria.utils.ImageHelper
import com.example.proyectoZapateria.viewmodel.ProductoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdminAgregarProductoScreen(
    navController: NavHostController,
    productoViewModel: ProductoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    val formState by productoViewModel.formState.collectAsStateWithLifecycle()
    val marcas by productoViewModel.marcas.collectAsStateWithLifecycle()
    val tallas by productoViewModel.tallas.collectAsStateWithLifecycle()

    // Estado para el archivo de la cámara
    var photoFile by remember { mutableStateOf<File?>(null) }

    // Permiso de cámara
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            productoViewModel.onImagenCapturada(context, photoFile!!)
            Toast.makeText(context, "Imagen capturada exitosamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No se pudo capturar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para abrir la cámara
    val abrirCamara = {
        when {
            cameraPermissionState.status.isGranted -> {
                try {
                    photoFile = ImageHelper.createImageFile(context)
                    val uri = ImageHelper.getUriForFile(context, photoFile!!)
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            cameraPermissionState.status.shouldShowRationale -> {
                Toast.makeText(
                    context,
                    "Se necesita permiso de cámara para capturar imágenes de productos",
                    Toast.LENGTH_LONG
                ).show()
                cameraPermissionState.launchPermissionRequest()
            }
            else -> {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // Mostrar mensaje de éxito
    LaunchedEffect(formState.success) {
        if (formState.success) {
            Toast.makeText(context, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show()
            productoViewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
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
                                text = "Agregar Producto",
                                color = colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nuevo zapato al catálogo",
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Sección de imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.imagenUri != null) {
                        // Mostrar imagen capturada
                        Image(
                            painter = rememberAsyncImagePainter(formState.imagenUri),
                            contentDescription = "Imagen del producto",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Botón para eliminar imagen
                        IconButton(
                            onClick = { productoViewModel.onRemoveImagen() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = colorScheme.error,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        colorScheme.surface.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        // Botón para capturar imagen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Tomar foto",
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = abrirCamara,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Capturar Imagen")
                            }
                        }
                    }
                }
            }

            if (formState.imagenError != null) {
                Text(
                    text = formState.imagenError!!,
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Nombre del modelo
            OutlinedTextField(
                value = formState.nombreModelo,
                onValueChange = { productoViewModel.onNombreChange(it) },
                label = { Text("Nombre del modelo") },
                placeholder = { Text("Ej: Air Max 270") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
                isError = formState.nombreError != null,
                supportingText = formState.nombreError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown: Marca
            var expandedMarcas by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedMarcas,
                onExpandedChange = { expandedMarcas = it }
            ) {
                OutlinedTextField(
                    value = marcas.find { it.id == formState.idMarcaSeleccionada }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marca") },
                    placeholder = { Text("Seleccione una marca") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMarcas) },
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                    isError = formState.marcaError != null,
                    supportingText = formState.marcaError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedMarcas,
                    onDismissRequest = { expandedMarcas = false }
                ) {
                    if (marcas.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay marcas disponibles") },
                            onClick = { }
                        )
                    } else {
                        marcas.forEach { marca ->
                            DropdownMenuItem(
                                text = { Text(marca.nombre) },
                                onClick = {
                                    productoViewModel.onMarcaSelected(marca.id)
                                    expandedMarcas = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Precio
            OutlinedTextField(
                value = formState.precio,
                onValueChange = { productoViewModel.onPrecioChange(it) },
                label = { Text("Precio") },
                placeholder = { Text("0") },
                leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium) },
                isError = formState.precioError != null,
                supportingText = formState.precioError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de Tallas y Stock
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            tint = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tallas Disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Selecciona las tallas e ingresa el stock para cada una",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (formState.tallasError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formState.tallasError!!,
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid de tallas
                    if (tallas.isEmpty()) {
                        Text(
                            text = "No hay tallas disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        tallas.chunked(4).forEach { rowTallas ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowTallas.forEach { talla ->
                                    val isSelected = formState.tallasSeleccionadas.containsKey(talla.id)
                                    val tallaConStock = formState.tallasSeleccionadas[talla.id]

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Chip de talla
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { productoViewModel.onTallaToggle(talla) },
                                            label = { Text(talla.valor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = colorScheme.primaryContainer,
                                                selectedLabelColor = colorScheme.onPrimaryContainer
                                            )
                                        )

                                        // Campo de stock para talla seleccionada
                                        if (isSelected && tallaConStock != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = tallaConStock.stock,
                                                onValueChange = {
                                                    productoViewModel.onStockTallaChange(talla.id, it)
                                                },
                                                placeholder = { Text("0", style = MaterialTheme.typography.bodySmall) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                isError = tallaConStock.stockError != null,
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = colorScheme.primary,
                                                    errorBorderColor = colorScheme.error
                                                )
                                            )
                                            if (tallaConStock.stockError != null) {
                                                Text(
                                                    text = tallaConStock.stockError,
                                                    color = colorScheme.error,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                // Rellenar espacios vacíos en la fila
                                repeat(4 - rowTallas.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Descripción
            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { productoViewModel.onDescripcionChange(it) },
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Detalles del producto...") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de error general
            if (formState.errorMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formState.errorMsg!!,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón guardar
            Button(
                onClick = { productoViewModel.guardarProducto(context) },
                enabled = formState.canSubmit && !formState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Guardar Producto",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Overlay loader que bloquea interacción mientras se sube
        if (formState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .clickable(enabled = true, onClick = {}) // intercepta clicks
                    .background(colorScheme.surface.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Subiendo imagen y guardando producto...", color = colorScheme.onSurface)
                }
            }
        }
    }
}
