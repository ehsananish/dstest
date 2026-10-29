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

/** Two visual styles. Pastel: soft, round, playful. Breeze (نسیم): flat, clean, messenger-like. */
enum class DesignStyle(val nameFa: String) { Pastel("پاستیل"), Breeze("نسیم") }

val BreezeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

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

    // Breeze palettes: one strong accent on neutral gray/white surfaces.
    private val bg = Color(0xFFF1F2F6)
    private val sv = Color(0xFFE9EBEF)
    private val ol = Color(0xFFD9DCE1)
    private val ink = Color(0xFF111418)
    private val ink2 = Color(0xFF6F757C)
    private val blue = Triple(Color(0xFF2A82DA), Color(0xFFDCEBFC), Color(0xFF0B3A6B))
    private val green = Triple(Color(0xFF2E9A50), Color(0xFFDAF2DF), Color(0xFF0B3A1A))
    private val violet = Triple(Color(0xFF7C5BE0), Color(0xFFECE4FD), Color(0xFF2D1566))
    private val orange = Triple(Color(0xFFD2660F), Color(0xFFFDE7D2), Color(0xFF4A2200))
    private val rose = Triple(Color(0xFFCC3F6E), Color(0xFFFCE0EA), Color(0xFF4F0D27))
    private val teal = Triple(Color(0xFF128C9C), Color(0xFFD5F1F5), Color(0xFF053B43))
    private fun breeze(id: String, name: String, a: Triple<Color, Color, Color>, b: Triple<Color, Color, Color>, c: Triple<Color, Color, Color>) =
        Palette(id, name, a.first, a.second, a.third, b.first, b.second, b.third, c.first, c.second, c.third, bg, sv, ol, ink, ink2)

    val breezeAll = listOf(
        breeze("b_blue", "آبی", blue, green, orange),
        breeze("b_green", "سبز", green, blue, violet),
        breeze("b_violet", "بنفش", violet, rose, blue),
        breeze("b_orange", "نارنجی", orange, blue, green),
        breeze("b_rose", "سرخابی", rose, violet, teal),
        breeze("b_teal", "فیروزه‌ای", teal, orange, violet),
    )

    fun forStyle(style: DesignStyle): List<Palette> = if (style == DesignStyle.Breeze) breezeAll else all
    fun byId(id: String?, style: DesignStyle = DesignStyle.Pastel): Palette =
        forStyle(style).let { list -> list.firstOrNull { it.id == id } ?: list.first() }
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

fun breezeLight(p: Palette): ColorScheme = lightScheme(p).copy(
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF6F7F9),
    surfaceContainerHigh = Color(0xFFEDEFF2),
    surfaceContainerHighest = Color(0xFFE4E7EB),
    outlineVariant = Color(0xFFE6E8EC),
)

fun breezeDark(p: Palette): ColorScheme {
    val night = Color(0xFF17212B)
    fun tint(c: Color, f: Float) = lerp(night, c, f)
    fun lift(c: Color) = lerp(c, Color.White, 0.12f)
    fun soft(c: Color) = lerp(c, Color.White, 0.3f)
    return darkColorScheme(
        primary = lift(p.primary), onPrimary = Color.White,
        primaryContainer = tint(p.primary, 0.35f), onPrimaryContainer = soft(p.primaryContainer),
        secondary = lift(p.secondary), onSecondary = Color.White,
        secondaryContainer = tint(p.secondary, 0.32f), onSecondaryContainer = soft(p.secondaryContainer),
        tertiary = lift(p.tertiary), onTertiary = Color.White,
        tertiaryContainer = tint(p.tertiary, 0.32f), onTertiaryContainer = soft(p.tertiaryContainer),
        background = Color(0xFF0E1621), onBackground = Color(0xFFF2F4F7),
        surface = Color(0xFF0E1621), onSurface = Color(0xFFF2F4F7),
        surfaceVariant = Color(0xFF242F3D), onSurfaceVariant = Color(0xFF8396AA),
        surfaceContainerLowest = night, surfaceContainerLow = night,
        surfaceContainer = Color(0xFF1D2733), surfaceContainerHigh = Color(0xFF242F3D),
        surfaceContainerHighest = Color(0xFF2C3948),
        outline = Color(0xFF34404E), outlineVariant = Color(0xFF26313E),
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
    Huge(1.3f, "درشت"),
}

fun appTypography(scale: Float, design: DesignStyle = DesignStyle.Pastel): Typography {
    val breeze = design == DesignStyle.Breeze
    fun style(size: Int, line: Int, weight: FontWeight): TextStyle {
        // Breeze headings are calmer: smaller and Bold instead of Black.
        val big = size >= 22
        val s = if (breeze && big) 0.86f else 1f
        val w = if (breeze && weight == FontWeight.Black) FontWeight.Bold else weight
        return TextStyle(
            fontFamily = Vazirmatn,
            fontWeight = w,
            fontSize = (size * scale * s).sp,
            lineHeight = (line * scale * s).sp,
        )
    }
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
