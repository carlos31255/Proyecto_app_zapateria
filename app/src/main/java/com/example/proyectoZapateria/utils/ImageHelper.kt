package com.example.proyectoZapateria.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// utilidad para trabajar con imágenes
object ImageHelper {

    private const val IMAGES_DIR = "product_images"

    // Archivo temporal para guardar la imagen capturada con la cámara
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Crear nombre único para el archivo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PRODUCT_${timeStamp}_"

        // Directorio donde se guardará la imagen
        val storageDir = File(context.filesDir, IMAGES_DIR)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Crear archivo temporal
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    // Obtiene la uri para el archivo temporal
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Obtiene el archivo desde una ubicacion guardada
    fun getFileFromPath(context: Context, path: String): File {
        return File(context.filesDir, path)
    }

    // Verifica si una imagen existe en el almacenamiento
    fun imageExists(context: Context, path: String): Boolean {
        val file = getFileFromPath(context, path)
        return file.exists()
    }

    // Elimina una imagen del almacenamiento
    fun deleteImage(context: Context, path: String): Boolean {
        return try {
            val file = getFileFromPath(context, path)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    // Obtiene la ruta relativa de la imagen para guardar en la base de datos
    fun getRelativePath(file: File, context: Context): String {
        val filesDir = context.filesDir.absolutePath
        return file.absolutePath.removePrefix(filesDir).removePrefix("/")
    }

    // Obtiene el tamaño del directorio de imágenes en MB
    fun getImagesDirSize(context: Context): Double {
        val dir = File(context.filesDir, IMAGES_DIR)
        var size: Long = 0

        if (dir.exists()) {
            dir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        }

        return size / (1024.0 * 1024.0) // Convertir a MB
    }

    // Limpia imágenes antiguas (opcional, para mantenimiento)
    fun cleanOldImages(context: Context, daysOld: Int = 30): Int {
        val dir = File(context.filesDir, IMAGES_DIR)
        var deletedCount = 0

        if (dir.exists()) {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)

            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
        }

        return deletedCount
    }

    // Obtiene el ID de un recurso drawable por su nombre (retorna null si no existe)
    fun getDrawableResourceId(context: Context, imageName: String?): Int? {
        if (imageName.isNullOrBlank()) return null
        val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (resourceId != 0) resourceId else null
    }
}

