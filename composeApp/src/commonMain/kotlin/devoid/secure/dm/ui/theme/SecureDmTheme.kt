package devoid.secure.dm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import devoid.secure.dm.data.datastore.SettingsSource
import devoid.secure.dm.domain.settings.SettingsConfig
import org.koin.compose.koinInject

private val DarkColorPalette = darkColorScheme(primary = Color(0xFFd1cecb), primaryContainer = Color(0xFF0b0c0d))

private val LightColorPalette = lightColorScheme(

)

fun ColorScheme.isLight() = this.background.luminance() > 0.5

@Composable
fun ColorScheme.backgroundBrush(): Brush {
    return if (isLight()) BackgroundBrushLight() else BackgroundBrushDark()
}

@Composable
fun ColorScheme.secondaryBackgroundBrush(): Brush {
    return if (isLight()) SecondaryBrushLight() else SecondaryBrushDark()
}

val ColorScheme.backgroundMiddleColor: Color
    get() = if (isLight()) BrushMiddleColorLight else BrushMiddleColorDark

val ColorScheme.blurColor
    get() = if (isLight()) BlurColourLight else BlurColourDark

@Composable
fun SecureDmTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val settingsSource = koinInject<SettingsSource>()
    val settingsConfig by settingsSource.getSettingsConfig().collectAsState(SettingsConfig())
    val colors =
        if ((settingsConfig.themePreference == ThemePreference.SYSTEM && isDarkTheme) || settingsConfig.themePreference == ThemePreference.DARK) {
            DarkColorPalette
        } else {
            LightColorPalette
        }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
