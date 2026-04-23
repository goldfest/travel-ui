package com.travelguide.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

private val LightColorScheme = lightColorScheme(
    primary = ExplorerGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F3E3),
    onPrimaryContainer = ExplorerInk,
    secondary = ExplorerGreenSoft,
    onSecondary = ExplorerInk,
    secondaryContainer = Color(0xFFE5F8EC),
    onSecondaryContainer = ExplorerInk,
    tertiary = ExplorerOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8D5),
    onTertiaryContainer = Color(0xFF4A290C),
    background = ExplorerMist,
    onBackground = ExplorerInk,
    surface = ExplorerSurface,
    onSurface = ExplorerInk,
    surfaceVariant = Color(0xFFF7FBF7),
    onSurfaceVariant = ExplorerInkSoft,
    outline = ExplorerBorder,
    error = ExplorerError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = ExplorerGreenSoft,
    onPrimary = ExplorerForestDark,
    primaryContainer = Color(0xFF1D4E31),
    onPrimaryContainer = Color(0xFFDDF6E7),
    secondary = Color(0xFF99E4B6),
    onSecondary = ExplorerForestDark,
    secondaryContainer = Color(0xFF1A3A27),
    onSecondaryContainer = Color(0xFFDDF6E7),
    tertiary = Color(0xFFFFC98E),
    onTertiary = ExplorerForestDark,
    tertiaryContainer = Color(0xFF5A3A17),
    onTertiaryContainer = Color(0xFFFFE6CC),
    background = ExplorerForestDark,
    onBackground = Color(0xFFE8F1EA),
    surface = ExplorerForestSurface,
    onSurface = Color(0xFFE8F1EA),
    surfaceVariant = ExplorerForestMuted,
    onSurfaceVariant = Color(0xFFB9CABC),
    outline = Color(0xFF395344),
    error = Color(0xFFFF9D97),
    onError = ExplorerForestDark
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
