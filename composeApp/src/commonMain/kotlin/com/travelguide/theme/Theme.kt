package com.travelguide.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ExplorerGreen = Color(0xFF219653)
val ExplorerGreenSoft = Color(0xFF6FCF97)
val ExplorerOrange = Color(0xFFF2994A)
val ExplorerMist = Color(0xFFF2F7F4)
val ExplorerSurface = Color(0xFFFFFFFF)
val ExplorerInk = Color(0xFF162319)
val ExplorerInkSoft = Color(0xFF5F6F63)
val ExplorerBorder = Color(0xFFD8E7DB)
val ExplorerError = Color(0xFFEB5757)
val ExplorerForestDark = Color(0xFF101A14)
val ExplorerForestSurface = Color(0xFF16241B)
val ExplorerForestMuted = Color(0xFF223529)
val ExplorerMapLine = Color(0xFF2D9B63)
val ExplorerMapLineAccent = Color(0xFF9BE7B7)

// Общая палитра нового стиля: dark travel / photo-first.
val TravelDark = Color(0xFF0B0B0C)
val TravelPanel = Color(0xFF1A1A1B)
val TravelPanelSoft = Color(0xFF222225)
val TravelTextPrimary = Color(0xFFFFFFFF)
val TravelTextSecondary = Color(0xFFA7A7AD)
val TravelAccent = Color(0xFF5BE58C)
val TravelAccentSoft = Color(0xFFB8F7C9)
val TravelAccentDeep = Color(0xFF1F8F4D)
val TravelGlow = Color(0x335BE58C)
val TravelGlass = Color(0xCC202124)
val TravelSuccess = Color(0xFF6FCF97)
val TravelDanger = Color(0xFFFF9D97)
val TravelScrim = Color(0x99000000)

private val LightColorScheme = lightColorScheme(
    primary = TravelAccent,
    onPrimary = TravelDark,
    primaryContainer = TravelPanelSoft,
    onPrimaryContainer = TravelTextPrimary,
    secondary = TravelSuccess,
    onSecondary = TravelDark,
    secondaryContainer = TravelPanelSoft,
    onSecondaryContainer = TravelTextPrimary,
    tertiary = ExplorerOrange,
    onTertiary = TravelDark,
    tertiaryContainer = Color(0xFF3A2A18),
    onTertiaryContainer = Color(0xFFFFE6CC),
    background = TravelDark,
    onBackground = TravelTextPrimary,
    surface = TravelPanel,
    onSurface = TravelTextPrimary,
    surfaceVariant = TravelPanelSoft,
    onSurfaceVariant = TravelTextSecondary,
    outline = Color(0xFF2E2E32),
    error = TravelDanger,
    onError = TravelDark
)

private val DarkColorScheme = darkColorScheme(
    primary = TravelAccent,
    onPrimary = TravelDark,
    primaryContainer = TravelPanelSoft,
    onPrimaryContainer = TravelTextPrimary,
    secondary = TravelSuccess,
    onSecondary = TravelDark,
    secondaryContainer = TravelPanelSoft,
    onSecondaryContainer = TravelTextPrimary,
    tertiary = Color(0xFFFFC98E),
    onTertiary = TravelDark,
    tertiaryContainer = Color(0xFF3A2A18),
    onTertiaryContainer = Color(0xFFFFE6CC),
    background = TravelDark,
    onBackground = TravelTextPrimary,
    surface = TravelPanel,
    onSurface = TravelTextPrimary,
    surfaceVariant = TravelPanelSoft,
    onSurfaceVariant = TravelTextSecondary,
    outline = Color(0xFF2E2E32),
    error = TravelDanger,
    onError = TravelDark
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
