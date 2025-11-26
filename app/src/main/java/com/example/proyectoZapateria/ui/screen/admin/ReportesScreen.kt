package com.example.proyectoZapateria.ui.screen.admin

import android.content.Context
import android.os.Build
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectoZapateria.domain.model.ReporteVentas
import com.example.proyectoZapateria.viewmodel.admin.ReportesViewModel
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
                is com.example.proyectoZapateria.viewmodel.admin.ReportesUiState.Initial -> {
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

                is com.example.proyectoZapateria.viewmodel.admin.ReportesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is com.example.proyectoZapateria.viewmodel.admin.ReportesUiState.Success -> {
                    ReporteContent(
                        reporte = state.reporte,
                        onDescargar = {
                            descargarReporte(
                                context = context,
                                reporte = state.reporte,
                                anio = anioSeleccionado,
                                mes = mesSeleccionado
                            )
                        }
                    )
                }

                is com.example.proyectoZapateria.viewmodel.admin.ReportesUiState.Error -> {
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
    onDescargar: () -> Unit
) {
    val locale = Locale.Builder().setLanguage("es").setRegion("CL").build()
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
                }
            }
        }

        // Detalles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalles de Ventas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Lista de detalles
        items(reporte.detallesVentas) { detalle ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Boleta: ${detalle.numeroBoleta}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Cliente: ${detalle.nombreCliente}")
                    Spacer(modifier = Modifier.height(4.dp))
                    val fechaFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Text(text = "Fecha: ${fechaFmt.format(Date(detalle.fecha))}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Monto: ${numberFormat.format(detalle.montoTotal)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Estado: ${detalle.estado}")
                }
            }
        }

        // Acciones
        item {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onDescargar) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descargar CSV")
                }
            }
        }
    }
}

/**
 * Guarda un CSV simple en la carpeta Downloads y muestra un Toast con el resultado.
 * Se maneja Android Q+ mediante MediaStore.
 */
fun descargarReporte(
    context: Context,
    reporte: ReporteVentas,
    anio: Int,
    mes: Int?
) {
    val filename = "reporte_ventas_${anio}${mes?.let { "_" + it } ?: ""}.csv"
    val csvHeader = "numeroBoleta,fecha,nombreCliente,monto,estado\n"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val sb = StringBuilder()
    sb.append(csvHeader)
    for (d in reporte.detallesVentas) {
        val fecha = sdf.format(Date(d.fecha))
        val line = "\"${d.numeroBoleta}\",\"$fecha\",\"${d.nombreCliente.replace('"',' ')}\",${d.montoTotal},\"${d.estado}\"\n"
        sb.append(line)
    }

    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = android.provider.MediaStore.Downloads.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val uri = resolver.insert(collection, values)
            if (uri == null) {
                Toast.makeText(context, "No se pudo crear el archivo", Toast.LENGTH_LONG).show()
                return
            }

            resolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    writer.write(sb.toString())
                    writer.flush()
                }
            }

            values.clear()
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            Toast.makeText(context, "Reporte guardado en Descargas: $filename", Toast.LENGTH_LONG).show()
        } else {
            // Para API < Q: guardar en el directorio de archivos externos específico de la app (no requiere permisos)
            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (dir == null) {
                Toast.makeText(context, "No se pudo acceder a almacenamiento externo", Toast.LENGTH_LONG).show()
                return
            }
            val file = java.io.File(dir, filename)
            file.outputStream().use { os ->
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    writer.write(sb.toString())
                    writer.flush()
                }
            }
            Toast.makeText(context, "Reporte guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar reporte: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
