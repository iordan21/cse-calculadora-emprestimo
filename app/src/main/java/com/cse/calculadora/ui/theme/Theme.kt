package com.cse.calculadora.ui.theme

import android.app.Activity
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------
// CORES
// ---------------------------------------------------------------------

val AzulPrimario = Color(0xFF1B5FBF)
val AzulPrimarioEscuro = Color(0xFF0D3E85)
val VerdeAcento = Color(0xFF4CC98A)
val VermelhoAlerta = Color(0xFFD32F2F)
val VerdeSucesso = Color(0xFF2E7D32)
val CinzaFundo = Color(0xFFF5F5F5)

private val LightColors = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    secondary = AzulPrimarioEscuro,
    tertiary = VerdeAcento,
    background = CinzaFundo,
    surface = Color.White,
    error = VermelhoAlerta
)

private val DarkColors = darkColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    secondary = AzulPrimarioEscuro,
    tertiary = VerdeAcento,
    error = VermelhoAlerta
)

// ---------------------------------------------------------------------
// TIPOGRAFIA
// ---------------------------------------------------------------------

val CseTypography = androidx.compose.material3.Typography(
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

// ---------------------------------------------------------------------
// TEMA
// ---------------------------------------------------------------------

@Composable
fun CSETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // deixe true se quiser usar Material You (Android 12+)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CseTypography,
        content = content
    )
}
