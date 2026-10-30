package ir.ehsananish.dstest.ds

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
    /** Optional app artwork (e.g. the mascot) drawn in the big tile instead of [icon]. */
    val art: (@Composable BoxScope.() -> Unit)? = null,
)

/** Lets an app screen switch AppShell's bottom tab, e.g. LocalSelectTab.current(0). */
val LocalSelectTab = staticCompositionLocalOf<(Int) -> Unit> { {} }

/** What the shared screens need to know about the app. */
data class AppInfo(
    val name: String,
    val onboarding: List<OnboardingPage>,
    val supportEmail: String = AppConfig.SUPPORT_EMAIL,
    val telegram: String = AppConfig.TELEGRAM,
    /** Shown from Settings > Privacy. Override if the app ever handles data differently. */
    val privacy: String = DEFAULT_PRIVACY,
)

const val DEFAULT_PRIVACY =
    "این برنامه بدون ثبت‌نام کار می‌کند و هیچ اطلاعات شخصی‌ای از تو جمع‌آوری، ارسال یا با کسی به اشتراک گذاشته نمی‌شود. " +
        "همهٔ داده‌هایی که در برنامه وارد می‌کنی فقط روی همین گوشی ذخیره می‌شوند و با حذف برنامه پاک می‌شوند. " +
        "برنامه تبلیغ ندارد و برای کار کردن به اینترنت نیاز ندارد. " +
        "اگر از بخش «ارتباط با ما» پیامی بفرستی، فقط همان پیام از راهی که خودت انتخاب می‌کنی (ایمیل یا تلگرام) به دست ما می‌رسد."

/** Makes status/navigation bar icons readable in the app's (not the system's) light/dark mode. */
@Composable
fun SyncSystemBars(activity: ComponentActivity) {
    val dark = LocalIsDark.current
    // Breeze draws a colored bar under the status bar once past onboarding, so it needs light icons.
    val lightStatusIcons = dark || (isBreeze() && LocalSettings.current.onboardingDone)
    DisposableEffect(dark, lightStatusIcons) {
        val t = android.graphics.Color.TRANSPARENT
        val status = if (lightStatusIcons) SystemBarStyle.dark(t) else SystemBarStyle.light(t, t)
        val nav = if (dark) SystemBarStyle.dark(t) else SystemBarStyle.light(t, t)
        activity.enableEdgeToEdge(status, nav)
        onDispose {}
    }
}

/**
 * App frame: onboarding on first launch, then the app's tabs plus a Settings tab
 * (colors, dark mode, text size, contact us, replay onboarding) in a floating pill bar.
 * initialTab = -1 opens Settings.
 */
@Composable
fun AppShell(
    info: AppInfo,
    tabs: List<NavItem>,
    initialTab: Int = 0,
    content: @Composable (tab: Int) -> Unit,
) {
    val settings = LocalSettings.current
    if (!settings.onboardingDone) {
        OnboardingScreen(info) { settings.updateOnboardingDone(true) }
        return
    }
    val allTabs = tabs + NavItem("تنظیمات", Icons.Rounded.Settings)
    val settingsIndex = allTabs.lastIndex
    var tab by rememberSaveable { mutableIntStateOf(if (initialTab < 0) settingsIndex else initialTab.coerceAtMost(settingsIndex)) }
    var showContact by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showContact) { showContact = false }
    BackHandler(enabled = !showContact && tab != 0) { tab = 0 }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PastelBackdrop(Modifier.fillMaxWidth().height(320.dp))
        if (isBreeze()) Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(headerBarColor()))
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = if (showContact) -1 else tab,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                    label = "screen",
                ) { t ->
                    when (t) {
                        -1 -> ContactScreen(info, onBack = { showContact = false })
                        settingsIndex -> SettingsScreen(info, onContact = { showContact = true })
                        else -> CompositionLocalProvider(LocalSelectTab provides { tab = it.coerceIn(0, settingsIndex) }) { content(t) }
                    }
                }
            }
            if (!showContact) PillBottomBar(allTabs, tab, { tab = it })
        }
    }
}

