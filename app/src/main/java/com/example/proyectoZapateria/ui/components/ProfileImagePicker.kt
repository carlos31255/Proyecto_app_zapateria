package com.example.proyectoZapateria.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.example.proyectoZapateria.utils.ImageHelper
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileImagePicker(
    imageUri: Uri?,
    onImageSelected: (Context, File) -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 100,
    isEditing: Boolean = false
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var showDialog by remember { mutableStateOf(false) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    // Permisos
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            onImageSelected(context, photoFile!!)
            Toast.makeText(context, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No se pudo capturar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Copiar el archivo de la galería a un archivo temporal
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = ImageHelper.createImageFile(context)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onImageSelected(context, tempFile)
                Toast.makeText(context, "Imagen seleccionada exitosamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
                    showDialog = false
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            cameraPermissionState.status.shouldShowRationale -> {
                Toast.makeText(
                    context,
                    "Se necesita permiso de cámara para tomar fotos",
                    Toast.LENGTH_LONG
                ).show()
                cameraPermissionState.launchPermissionRequest()
            }
            else -> {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // Función para abrir la galería
    val abrirGaleria = {
        galleryLauncher.launch("image/*")
        showDialog = false
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(colorScheme.primary)
            .clickable(enabled = isEditing) {
                if (isEditing) showDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }

        // Indicador de edición
        if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar foto",
                tint = Color.White,
                modifier = Modifier.size((size * 0.4).dp)
            )
        }
    }

    // Diálogo para elegir entre cámara o galería
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cambiar foto de perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Cámara
                    Button(
                        onClick = abrirCamara,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar foto")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Galería
                    OutlinedButton(
                        onClick = abrirGaleria,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elegir de galería")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Cancelar
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

