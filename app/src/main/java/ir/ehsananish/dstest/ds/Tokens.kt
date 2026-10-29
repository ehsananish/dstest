package ir.ehsananish.dstest.ds

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsananish.dstest.R

/*
 * "Pastel" design system shared by every factory app.
 * Soft tonal surfaces, big rounded shapes, pill buttons, bold Vazirmatn headings.
 * Use these tokens instead of hard-coded sizes and colors.
 */

object Radius {
    val xs = 8.dp
    val sm = 12.dp
    val md = 18.dp
    val lg = 26.dp
    val xl = 34.dp
}

object Space {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Stroke {
    val hairline = 1.dp
    val bold = 2.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl),
)

/** One color theme. Users pick between these in Settings. */
data class Palette(
    val id: String,
    val nameFa: String,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
)

object Palettes {
    val Lilac = Palette(
        "lilac", "یاسی",
        Color(0xFF6750C8), Color(0xFFE9E0FF), Color(0xFF22125E),
        Color(0xFFB0517A), Color(0xFFFFD9E6), Color(0xFF3E0C24),
        Color(0xFF2D7DA0), Color(0xFFCDEBFA), Color(0xFF062B3D),
        Color(0xFFFBF8FF), Color(0xFFEFEAF7), Color(0xFFCFC7DE), Color(0xFF1F1B2A), Color(0xFF625C70),
    )
    val Peach = Palette(
        "peach", "هلویی",
        Color(0xFFC4532F), Color(0xFFFFDCCF), Color(0xFF3D1406),
        Color(0xFF6B5B95), Color(0xFFE6DEFF), Color(0xFF23184A),
        Color(0xFF2F7A6B), Color(0xFFCDEFE5), Color(0xFF06302A),
        Color(0xFFFFF8F4), Color(0xFFF6EAE4), Color(0xFFD9C6BD), Color(0xFF2A1F1B), Color(0xFF6E5E57),
    )
    val Mint = Palette(
        "mint", "نعنایی",
        Color(0xFF23806A), Color(0xFFC7F1E2), Color(0xFF00291F),
        Color(0xFFC05A3E), Color(0xFFFFDCCF), Color(0xFF3D1406),
        Color(0xFF6A5BC4), Color(0xFFE5E0FF), Color(0xFF1E1452),
        Color(0xFFF5FCF8), Color(0xFFE4F1EB), Color(0xFFBFD6CC), Color(0xFF17241F), Color(0xFF55665F),
    )
    val Sky = Palette(
        "sky", "آسمانی",
        Color(0xFF2B6CC4), Color(0xFFD6E6FF), Color(0xFF041C3F),
        Color(0xFFC2477F), Color(0xFFFFD8E8), Color(0xFF3F0722),
        Color(0xFF3D8B4F), Color(0xFFD3F2D6), Color(0xFF062C10),
        Color(0xFFF6F9FF), Color(0xFFE6EDF8), Color(0xFFC3CFDF), Color(0xFF18202C), Color(0xFF56627A),
    )
    val Lemon = Palette(
        "lemon", "لیمویی",
        Color(0xFF8A6A00), Color(0xFFFFEDA8), Color(0xFF2B2000),
        Color(0xFF3F7C8C), Color(0xFFCDEEF6), Color(0xFF052A33),
        Color(0xFFB5536B), Color(0xFFFFD9E0), Color(0xFF3E0717),
        Color(0xFFFFFBF0), Color(0xFFF4EEDC), Color(0xFFD9CFB0), Color(0xFF24200F), Color(0xFF675F45),
    )
    val Rose = Palette(
        "rose", "صورتی",
        Color(0xFFB83E6A), Color(0xFFFFD9E5), Color(0xFF3E0420),
        Color(0xFF6B5BC4), Color(0xFFE5E0FF), Color(0xFF1E1452),
        Color(0xFF2E8079), Color(0xFFC9F0EB), Color(0xFF002A26),
        Color(0xFFFFF7F9), Color(0xFFF6E7EC), Color(0xFFDCC3CC), Color(0xFF2A1A20), Color(0xFF6F5A62),
    )

    val all = listOf(Lilac, Peach, Mint, Sky, Lemon, Rose)
    fun byId(id: String?): Palette = all.firstOrNull { it.id == id } ?: all.first()
}

