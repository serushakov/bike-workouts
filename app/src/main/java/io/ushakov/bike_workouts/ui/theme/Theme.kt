package io.ushakov.bike_workouts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val DarkColorPalette = darkColors(
    primary = Blue700,
    primaryVariant = Blue900,
    secondary = Orange300
)

private val LightColorPalette = lightColors(
    primary = Blue800,
    primaryVariant = Blue900,
    secondary = Orange300,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun BikeWorkoutsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit,
) {
    val systemUi = rememberSystemUiController()

    if (darkTheme) {
        systemUi.setSystemBarsColor(DarkColorPalette.surface)
    } else {
        systemUi.setSystemBarsColor(LightColorPalette.surface)
    }

    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )

}