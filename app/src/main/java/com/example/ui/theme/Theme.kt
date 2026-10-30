package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EffectCraftDarkColorScheme = darkColorScheme(
  primary = AccentCyan,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF004D59),
  onPrimaryContainer = Color(0xFF80F3FF),
  secondary = AccentAmber,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF573E00),
  onSecondaryContainer = Color(0xFFFFDF9E),
  tertiary = AccentMagenta,
  onTertiary = Color.White,
  background = StudioBackground,
  onBackground = TextPrimary,
  surface = StudioSurface,
  onSurface = TextPrimary,
  surfaceVariant = StudioSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = StudioSurfaceBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = EffectCraftDarkColorScheme,
    typography = Typography,
    content = content
  )
}
