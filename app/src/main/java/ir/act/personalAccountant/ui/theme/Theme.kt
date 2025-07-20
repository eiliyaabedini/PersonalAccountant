package ir.act.personalAccountant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = YellowPrimary,
    secondary = GreenSecondary,
    tertiary = PurpleAccent,
    background = DarkBackground,
    surface = DarkCard,
    surfaceVariant = DarkCard,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCard,
    surfaceContainerHighest = DarkCard,
    surfaceContainerLow = DarkCard,
    surfaceContainerLowest = DarkCard,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = WhiteSection,
    onSurface = WhiteSection,
    onSurfaceVariant = WhiteSection,
    primaryContainer = WhiteSection,
    onPrimaryContainer = DarkBackground,
    secondaryContainer = DarkCard,
    onSecondaryContainer = WhiteSection,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = WhiteSection
)

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,
    secondary = GreenSecondary,
    tertiary = PurpleAccent,
    background = DarkBackground,
    surface = DarkCard,
    surfaceVariant = DarkCard,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCard,
    surfaceContainerHighest = DarkCard,
    surfaceContainerLow = DarkCard,
    surfaceContainerLowest = DarkCard,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = WhiteSection,
    onSurface = WhiteSection,
    onSurfaceVariant = WhiteSection,
    primaryContainer = WhiteSection,
    onPrimaryContainer = DarkBackground,
    secondaryContainer = DarkCard,
    onSecondaryContainer = WhiteSection,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = WhiteSection
)

@Composable
fun PersonalAccountantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to use our custom design
    content: @Composable () -> Unit
) {
    // Always use our custom dark color scheme for the black/white design
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}