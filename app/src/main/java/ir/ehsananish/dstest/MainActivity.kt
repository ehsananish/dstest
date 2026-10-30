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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Switch
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import ir.ehsananish.dstest.ds.*
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
                    tabs = listOf(NavItem("خانه", Icons.Rounded.Home), NavItem("گفتگو", Icons.Rounded.Favorite)),
                    initialTab = initialTab,
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen()
                        else -> SocialScreen()
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
        ScreenHeader("گالری اجزا", subtitle = "کیت جدید سیستم دیزاین")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            HeroCard {
                Text("امروز", style = MaterialTheme.typography.titleMedium, color = LocalContentColor.current)
                HeroNumber(value = 12840, caption = "قدم تا هدف ۱۵٬۰۰۰ تا", unit = "قدم")
                Spacer(Modifier.height(Space.xs))
                PillButton("ثبت فعالیت", onClick = {}, icon = Icons.Rounded.Bolt, style = BtnStyle.Tonal)
            }
            SectionTitle("آواتارهای گرادیانی")
            PastelCard {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm), modifier = Modifier.fillMaxWidth()) {
                    listOf("علی", "مریم", "نان", "سبزی", "ورزش", "قرص", "کتاب", "سفر").forEachIndexed { i, n ->
                        GradientAvatar(n, pair = i, size = 36.dp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalAlignment = Alignment.CenterVertically) {
                    GradientAvatar("x", label = "🍎", size = 56.dp, pair = 1)
                    GradientAvatar("y", icon = Icons.Rounded.Favorite, size = 56.dp, pair = 6)
                    GradientAvatar("رضا", size = 56.dp)
                    IconTile(Icons.Rounded.Notifications, pair = 3)
                    IconTile(Icons.Rounded.Lock, colors = listOf(cs.primary))
                    IconTile(Icons.Rounded.Star, size = 48.dp, circle = true, pair = 5)
                }
            }
            SectionTitle("حلقه و نمودار")
            PastelCard {
                Text("این هفته نسبت به هفته‌ی قبل بهتر بوده است", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.md), verticalAlignment = Alignment.CenterVertically) {
                    RingMeter(0.72f, size = 112.dp) {
                        CountUpText(72, style = MaterialTheme.typography.headlineMedium, format = { it.fa() + "٪" })
                    }
                    RingMeter(0.4f, size = 112.dp, colors = listOf(GradientPairs.at(1).first, GradientPairs.at(6).second)) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = GradientPairs.at(6).first, modifier = Modifier.size(36.dp))
                    }
                }
                RoundedBarChart(
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج").zip(listOf(4f, 7f, 5f, 9f, 6f, 10f, 3f)).map { BarEntry(it.first, it.second) },
                    valueText = { it.toInt().fa() },
                )
            }
            SectionTitle("اشکال تزیینی")
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                Box(Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(Radius.lg)).background(cs.primaryContainer).blobBackground(cs.primary, seed = 5, alpha = 0.3f)) {
                    Text("بلاب", Modifier.padding(Space.md), style = MaterialTheme.typography.titleMedium)
                }
                Box(Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(Radius.lg)).background(cs.tertiaryContainer).dotsBackground(cs.tertiary, alpha = 0.5f)) {
                    Text("نقطه‌ها", Modifier.padding(Space.md), style = MaterialTheme.typography.titleMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                Box(Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(Radius.lg)).background(cs.surfaceContainerLowest)) {
                    Box(Modifier.fillMaxWidth().height(80.dp).waveHeader(Brush.linearGradient(listOf(cs.secondary, cs.tertiary)))) {
                        Text("موج", Modifier.padding(Space.md), style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
                Box(Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(Radius.lg)).background(cs.secondaryContainer).sparkleOverlay(cs.secondary, count = 7)) {
                    Text("درخشش", Modifier.padding(Space.md), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(Space.lg))
        }
    }
}

@Composable
private fun SocialScreen() {
    val cs = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") }
    var notif by rememberSaveable { mutableStateOf(true) }
    var burst by rememberSaveable { mutableIntStateOf(0) }
    var mood by rememberSaveable { mutableStateOf(MascotMood.Happy) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("گفتگو و تنظیمات", subtitle = "حباب، جستجو، گروه‌ها، شخصیت")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            PillSearchBar(query, { query = it }, placeholder = "جستجوی یادداشت‌ها")
            SettingsGroup(title = "حساب") {
                SettingsGroupRow(Icons.Rounded.Notifications, "یادآوری‌ها", subtitle = "هر روز ساعت ۸", pair = 0, trailing = {
                    Switch(checked = notif, onCheckedChange = { notif = it })
                })
                SettingsGroupRow(Icons.Rounded.Palette, "پوسته", value = "پاستیل", pair = 5, onClick = {})
                SettingsGroupRow(Icons.Rounded.Lock, "حریم خصوصی", pair = 4, onClick = {})
                SettingsGroupRow(Icons.Rounded.Settings, "پیشرفته", pair = 2, divider = false, onClick = {})
            }
            BlueSectionHeader("پیام‌ها")
            ChatBubble("سلام! امروز ۳ از ۵ هدف را انجام دادی. آفرین!", title = "مربی", time = "۱۴:۲۰")
            ChatBubble("یادآوری فردا ساعت ۸ باشد", mine = true, time = "۱۴:۲۱")
            ChatBubble("حتماً. فردا صبح خبرت می‌کنم.", time = "۱۴:۲۱")
            BlueSectionHeader("شخصیت و جشن")
            PastelCard {
                Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
                    MascotFrame(mood, size = 160.dp) { m, blink -> Blobby(m, blink) }
                    ConfettiBurst(burst, previewFrame = if (burst == 0) 0.45f else null)
                }
                PillSelectorRow(mood) { mood = it }
                PillButton("جشن بگیر", onClick = { burst++; mood = MascotMood.Celebrating }, icon = Icons.Rounded.AutoAwesome)
            }
            Spacer(Modifier.height(Space.lg))
        }
    }
}

