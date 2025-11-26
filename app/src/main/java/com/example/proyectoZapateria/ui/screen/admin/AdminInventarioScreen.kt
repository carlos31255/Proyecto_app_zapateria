@file:Suppress("UNUSED_VARIABLE", "UNUSED_VALUE", "RedundantElvis", "UNUSED_PARAMETER")

package com.example.proyectoZapateria.ui.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.proyectoZapateria.data.remote.inventario.dto.MarcaDTO
import com.example.proyectoZapateria.data.remote.inventario.dto.ProductoDTO
import com.example.proyectoZapateria.navigation.Route
import com.example.proyectoZapateria.utils.ImageHelper
import com.example.proyectoZapateria.viewmodel.AuthViewModel
import com.example.proyectoZapateria.viewmodel.InventarioViewModel
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun AdminInventarioScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    inventarioViewModel: InventarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val productos by inventarioViewModel.productos.collectAsStateWithLifecycle()
    val marcas by inventarioViewModel.marcas.collectAsStateWithLifecycle()
    val isLoading by inventarioViewModel.isLoadingProductos.collectAsStateWithLifecycle()

    var productoSeleccionado by remember { mutableStateOf<ProductoDTO?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }

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
                                text = "Inventario",
                                color = colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${productos.size} productos",
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    IconButton(
                        onClick = { navController.navigate(Route.AdminAgregarProducto.path) }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar producto",
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Mostrar loader mientras se cargan los productos
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando inventario...", color = colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        if (productos.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay productos en el inventario",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate(Route.AdminAgregarProducto.path) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Producto")
                    }
                }
            }
        } else {
            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Ensure we pass a non-null key (items() expects a non-null Any key)
                items(productos, key = { it.id ?: 0L }) { producto ->
                    ProductoCard(
                        producto = producto,
                        nombreMarca = marcas.find { it.id == producto.marcaId }?.nombre ?: "Sin marca",
                        onEdit = {
                            productoSeleccionado = producto
                            mostrarDialogoEditar = true
                        },
                        onDelete = {
                            productoSeleccionado = producto
                            confirmarEliminar = false
                            mostrarDialogoEliminar = true
                        },
                        context = context,
                        colorScheme = colorScheme,
                        inventarioViewModel = inventarioViewModel // pasar viewModel aquí
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }

    // Diálogo de eliminar con confirmación doble
    if (mostrarDialogoEliminar && productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (!confirmarEliminar) "¿Eliminar producto?" else "¿Estás seguro?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        if (!confirmarEliminar) {
                            "Se eliminará el producto '${productoSeleccionado!!.nombre}'"
                        } else {
                            "Esta acción no se puede deshacer. Se eliminarán todos los datos del producto incluyendo su imagen."
                        }
                    )
                    if (confirmarEliminar) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Presiona 'Eliminar definitivamente' para confirmar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!confirmarEliminar) {
                            confirmarEliminar = true
                        } else {
                            inventarioViewModel.eliminarProducto(context, productoSeleccionado!!)
                            mostrarDialogoEliminar = false
                            productoSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text(if (!confirmarEliminar) "Eliminar" else "Eliminar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminar = false
                    confirmarEliminar = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de editar (completo con tallas y stock)
    if (mostrarDialogoEditar && productoSeleccionado != null) {
        EditarProductoCompletoDialog(
            producto = productoSeleccionado!!,
            marcas = marcas,
            viewModel = inventarioViewModel,
            context = context,
            onDismiss = {
                mostrarDialogoEditar = false
                productoSeleccionado = null
            }
        )
    }
}

@Composable
fun ProductoCard(
    producto: ProductoDTO,
    nombreMarca: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    context: android.content.Context,
    colorScheme: ColorScheme,
    inventarioViewModel: InventarioViewModel // new param
) {
    // solicitar carga de imagen si no está en cache (siempre intentarlo)
    val imagenes by inventarioViewModel.imagenes.collectAsState()

    LaunchedEffect(producto.id) {
        producto.id?.let { id ->
            // sólo pedir si no existe la clave en el mapa de imágenes (evita peticiones repetidas)
            if (!imagenes.containsKey(id)) {
                inventarioViewModel.loadImagenProducto(id)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceVariant)
            ) {
                val bytes = imagenes[producto.id]

                // 1) Si hay bytes en cache, mostrarlos (prioritario)
                if (bytes != null) {
                    val bmp = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Imagen remota",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                } else if (!producto.imagenUrl.isNullOrBlank()) {
                    // 2) Si imagenUrl apunta a un recurso local conocido
                    val drawableId = ImageHelper.getDrawableResourceId(context, producto.imagenUrl)
                    if (drawableId != null) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = drawableId),
                            contentDescription = "Imagen de ${producto.nombre}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 3) Si es un path de archivo local
                        val imageFile = ImageHelper.getFileFromPath(context, producto.imagenUrl)
                        if (imageFile.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(imageFile),
                                contentDescription = "Imagen de ${producto.imagenUrl}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 4) Si imagenUrl parece ser una URL absoluta (http/https), intentar cargarla con Coil
                            val url = producto.imagenUrl
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = "Imagen remota",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // 5) fallback: mostrar icono (se intentó cargar bytes en background)
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.Center),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                } else {
                    // 6) No hay URL ni bytes: mostrar icono
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Información del producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = nombreMarca,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mostrar precio en CLP
                val clpFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }
                Text(
                    text = clpFormatter.format(producto.precioUnitario),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (!producto.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = producto.descripcion ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botones de acción
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoCompletoDialog(
    producto: ProductoDTO,
    marcas: List<MarcaDTO>,
    viewModel: InventarioViewModel,
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var precio by remember { mutableStateOf(producto.precioUnitario.toString()) }
    var descripcion by remember { mutableStateOf(producto.descripcion ?: "") }
    var idMarcaSeleccionada by remember { mutableStateOf(producto.marcaId) }
    var expandedMarcas by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }

    // Estado de loading mientras se guardan los cambios
    var isGuardando by remember { mutableStateOf(false) }

    // Cargar inventario y tallas
    val tallas by viewModel.tallas.collectAsStateWithLifecycle()
    val inventario by viewModel.inventarioPorModelo.collectAsStateWithLifecycle()
    val loadingInventario by viewModel.isLoadingInventario.collectAsStateWithLifecycle()

    LaunchedEffect(producto.id) {
        producto.id?.let { viewModel.cargarInventarioDeModelo(it) }
    }

    // Map de idTalla a stock actual (editable)
    val stockPorTalla = remember { mutableStateMapOf<Long, String>() }

    // Inicializar con el inventario actual
    LaunchedEffect(inventario, tallas) {
        stockPorTalla.clear()
        // Para cada talla remota, mapear el stock si existe en inventario (InventarioUi usa tallaIdLocal:Int?)
        tallas.forEach { tallaDto ->
            val inv = inventario.find { it.tallaIdLocal == tallaDto.id }
            stockPorTalla[tallaDto.id] = inv?.stock?.toString() ?: "0"
        }
    }

    Dialog(onDismissRequest = if (isGuardando) { {} } else onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Si el inventario del modelo está cargando, mostrar loader central
                if (loadingInventario == producto.id) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cargando inventario...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    return@Box
                }

                LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                item {
                    Text(
                        text = "Editar Producto",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Campo nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            nombreError = if (it.isBlank()) "El nombre es requerido" else null
                        },
                        label = { Text("Nombre del modelo") },
                        isError = nombreError != null,
                        supportingText = nombreError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // Dropdown marca
                    ExposedDropdownMenuBox(
                        expanded = expandedMarcas,
                        onExpandedChange = { expandedMarcas = it }
                    ) {
                        OutlinedTextField(
                            value = marcas.find { it.id == idMarcaSeleccionada }?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Marca") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMarcas) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor() // corregido: no pasar MenuAnchorType
                        )

                        ExposedDropdownMenu(
                            expanded = expandedMarcas,
                            onDismissRequest = { expandedMarcas = false }
                        ) {
                            marcas.forEach { marca ->
                                DropdownMenuItem(
                                    text = { Text(marca.nombre) },
                                    onClick = {
                                        idMarcaSeleccionada = marca.id
                                        expandedMarcas = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // Campo precio
                    OutlinedTextField(
                        value = precio,
                        onValueChange = {
                            precio = it.filter { char -> char.isDigit() }
                            precioError = when {
                                precio.isBlank() -> "El precio es requerido"
                                precio.toIntOrNull() == null -> "Precio inválido"
                                precio.toInt() <= 0 -> "El precio debe ser mayor a 0"
                                else -> null
                            }
                        },
                        label = { Text("Precio") },
                        leadingIcon = { Text("$") },
                        isError = precioError != null,
                        supportingText = precioError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // Campo descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Título de inventario
                    Text(
                        text = "Gestión de Stock por Talla",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ingresa el stock disponible para cada talla. Deja en 0 para no tener esa talla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(tallas, key = { it.id }) { talla ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Talla ${talla.valor}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = stockPorTalla[talla.id] ?: "0",
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                stockPorTalla[talla.id] = filtered
                            },
                            label = { Text("Stock") },
                            modifier = Modifier.width(120.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                if (nombre.isNotBlank() &&
                                    precio.toIntOrNull() != null &&
                                    precio.toInt() > 0 &&
                                    !isGuardando) {

                                    // Activar estado de loading
                                    isGuardando = true

                                    android.util.Log.d("AdminInventario", "=== GUARDAR CAMBIOS ===")
                                    android.util.Log.d("AdminInventario", "Producto ID: ${producto.id}")
                                    android.util.Log.d("AdminInventario", "Tallas a guardar: ${stockPorTalla.size}")
                                    stockPorTalla.forEach { (tallaId, stock) ->
                                        android.util.Log.d("AdminInventario", "  - Talla ID=$tallaId, Stock=$stock")
                                    }

                                    // Verificar si cambiaron los datos del producto
                                    val nombreCambio = nombre.trim() != producto.nombre
                                    val precioCambio = precio.toInt() != producto.precioUnitario
                                    val descripcionCambio = descripcion.trim().ifBlank { null } != producto.descripcion
                                    val marcaCambio = idMarcaSeleccionada != producto.marcaId

                                    android.util.Log.d("AdminInventario", "Cambios detectados:")
                                    android.util.Log.d("AdminInventario", "  - Nombre: $nombreCambio (${producto.nombre} → ${nombre.trim()})")
                                    android.util.Log.d("AdminInventario", "  - Precio: $precioCambio (${producto.precioUnitario} → ${precio.toInt()})")
                                    android.util.Log.d("AdminInventario", "  - Descripción: $descripcionCambio")
                                    android.util.Log.d("AdminInventario", "  - Marca: $marcaCambio")

                                    val productoCambio = nombreCambio || precioCambio || descripcionCambio || marcaCambio

                                    if (productoCambio) {
                                        android.util.Log.d("AdminInventario", "✅ Producto cambió → Actualizar PRODUCTO + INVENTARIO")
                                        // Solo actualizar el producto si cambió
                                        viewModel.actualizarProducto(
                                            producto,
                                            nombre.trim(),
                                            precio.toInt(),
                                            descripcion.trim().ifBlank { null },
                                            idMarcaSeleccionada,
                                            onSuccess = {
                                                android.util.Log.d("AdminInventario", "Producto actualizado, ahora actualizando inventario...")
                                                // Luego actualizar inventario
                                                val inventarioMap = stockPorTalla.mapKeys { it.key }
                                                    .mapValues { (_, stock) -> stock.toIntOrNull() ?: 0 }

                                                producto.id?.let { idProd ->
                                                    viewModel.actualizarInventario(
                                                        idProd,
                                                        inventarioMap,
                                                        context,
                                                        onSuccess = {
                                                            isGuardando = false
                                                            onDismiss()
                                                        }
                                                    )
                                                } ?: run {
                                                    isGuardando = false
                                                    onDismiss()
                                                }
                                            }
                                        )
                                    } else {
                                        android.util.Log.d("AdminInventario", "✅ Solo tallas cambiaron → Actualizar SOLO INVENTARIO")
                                        // Solo actualizar inventario si no cambió el producto
                                        val inventarioMap = stockPorTalla.mapKeys { it.key }
                                            .mapValues { (_, stock) -> stock.toIntOrNull() ?: 0 }

                                        producto.id?.let { idProd ->
                                            viewModel.actualizarInventario(
                                                idProd,
                                                inventarioMap,
                                                context,
                                                onSuccess = {
                                                    isGuardando = false
                                                    onDismiss()
                                                }
                                            )
                                        } ?: run {
                                            isGuardando = false
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isGuardando &&
                                     nombre.isNotBlank() &&
                                     precio.toIntOrNull() != null &&
                                     precio.toInt() > 0
                        ) {
                            if (isGuardando) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardando...")
                                }
                            } else {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }

            // Overlay de loading cuando se está guardando
            if (isGuardando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Guardando cambios...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Por favor espera",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        }
    }
}
