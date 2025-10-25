package com.example.proyectoZapateria

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZapateriaApplication : Application() {
    // Hilt generará automáticamente el código necesario
    // para la inyección de dependencias
}

