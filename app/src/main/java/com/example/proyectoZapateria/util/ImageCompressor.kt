package com.example.proyectoZapateria.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageCompressor {

    /**
     * Comprime una imagen a un tamaño máximo y calidad específica
     * @param maxWidth Ancho máximo (por defecto 1024px)
     * @param maxHeight Alto máximo (por defecto 1024px)
     * @param quality Calidad de compresión JPEG (0-100, por defecto 80)
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        try {
            // Leer la imagen original
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Obtener la orientación de la imagen (importante para fotos de cámara)
            val rotation = getImageRotation(context, imageUri)

            // Calcular el nuevo tamaño manteniendo la proporción
            val (newWidth, newHeight) = calculateNewSize(
                originalBitmap.width,
                originalBitmap.height,
                maxWidth,
                maxHeight
            )

            // Redimensionar la imagen
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // Rotar la imagen si es necesario
            val rotatedBitmap = if (rotation != 0) {
                rotateBitmap(scaledBitmap, rotation)
            } else {
                scaledBitmap
            }

            // Liberar memoria del bitmap original
            if (originalBitmap != scaledBitmap) {
                originalBitmap.recycle()
            }
            if (scaledBitmap != rotatedBitmap) {
                scaledBitmap.recycle()
            }

            // Guardar la imagen comprimida en un archivo temporal
            val compressedFile = File.createTempFile(
                "compressed_",
                ".jpg",
                context.cacheDir
            )

            FileOutputStream(compressedFile).use { outputStream ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Liberar memoria
            rotatedBitmap.recycle()

            return compressedFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Comprime una imagen desde un archivo
     */
    fun compressImageFromFile(
        context: Context,
        file: File,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        return compressImage(context, Uri.fromFile(file), maxWidth, maxHeight, quality)
    }

    /**
     * Calcula el nuevo tamaño manteniendo la proporción
     */
    private fun calculateNewSize(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        val ratio = originalWidth.toFloat() / originalHeight.toFloat()

        return if (originalWidth > originalHeight) {
            // Imagen horizontal
            val newWidth = minOf(originalWidth, maxWidth)
            val newHeight = (newWidth / ratio).toInt()
            Pair(newWidth, newHeight)
        } else {
            // Imagen vertical
            val newHeight = minOf(originalHeight, maxHeight)
            val newWidth = (newHeight * ratio).toInt()
            Pair(newWidth, newHeight)
        }
    }

    /**
     * Obtiene la rotación de la imagen desde los metadatos EXIF
     */
    private fun getImageRotation(context: Context, imageUri: Uri): Int {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * Rota un bitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

