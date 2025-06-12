package ir.act.personalAccountant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = YellowPrimary,
    secondary = GreenSecondary,
    tertiary = PurpleAccent,
    background = DarkBackground,
    surface = DarkCard,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = WhiteSection,
    onSurface = WhiteSection,
    primaryContainer = WhiteSection,
    onPrimaryContainer = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,
    secondary = GreenSecondary,
    tertiary = PurpleAccent,
    background = DarkBackground,
    surface = DarkCard,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = WhiteSection,
    onSurface = WhiteSection,
    primaryContainer = WhiteSection,
    onPrimaryContainer = DarkBackground
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