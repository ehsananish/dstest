package ir.ehsananish.dstest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.ehsananish.dstest.ds.AppInfo
import ir.ehsananish.dstest.ds.AppSettings
import ir.ehsananish.dstest.ds.AppShell
import ir.ehsananish.dstest.ds.AppTheme
import ir.ehsananish.dstest.ds.ConfettiBurst
import ir.ehsananish.dstest.ds.DesignStyle
import ir.ehsananish.dstest.ds.FontScale
import ir.ehsananish.dstest.ds.NavItem
import ir.ehsananish.dstest.ds.OnboardingPage
import ir.ehsananish.dstest.ds.SyncSystemBars

private val APP_INFO = AppInfo(
    name = "حال‌نامه",
    onboarding = listOf(
        OnboardingPage(Icons.Rounded.Mood, "حالت را در ده ثانیه بنویس", "هر شب یکی از پنج صورتک را بزن، چند برچسب بگذار و اگر خواستی یک خط بنویس."),
        OnboardingPage(Icons.Rounded.AutoGraph, "بفهم چه چیزی حالت را خوب می‌کند", "تقویم رنگی شمسی، نمودار روند و تحلیل برچسب‌ها نشانت می‌دهد ورزش، دوستان یا خواب چقدر روی حالت اثر دارند."),
        OnboardingPage(Icons.Rounded.Lock, "خصوصی و فقط برای خودت", "بدون حساب و بدون اینترنت. می‌توانی رمز بگذاری، یادآور شبانه روشن کنی و از دفترت فایل پشتیبان بگیری."),
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Lifestyle / wellbeing: the soft pastel style; users can switch in Settings.
        val settings = AppSettings(this, defaultStyle = DesignStyle.Pastel)
        // CI screenshots: --ez skip_onboarding true --ei open_tab N (-1 = settings), --ez alt_style true,
        // --ez big_font true, --ez demo_data true fills sixty sample days, --ez open_entry true opens today's sheet.
        if (intent.getBooleanExtra("skip_onboarding", false)) settings.updateOnboardingDone(true)
        if (savedInstanceState == null && intent.getBooleanExtra("alt_style", false)) {
            settings.updateStyle(if (settings.style == DesignStyle.Pastel) DesignStyle.Breeze else DesignStyle.Pastel)
        }
        if (savedInstanceState == null && intent.getBooleanExtra("big_font", false)) settings.updateFontScale(FontScale.entries.last())
        val initialTab = intent.getIntExtra("open_tab", 0)
        val openEntry = savedInstanceState == null && intent.getBooleanExtra("open_entry", false)
        val store = Store.get(this)
        if (intent.getBooleanExtra("demo_data", false)) store.seedDemo()
        setContent {
            AppTheme(settings) {
                SyncSystemBars(this)
                if (store.hasPin && !AppLock.unlocked) {
                    LockScreen(store)
                } else {
                    Box(Modifier.fillMaxSize()) {
                        AppShell(
                            info = APP_INFO,
                            tabs = listOf(
                                NavItem("امروز", Icons.Rounded.Home),
                                NavItem("تقویم", Icons.Rounded.CalendarMonth),
                                NavItem("بینش", Icons.Rounded.Insights),
                                NavItem("دفتر", Icons.Rounded.MenuBook),
                                NavItem("بیشتر", Icons.Rounded.MoreHoriz),
                            ),
                            initialTab = initialTab,
                        ) { tab ->
                            when (tab) {
                                0 -> HomeScreen(store, openEntry)
                                1 -> CalendarScreen(store)
                                2 -> InsightsScreen(store)
                                3 -> JournalScreen(store)
                                else -> MoreScreen(store)
                            }
                        }
                        ConfettiBurst(Party.trigger, Modifier.fillMaxSize())
                    }
                }
            }
        }
        Reminders.reschedule(this, store)
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) AppLock.unlocked = false
    }
}
