package com.example.proyectoZapateria.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    // Nuevo esquema de colores morado/violeta claro - Material Design 3
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.15f),
                        colorScheme.primaryContainer.copy(alpha = 0.4f),
                        colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Bienvenido a StepStyle!",
                style = MaterialTheme.typography.headlineLarge,
                color = colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu tienda de calzado de confianza",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGoLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Iniciar Sesión", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoRegister,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Crear Cuenta", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

