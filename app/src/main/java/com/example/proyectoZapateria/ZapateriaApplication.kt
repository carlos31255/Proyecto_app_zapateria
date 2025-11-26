package com.example.proyectoZapateria

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

@HiltAndroidApp
class ZapateriaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Manejador global para capturar excepciones no capturadas y salvar el stacktrace
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {

                // Guardar en archivo interno para poder recuperarlo con adb
                try {
                    val crashFile = File(filesDir, "crash_log.txt")
                    FileOutputStream(crashFile, true).use { fos ->
                        PrintWriter(fos).use { pw ->
                            pw.println("--- Crash: ${System.currentTimeMillis()} Thread=${thread.name} ---")
                            throwable.printStackTrace(pw)
                            pw.println()
                        }
                    }
                } catch (e: Exception) {
                }
            } catch (ignored: Throwable) {
                // Evitar crash en el manejador
            } finally {
                // Delegar al handler por defecto (para conservar comportamiento de crash del sistema)
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
