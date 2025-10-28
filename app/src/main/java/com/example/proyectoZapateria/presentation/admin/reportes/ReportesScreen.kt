package com.example.proyectoZapateria.presentation.admin.reportes

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectoZapateria.domain.model.DetalleVentaReporte
import com.example.proyectoZapateria.domain.model.ReporteVentas
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReportesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val anioSeleccionado by viewModel.anioSeleccionado.collectAsState()
    val mesSeleccionado by viewModel.mesSeleccionado.collectAsState()

    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var showAnioDialog by remember { mutableStateOf(false) }
    var showMesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Reportes de Ventas",
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (mesSeleccionado != null)
                                "${viewModel.obtenerNombreMes(mesSeleccionado!!)} $anioSeleccionado"
                            else
                                "Año $anioSeleccionado",
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Filtros
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Selector de Año
                        OutlinedButton(
                            onClick = { showAnioDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Año: $anioSeleccionado")
                        }

                        // Selector de Mes
                        OutlinedButton(
                            onClick = { showMesDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (mesSeleccionado != null)
                                    viewModel.obtenerNombreMes(mesSeleccionado!!)
                                else
                                    "Todo el año"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.generarReporte() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar Reporte")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido del reporte
            when (val state = uiState) {
                is ReportesUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Selecciona los filtros y genera un reporte",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is ReportesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ReportesUiState.Success -> {
                    ReporteContent(
                        reporte = state.reporte,
                        mesSeleccionado = mesSeleccionado,
                        onDescargar = {
                            descargarReporte(
                                context = context,
                                reporte = state.reporte,
                                anio = anioSeleccionado,
                                mes = mesSeleccionado,
                                viewModel = viewModel
                            )
                        }
                    )
                }

                is ReportesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de selección de año
    if (showAnioDialog) {
        AlertDialog(
            onDismissRequest = { showAnioDialog = false },
            title = { Text("Seleccionar Año") },
            text = {
                LazyColumn {
                    items(viewModel.obtenerAniosDisponibles()) { anio ->
                        TextButton(
                            onClick = {
                                viewModel.seleccionarAnio(anio)
                                showAnioDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                anio.toString(),
                                style = if (anio == anioSeleccionado) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnioDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Diálogo de selección de mes
    if (showMesDialog) {
        AlertDialog(
            onDismissRequest = { showMesDialog = false },
            title = { Text("Seleccionar Mes") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                viewModel.seleccionarMes(null)
                                showMesDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Todo el año",
                                style = if (mesSeleccionado == null) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }
                            )
                        }
                    }
                    items((1..12).toList()) { mes ->
                        TextButton(
                            onClick = {
                                viewModel.seleccionarMes(mes)
                                showMesDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                viewModel.obtenerNombreMes(mes),
                                style = if (mes == mesSeleccionado) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMesDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun ReporteContent(
    reporte: ReporteVentas,
    mesSeleccionado: Int?,
    onDescargar: () -> Unit
) {
    val locale = java.util.Locale.Builder().setLanguage("es").setRegion("CL").build()
    val numberFormat = NumberFormat.getCurrencyInstance(locale)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Resumen
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                        Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Ventas Realizadas",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                reporte.numeroVentasRealizadas.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Ventas Canceladas",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                reporte.numeroVentasCanceladas.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Ingresos Totales",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        numberFormat.format(reporte.ingresosTotal),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Botón de descarga
        item {
            Button(
                onClick = onDescargar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descargar Reporte")
            }
        }

        // Detalles de ventas (solo si es filtro por mes)
        if (mesSeleccionado != null && reporte.detallesVentas.isNotEmpty()) {
            item {
                Text(
                    text = "Detalle de Ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(reporte.detallesVentas) { detalle ->
                DetalleVentaCard(detalle)
            }
        }
    }
}

@Composable
fun DetalleVentaCard(detalle: DetalleVentaReporte) {
    val locale = java.util.Locale.Builder().setLanguage("es").setRegion("CL").build()
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (detalle.estado == "cancelada") {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detalle.numeroBoleta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = if (detalle.estado == "cancelada") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = detalle.estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cliente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = detalle.nombreCliente,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(detalle.fecha)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Monto Total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = numberFormat.format(detalle.montoTotal),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (detalle.estado == "cancelada") {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

private fun descargarReporte(
    context: Context,
    reporte: ReporteVentas,
    anio: Int,
    mes: Int?,
    viewModel: ReportesViewModel
) {
    try {
        val fileName = if (mes != null) {
            "reporte_${viewModel.obtenerNombreMes(mes)}_$anio.txt"
        } else {
            "reporte_$anio.txt"
        }

        // Usar MediaStore API para Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            descargarConMediaStore(context, reporte, fileName, anio, mes, viewModel)
        } else {
            // Fallback para Android 9 y anteriores
            descargarTradicional(context, reporte, fileName, anio, mes, viewModel)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Error al descargar reporte: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

// MediaStore API - Android 10+ (API 29+)
@androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
private fun descargarConMediaStore(
    context: Context,
    reporte: ReporteVentas,
    fileName: String,
    anio: Int,
    mes: Int?,
    viewModel: ReportesViewModel
) {
    try {
        // Configurar metadatos del archivo
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1) // Marca como pendiente mientras se escribe
        }

        // Insertar archivo en MediaStore
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { fileUri ->
            // Escribir contenido del reporte con encoding UTF-8
            resolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    escribirContenidoReporte(writer, reporte, anio, mes, viewModel)
                }
            }

            // Marcar archivo como completado
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(fileUri, contentValues, null, null)

            // Mostrar mensaje de éxito
            Toast.makeText(
                context,
                "Reporte descargado en Downloads: $fileName",
                Toast.LENGTH_LONG
            ).show()

            // Abrir el archivo
            abrirArchivo(context, fileUri)
        } ?: run {
            Toast.makeText(
                context,
                "Error al crear el archivo de reporte",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Error al descargar: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

// Método tradicional - Android 9 y anteriores
private fun descargarTradicional(
    context: Context,
    reporte: ReporteVentas,
    fileName: String,
    anio: Int,
    mes: Int?,
    viewModel: ReportesViewModel
) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Crear directorio si no existe
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        // Usar OutputStreamWriter con UTF-8 en lugar de FileWriter
        file.outputStream().use { outputStream ->
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                escribirContenidoReporte(writer, reporte, anio, mes, viewModel)
            }
        }

        // Obtener URI con FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        Toast.makeText(
            context,
            "Reporte descargado en Downloads: $fileName",
            Toast.LENGTH_LONG
        ).show()

        // Abrir el archivo
        abrirArchivo(context, uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Error al descargar: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

// Función auxiliar para escribir el contenido del reporte
private fun escribirContenidoReporte(
    writer: java.io.Writer,
    reporte: ReporteVentas,
    anio: Int,
    mes: Int?,
    viewModel: ReportesViewModel
) {
    val locale = java.util.Locale.Builder().setLanguage("es").setRegion("CL").build()
    val numberFormat = NumberFormat.getCurrencyInstance(locale)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    writer.write("===========================================\n")
    writer.write("     REPORTE DE VENTAS - ZAPATERIA\n")
    writer.write("===========================================\n\n")

    if (mes != null) {
        writer.write("Periodo: ${viewModel.obtenerNombreMes(mes)} $anio\n\n")
    } else {
        writer.write("Periodo: Año $anio\n\n")
    }

    writer.write("-------------------------------------------\n")
    writer.write("RESUMEN\n")
    writer.write("-------------------------------------------\n\n")
    writer.write("Ventas Realizadas:    ${reporte.numeroVentasRealizadas}\n")
    writer.write("Ventas Canceladas:    ${reporte.numeroVentasCanceladas}\n")
    writer.write("Ingresos Totales:     ${numberFormat.format(reporte.ingresosTotal)}\n\n")

    if (reporte.detallesVentas.isNotEmpty()) {
        writer.write("-------------------------------------------\n")
        writer.write("DETALLE DE VENTAS\n")
        writer.write("-------------------------------------------\n\n")

        reporte.detallesVentas.forEach { detalle ->
            writer.write("Boleta: ${detalle.numeroBoleta}\n")
            writer.write("Estado: ${detalle.estado.uppercase()}\n")
            writer.write("Cliente: ${detalle.nombreCliente}\n")
            writer.write("Fecha: ${dateFormat.format(Date(detalle.fecha))}\n")
            writer.write("Monto: ${numberFormat.format(detalle.montoTotal)}\n\n")
        }
    }

    writer.write("===========================================\n")
    writer.write("Reporte generado el ${dateFormat.format(Date())}\n")
    writer.write("===========================================\n")
    writer.flush()
}

// Función para abrir el archivo descargado
private fun abrirArchivo(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Verificar si hay una aplicación que pueda abrir el archivo
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Abrir reporte"))
        } else {
            Toast.makeText(
                context,
                "No hay aplicacion para abrir archivos de texto",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "No se pudo abrir el archivo",
            Toast.LENGTH_SHORT
        ).show()
    }
}

