package com.example.pagoflex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AzulCorporativo,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulMarino,
    secondary = VerdeEsmeralda,
    onSecondary = Color.White,
    secondaryContainer = VerdeSuave,
    onSecondaryContainer = VerdeOscuro,
    tertiary = Ambar,
    onTertiary = AzulMarino,
    tertiaryContainer = AmbarSuave,
    onTertiaryContainer = AmbarOscuro,
    error = Coral,
    onError = Color.White,
    errorContainer = CoralSuave,
    onErrorContainer = CoralOscuro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = TarjetaBlanca,
    onSurface = TextoOscuro,
    onSurfaceVariant = TextoGris,
    outline = TextoGris,
    outlineVariant = BordeGris
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    onPrimary = AzulMarino,
    primaryContainer = AzulCorporativo,
    onPrimaryContainer = AzulSuave,
    secondary = VerdeEsmeralda,
    onSecondary = AzulMarino,
    tertiary = Ambar,
    onTertiary = AzulMarino,
    error = Coral,
    onError = Color.White,
    background = FondoOscuro,
    onBackground = FondoClaro,
    surface = TarjetaOscura,
    onSurface = FondoClaro,
    onSurfaceVariant = BordeGris,
    outlineVariant = TextoGris
)

private val FormasPagoFlex = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun PagoFlexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AzulMarino.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FormasPagoFlex,
        content = content
    )
}