fun lightScheme(p: Palette): ColorScheme = lightColorScheme(
    primary = p.primary, onPrimary = Color.White,
    primaryContainer = p.primaryContainer, onPrimaryContainer = p.onPrimaryContainer,
    secondary = p.secondary, onSecondary = Color.White,
    secondaryContainer = p.secondaryContainer, onSecondaryContainer = p.onSecondaryContainer,
    tertiary = p.tertiary, onTertiary = Color.White,
    tertiaryContainer = p.tertiaryContainer, onTertiaryContainer = p.onTertiaryContainer,
    background = p.background, onBackground = p.onSurface,
    surface = p.background, onSurface = p.onSurface,
    surfaceVariant = p.surfaceVariant, onSurfaceVariant = p.onSurfaceVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = lerp(Color.White, p.surfaceVariant, 0.35f),
    surfaceContainer = lerp(Color.White, p.surfaceVariant, 0.6f),
    surfaceContainerHigh = p.surfaceVariant,
    surfaceContainerHighest = lerp(p.surfaceVariant, p.outline, 0.3f),
    outline = p.outline, outlineVariant = lerp(p.outline, Color.White, 0.4f),
)

fun darkScheme(p: Palette): ColorScheme {
    val base = Color(0xFF141217)
    fun tint(c: Color, f: Float) = lerp(base, c, f)
    return darkColorScheme(
        primary = p.primaryContainer, onPrimary = p.onPrimaryContainer,
        primaryContainer = tint(p.primary, 0.42f), onPrimaryContainer = p.primaryContainer,
        secondary = p.secondaryContainer, onSecondary = p.onSecondaryContainer,
        secondaryContainer = tint(p.secondary, 0.38f), onSecondaryContainer = p.secondaryContainer,
        tertiary = p.tertiaryContainer, onTertiary = p.onTertiaryContainer,
        tertiaryContainer = tint(p.tertiary, 0.38f), onTertiaryContainer = p.tertiaryContainer,
        background = tint(p.primary, 0.05f), onBackground = Color(0xFFEDE7EE),
        surface = tint(p.primary, 0.05f), onSurface = Color(0xFFEDE7EE),
        surfaceVariant = tint(p.primary, 0.16f), onSurfaceVariant = Color(0xFFC4BCC6),
        surfaceContainerLowest = tint(p.primary, 0.03f),
        surfaceContainerLow = tint(p.primary, 0.08f),
        surfaceContainer = tint(p.primary, 0.11f),
        surfaceContainerHigh = tint(p.primary, 0.15f),
        surfaceContainerHighest = tint(p.primary, 0.19f),
        outline = Color(0xFF5A535C), outlineVariant = Color(0xFF3A353C),
    )
}

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black),
)

enum class FontScale(val factor: Float, val nameFa: String) {
    Small(0.9f, "کوچک"),
    Normal(1f, "معمولی"),
    Large(1.15f, "بزرگ"),
    Huge(1.3f, "خیلی بزرگ"),
}

fun appTypography(scale: Float): Typography {
    fun style(size: Int, line: Int, weight: FontWeight) = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = weight,
        fontSize = (size * scale).sp,
        lineHeight = (line * scale).sp,
    )
    return Typography(
        displayLarge = style(48, 64, FontWeight.Black),
        displayMedium = style(40, 54, FontWeight.Black),
        displaySmall = style(34, 46, FontWeight.Black),
        headlineLarge = style(30, 42, FontWeight.Black),
        headlineMedium = style(26, 38, FontWeight.Bold),
        headlineSmall = style(22, 34, FontWeight.Bold),
        titleLarge = style(20, 32, FontWeight.Bold),
        titleMedium = style(17, 28, FontWeight.Bold),
        titleSmall = style(15, 24, FontWeight.Medium),
        bodyLarge = style(16, 28, FontWeight.Normal),
        bodyMedium = style(14, 25, FontWeight.Normal),
        bodySmall = style(12, 21, FontWeight.Normal),
        labelLarge = style(15, 22, FontWeight.Bold),
        labelMedium = style(13, 20, FontWeight.Medium),
        labelSmall = style(11, 17, FontWeight.Medium),
    )
}

private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** Converts ASCII digits to Persian digits for display. */
fun String.fa(): String = map { if (it in '0'..'9') faDigits[it - '0'] else it }.joinToString("")
fun Int.fa(): String = toString().fa()
fun Long.fa(): String = toString().fa()
