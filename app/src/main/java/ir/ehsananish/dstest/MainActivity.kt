package ir.ehsananish.dstest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.ehsananish.dstest.ds.AppInfo
import ir.ehsananish.dstest.ds.AppSettings
import ir.ehsananish.dstest.ds.AppShell
import ir.ehsananish.dstest.ds.AppTheme
import ir.ehsananish.dstest.ds.BtnStyle
import ir.ehsananish.dstest.ds.DesignStyle
import ir.ehsananish.dstest.ds.EmptyState
import ir.ehsananish.dstest.ds.NavItem
import ir.ehsananish.dstest.ds.OnboardingPage
import ir.ehsananish.dstest.ds.PastelCard
import ir.ehsananish.dstest.ds.PillButton
import ir.ehsananish.dstest.ds.ScreenHeader
import ir.ehsananish.dstest.ds.SectionTitle
import ir.ehsananish.dstest.ds.SettingsRow
import ir.ehsananish.dstest.ds.Space
import ir.ehsananish.dstest.ds.StatTile
import ir.ehsananish.dstest.ds.SyncSystemBars
import ir.ehsananish.dstest.ds.fa

// Replace the demo screens below with the real app. Keep AppTheme + AppShell + SyncSystemBars:
// they provide onboarding, settings (colors, dark mode, text size) and contact us.
private val APP_INFO = AppInfo(
    name = "پاستیل",
    onboarding = listOf(
        OnboardingPage(Icons.Rounded.AutoAwesome, "خوش آمدی!", "اینجا یک جمله‌ی کوتاه درباره‌ی کاری که برنامه برایت انجام می‌دهد."),
        OnboardingPage(Icons.Rounded.Bolt, "سریع و ساده", "همه‌چیز روی گوشی خودت ذخیره می‌شود و به اینترنت نیازی نیست."),
        OnboardingPage(Icons.Rounded.Palette, "به سلیقه‌ی خودت", "از تنظیمات، رنگ، حالت تیره و اندازه‌ی متن را عوض کن."),
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Default look for this app: DesignStyle.Pastel or DesignStyle.Breeze. Users can switch in Settings.
        val settings = AppSettings(this, defaultStyle = DesignStyle.Pastel)
        // Used by the screenshot step on CI: --ez skip_onboarding true --ei open_tab N (-1 = settings),
        // --ez alt_style true switches to the other design style.
        if (intent.getBooleanExtra("skip_onboarding", false)) settings.updateOnboardingDone(true)
        if (intent.getBooleanExtra("alt_style", false)) {
            settings.updateStyle(if (settings.style == DesignStyle.Pastel) DesignStyle.Breeze else DesignStyle.Pastel)
        }
        val initialTab = intent.getIntExtra("open_tab", 0)
        setContent {
            AppTheme(settings) {
                SyncSystemBars(this)
                AppShell(
                    info = APP_INFO,
                    tabs = listOf(NavItem("خانه", Icons.Rounded.Home), NavItem("علاقه‌مندی", Icons.Rounded.Favorite)),
                    initialTab = initialTab,
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen()
                        else -> EmptyState(Icons.Rounded.Inbox, "هنوز چیزی نیست", "وقتی چیزی اضافه کنی اینجا نشان داده می‌شود.", actionText = "افزودن")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("پاستیل", subtitle = "نمونه‌ی سیستم دیزاین پاستلی")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                StatTile("امروز", 12.fa(), Icons.Rounded.Bolt, Modifier.weight(1f))
                StatTile("امتیاز", 340.fa(), Icons.Rounded.Star, Modifier.weight(1f), container = cs.tertiaryContainer)
            }
            PastelCard(color = cs.secondaryContainer) {
                Text("کارت رنگی", style = MaterialTheme.typography.titleLarge)
                Text("کارت‌ها گوشه‌های گرد و رنگ ملایم دارند و بدون سایه روی پس‌زمینه می‌نشینند.", style = MaterialTheme.typography.bodyMedium)
                PillButton("دکمه‌ی اصلی", onClick = {}, icon = Icons.Rounded.AutoAwesome)
            }
            SectionTitle("فهرست", action = "همه")
            PastelCard {
                SettingsRow(Icons.Rounded.Favorite, "یک ردیف فهرست", subtitle = "با آیکن و توضیح", onClick = {})
                SettingsRow(Icons.Rounded.Palette, "ردیف دیگر", subtitle = "رنگ آیکن قابل تغییر است", container = cs.tertiaryContainer, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm), modifier = Modifier.fillMaxWidth()) {
                PillButton("ثانویه", onClick = {}, style = BtnStyle.Tonal, modifier = Modifier.weight(1f))
                PillButton("خطی", onClick = {}, style = BtnStyle.Outlined, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(Space.lg))
        }
    }
}