@Composable
private fun PillSelectorRow(mood: MascotMood, onSelect: (MascotMood) -> Unit) {
    PillChoice(
        listOf(MascotMood.Idle to "عادی", MascotMood.Happy to "شاد", MascotMood.Sleepy to "خواب", MascotMood.Celebrating to "جشن"),
        mood, onSelect,
    )
}

/** Demo mascot drawn by the "app": a round body with a face that follows the mood. */
@Composable
private fun Blobby(mood: MascotMood, blink: Boolean) {
    val body = MaterialTheme.colorScheme.primary
    val ink = Color(0xFF2A1F2D)
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRoundRect(body, Offset(w * 0.08f, h * 0.15f), Size(w * 0.84f, h * 0.8f), androidx.compose.ui.geometry.CornerRadius(w * 0.4f))
        val eyeY = h * 0.46f
        val closed = blink || mood == MascotMood.Sleepy
        for (x in listOf(0.34f, 0.66f)) {
            if (closed) {
                drawArc(ink, 0f, 180f, false, Offset(w * x - w * 0.06f, eyeY - w * 0.04f), Size(w * 0.12f, w * 0.08f), style = Stroke(w * 0.035f, cap = StrokeCap.Round))
            } else {
                drawCircle(Color.White, w * 0.085f, Offset(w * x, eyeY))
                drawCircle(ink, w * 0.045f, Offset(w * x, eyeY + if (mood == MascotMood.Celebrating) -w * 0.01f else 0f))
            }
        }
        drawCircle(Color(0x55FF6B8B), w * 0.06f, Offset(w * 0.24f, h * 0.6f))
        drawCircle(Color(0x55FF6B8B), w * 0.06f, Offset(w * 0.76f, h * 0.6f))
        val mw = w * if (mood == MascotMood.Celebrating) 0.26f else 0.18f
        when (mood) {
            MascotMood.Celebrating -> drawArc(ink, 0f, 180f, true, Offset(w * 0.5f - mw / 2, h * 0.6f), Size(mw, mw * 0.8f))
            MascotMood.Sleepy -> drawCircle(ink, w * 0.025f, Offset(w * 0.5f, h * 0.68f))
            MascotMood.Happy -> drawArc(ink, 10f, 160f, false, Offset(w * 0.5f - mw / 2, h * 0.6f), Size(mw, mw * 0.7f), style = Stroke(w * 0.035f, cap = StrokeCap.Round))
            else -> drawLine(ink, Offset(w * 0.5f - mw / 3, h * 0.68f), Offset(w * 0.5f + mw / 3, h * 0.68f), w * 0.035f, StrokeCap.Round)
        }
    }
}
