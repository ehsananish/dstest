package ir.ehsananish.dstest.ds

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

enum class ThemeMode(val nameFa: String) { System("خودکار"), Light("روشن"), Dark("تیره") }

/** User preferences shared by all screens; changes recompose the whole app instantly. */
class AppSettings(context: Context, defaultStyle: DesignStyle = DesignStyle.Pastel) {
    private val prefs = context.getSharedPreferences("ds_settings", Context.MODE_PRIVATE)

    var style by mutableStateOf(enumOr(prefs.getString("style", null), defaultStyle))
        private set
    var palette by mutableStateOf(Palettes.byId(prefs.getString("palette", null), style))
        private set
    var themeMode by mutableStateOf(enumOr(prefs.getString("theme_mode", null), ThemeMode.System))
        private set
    var fontScale by mutableStateOf(enumOr(prefs.getString("font_scale", null), FontScale.Normal))
        private set
    var onboardingDone by mutableStateOf(prefs.getBoolean("onboarding_done", false))
        private set

    fun updateStyle(s: DesignStyle) {
        style = s
        if (palette !in Palettes.forStyle(s)) palette = Palettes.forStyle(s).first()
        prefs.edit().putString("style", s.name).putString("palette", palette.id).apply()
    }
    fun updatePalette(p: Palette) { palette = p; prefs.edit().putString("palette", p.id).apply() }
    fun updateThemeMode(m: ThemeMode) { themeMode = m; prefs.edit().putString("theme_mode", m.name).apply() }
    fun updateFontScale(s: FontScale) { fontScale = s; prefs.edit().putString("font_scale", s.name).apply() }
    fun updateOnboardingDone(done: Boolean) {
        onboardingDone = done
        prefs.edit().putBoolean("onboarding_done", done).apply()
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}

val LocalSettings = staticCompositionLocalOf<AppSettings> { error("AppSettings not provided") }
val LocalIsDark = staticCompositionLocalOf { false }
val LocalStyle = staticCompositionLocalOf { DesignStyle.Pastel }

@Composable
fun AppTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalSettings provides settings,
        LocalIsDark provides dark,
        LocalStyle provides settings.style,
    ) {
        val p = settings.palette
        val breeze = settings.style == DesignStyle.Breeze
        val scheme = when {
            breeze && dark -> breezeDark(p)
            breeze -> breezeLight(p)
            dark -> darkScheme(p)
            else -> lightScheme(p)
        }
        MaterialTheme(
            colorScheme = scheme,
            typography = appTypography(settings.fontScale.factor, settings.style),
            shapes = if (breeze) BreezeShapes else AppShapes,
        ) {
            // Text outside a Surface would otherwise default to black, unreadable in dark mode.
            CompositionLocalProvider(LocalContentColor provides scheme.onBackground, content = content)
        }
    }
}