@Composable
fun OnboardingScreen(info: AppInfo, onDone: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val pages = info.onboarding
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val settings = LocalSettings.current
    val hasVoice = rememberHasVoiceGuide()
    if (hasVoice) OnboardingVoice(pager.currentPage, settings.voiceGuide)
    val breeze = isBreeze()
    val containers = if (breeze) listOf(cs.primary, cs.secondary, cs.tertiary)
    else listOf(cs.primaryContainer, cs.tertiaryContainer, cs.secondaryContainer)
    Box(Modifier.fillMaxSize().background(cs.background)) {
        PastelBackdrop(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(Space.lg)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(info.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (hasVoice) {
                    CircleIconButton(
                        if (settings.voiceGuide) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        onClick = { settings.updateVoiceGuide(!settings.voiceGuide) },
                        contentDescription = if (settings.voiceGuide) "قطع صدای راهنما" else "پخش صدای راهنما",
                    )
                }
                Text(
                    "رد شدن",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onDone)
                        .padding(horizontal = Space.md, vertical = Space.xs),
                )
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
                val p = pages[i]
                val container = containers[i % containers.size]
                Column(
                    Modifier.fillMaxSize().padding(horizontal = Space.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.size(if (breeze) 160.dp else 200.dp)
                            .clip(if (breeze) CircleShape else RoundedCornerShape(Radius.xl * 2)).background(container),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (p.art != null) p.art.invoke(this)
                        else Icon(p.icon, null, tint = contentColorFor(container), modifier = Modifier.size(if (breeze) 80.dp else 96.dp))
                    }
                    Spacer(Modifier.height(Space.xl))
                    Text(p.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(Space.sm))
                    Text(p.body, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    pages.indices.forEach { i ->
                        val on = i == pager.currentPage
                        val w by animateDpAsState(if (on) 28.dp else 10.dp, label = "dot")
                        Box(Modifier.height(10.dp).width(w).clip(CircleShape).background(if (on) cs.primary else cs.outlineVariant))
                    }
                }
                val last = pager.currentPage == pages.lastIndex
                PillButton(
                    if (last) "شروع کنیم" else "بعدی",
                    onClick = {
                        if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    },
                    icon = if (last) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(info: AppInfo, onContact: () -> Unit) {
    val s = LocalSettings.current
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            icon = { Icon(Icons.Rounded.Lock, null) },
            title = { Text("حریم خصوصی") },
            text = { Text(info.privacy, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("متوجه شدم") } },
        )
    }
    val version = remember {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: ""
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("تنظیمات", subtitle = "ظاهر برنامه را به سلیقه‌ی خودت تغییر بده")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            PastelCard {
                Text("سبک طراحی", style = MaterialTheme.typography.titleMedium)
                PillChoice(DesignStyle.entries.map { it to it.nameFa }, s.style, s::updateStyle)
                Text(
                    if (s.style == DesignStyle.Breeze) "ساده، تخت و مرتب؛ مثل یک پیام‌رسان." else "نرم، گرد و رنگی؛ شاد و دوست‌داشتنی.",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
            }
            PastelCard {
                Text("رنگ برنامه", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                ) {
                    Palettes.forStyle(s.style).forEach { p ->
                        val on = p.id == s.palette.id
                        Column(
                            Modifier.clip(RoundedCornerShape(Radius.md)).clickable { s.updatePalette(p) }.padding(Space.xxs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Space.xxs),
                        ) {
                            Box(
                                Modifier.size(52.dp).clip(CircleShape).background(p.primaryContainer)
                                    .then(if (on) Modifier.border(3.dp, p.primary, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (on) Icon(Icons.Rounded.Check, null, tint = p.primary)
                                else Box(Modifier.size(20.dp).clip(CircleShape).background(p.primary))
                            }
                            Text(p.nameFa, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            PastelCard {
                Text("حالت نمایش", style = MaterialTheme.typography.titleMedium)
                PillChoice(ThemeMode.entries.map { it to it.nameFa }, s.themeMode, s::updateThemeMode)
            }
            PastelCard {
                Text("اندازه‌ی متن", style = MaterialTheme.typography.titleMedium)
                PillChoice(FontScale.entries.map { it to it.nameFa }, s.fontScale, s::updateFontScale)
                Text("این یک متن نمونه است تا اندازه را ببینی.", style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
            }
            PastelCard {
                SettingsRow(Icons.Rounded.Forum, "ارتباط با ما", subtitle = "پیشنهاد، مشکل یا سؤال؟ برایمان بنویس", onClick = onContact)
                if (rememberHasVoiceGuide()) {
                    SettingsRow(
                        Icons.AutoMirrored.Rounded.VolumeUp, "راهنمای صوتی",
                        subtitle = "پخش توضیح صوتی در راهنمای شروع",
                        container = cs.secondaryContainer,
                        onClick = { s.updateVoiceGuide(!s.voiceGuide) },
                        trailing = { Switch(checked = s.voiceGuide, onCheckedChange = { s.updateVoiceGuide(it) }) },
                    )
                }
                SettingsRow(
                    Icons.Rounded.AutoAwesome, "دیدن دوباره‌ی راهنما",
                    container = cs.tertiaryContainer, onClick = { s.updateOnboardingDone(false) },
                )
                SettingsRow(
                    Icons.Rounded.Lock, "حریم خصوصی", subtitle = "اطلاعاتت فقط روی گوشی خودت می‌ماند",
                    container = cs.secondaryContainer, onClick = { showPrivacy = true },
                )
                SettingsRow(Icons.Rounded.Info, info.name, subtitle = "نسخه‌ی " + version.fa(), container = cs.primaryContainer)
            }
            Spacer(Modifier.height(Space.lg))
        }
    }
}

@Composable
fun ContactScreen(info: AppInfo, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var message by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        ScreenHeader(
            "ارتباط با ما",
            subtitle = "هر نظری داری خوشحال می‌شویم بشنویم. همه‌ی پیام‌ها را می‌خوانیم.",
            onBack = onBack,
        )
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            if (info.supportEmail.isNotBlank()) {
                PastelCard(color = cs.primaryContainer) {
                    Text("پیامت را بنویس", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                        placeholder = { Text("مثلاً: کاش این امکان را هم داشت که…") },
                        shape = RoundedCornerShape(Radius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cs.surfaceContainerLowest,
                            unfocusedContainerColor = cs.surfaceContainerLowest,
                        ),
                    )
                    PillButton(
                        "ارسال با ایمیل", onClick = { sendEmail(ctx, info, message) },
                        icon = Icons.Rounded.Email, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (info.telegram.isNotBlank()) {
                PastelCard {
                    Text(
                        "ایده، انتقاد یا پیشنهاد داری؟ در تلگرام برایمان بنویس؛ هر بهبودی که بخواهی را می‌خوانیم و روی بهتر شدن برنامه کار می‌کنیم.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                    )
                    SettingsRow(
                        Icons.Rounded.Forum, "گفت‌وگو در تلگرام", subtitle = "@" + info.telegram,
                        onClick = { openUrl(ctx, "https://t.me/" + info.telegram) },
                    )
                }
            }
            if (info.supportEmail.isNotBlank()) {
                PastelCard {
                    SettingsRow(
                        Icons.Rounded.Email, "ایمیل پشتیبانی", subtitle = info.supportEmail,
                        container = cs.tertiaryContainer, onClick = { sendEmail(ctx, info, "") },
                    )
                }
            }
            if (info.supportEmail.isBlank() && info.telegram.isBlank()) {
                EmptyState(Icons.Rounded.Forum, "به‌زودی", "راه‌های ارتباطی به‌زودی اضافه می‌شوند.")
            }
            MoreAppsSection(ctx)
        }
    }
}

@Composable
private fun MoreAppsSection(ctx: Context) {
    val apps = MoreApps.list
    if (apps.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    SectionTitle("اپ‌های دیگر ما")
    PastelCard {
        apps.forEach { app ->
            val res = ctx.resources.getIdentifier(app.iconName, "drawable", ctx.packageName)
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .clickable { openApp(ctx, app.pkg) }
                    .padding(vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.md),
            ) {
                if (res != 0) {
                    Image(
                        painterResource(res), contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(app.name, style = MaterialTheme.typography.titleSmall)
                    Text(app.blurb, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2)
                }
            }
        }
    }
}

private fun openApp(ctx: Context, pkg: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("bazaar://details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        openUrl(ctx, "https://cafebazaar.ir/app/$pkg")
    }
}

private fun sendEmail(ctx: Context, info: AppInfo, body: String) {
    val subject = "[${ctx.packageName}] ${info.name}"
    val text = body + "\n\n---\n${info.name} | Android ${Build.VERSION.RELEASE} | ${Build.MODEL}"
    val uri = Uri.parse("mailto:${info.supportEmail}?subject=${Uri.encode(subject)}&body=${Uri.encode(text)}")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(info.supportEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        ctx.startActivity(Intent.createChooser(intent, "ارسال ایمیل").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, "برنامه‌ی ایمیل پیدا نشد", Toast.LENGTH_SHORT).show()
    }
}

private fun openUrl(ctx: Context, url: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, "باز نشد", Toast.LENGTH_SHORT).show()
    }
}
