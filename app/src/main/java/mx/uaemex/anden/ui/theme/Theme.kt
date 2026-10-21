package mx.uaemex.anden.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Institucional Movilidad del Estado de México / Tren Interurbano "El Insurgente" (Material Design 3)
val AndenBurgundyPrimary = Color(0xFF6B1D2F)
val AndenBurgundyDark = Color(0xFF4A101E)
val AndenGoldSecondary = Color(0xFFC59B27)
val AndenEmeraldValid = Color(0xFF1E7E34)
val AndenAmberWarning = Color(0xFFD97706)
val AndenDangerRed = Color(0xFFB91C1C)
val AndenSurfaceLight = Color(0xFFFBF8F6)
val AndenCardSurface = Color(0xFFFFFFFF)

private val AndenLightColorScheme = lightColorScheme(
    primary = AndenBurgundyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE8EC),
    onPrimaryContainer = AndenBurgundyDark,
    secondary = AndenGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF4D6),
    onSecondaryContainer = Color(0xFF423000),
    tertiary = AndenEmeraldValid,
    onTertiary = Color.White,
    background = AndenSurfaceLight,
    onBackground = Color(0xFF1F1A1C),
    surface = AndenCardSurface,
    onSurface = Color(0xFF1F1A1C)
)

@Composable
fun AndenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AndenLightColorScheme,
        content = content
    )
}
