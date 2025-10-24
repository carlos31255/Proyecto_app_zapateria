package com.example.proyectoZapateria.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Esquema de colores OSCURO (para modo nocturno)
private val DarkColorScheme = darkColorScheme(
    // Colores principales
    primary = Purple200Dark,
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Purple300Dark,
    onPrimaryContainer = Color.White,

    // Colores secundarios
    secondary = Violet200Dark,
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Violet300,
    onSecondaryContainer = Color.White,

    // Colores terciarios
    tertiary = Pink300,
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Rose200,
    onTertiaryContainer = Color.White,

    // Backgrounds y surfaces
    background = BackgroundDark,
    onBackground = Color(0xFFE8E8E8),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCCCCCC),

    // Otros
    error = Error,
    onError = Color.White,
    outline = Color(0xFF6B6B6B),
    outlineVariant = Color(0xFF4A4A4A)
)

// Esquema de colores CLARO (tema principal - aspecto moderno y luminoso)
private val LightColorScheme = lightColorScheme(
    // Colores principales - Morado vibrante
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple200,
    onPrimaryContainer = Purple700,

    // Colores secundarios - Violeta complementario
    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Violet100,
    onSecondaryContainer = Purple700,

    // Colores terciarios - Acentos lavanda/rosa
    tertiary = Lavender400,
    onTertiary = Color.White,
    tertiaryContainer = Rose200,
    onTertiaryContainer = Purple700,

    // Backgrounds y surfaces - Muy claros y luminosos
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    // Colores de estado
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),

    // Bordes y divisores
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),

    // Otros elementos
    inverseSurface = Color(0xFF2D2640),
    inverseOnSurface = Color(0xFFF3F0F7),
    inversePrimary = Purple200Dark,
    scrim = Color(0xFF000000)
)

@Composable
fun Proyecto_zapateriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}