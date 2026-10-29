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
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("ds_settings", Context.MODE_PRIVATE)

    var palette by mutableStateOf(Palettes.byId(prefs.getString("palette", null)))
        private set
    var themeMode by mutableStateOf(enumOr(prefs.getString("theme_mode", null), ThemeMode.System))
        private set
    var fontScale by mutableStateOf(enumOr(prefs.getString("font_scale", null), FontScale.Normal))
        private set
    var onboardingDone by mutableStateOf(prefs.getBoolean("onboarding_done", false))
        private set

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
    ) {
        val scheme = if (dark) darkScheme(settings.palette) else lightScheme(settings.palette)
        MaterialTheme(colorScheme = scheme, typography = appTypography(settings.fontScale.factor), shapes = AppShapes) {
            // Text outside a Surface would otherwise default to black, unreadable in dark mode.
            CompositionLocalProvider(LocalContentColor provides scheme.onBackground, content = content)
        }
    }
}
