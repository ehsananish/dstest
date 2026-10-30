package ir.ehsananish.dstest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.ehsananish.dstest.ds.*

private fun moodWord(avg: Double) = MOOD_NAMES[(Math.round(avg).toInt().coerceIn(1, 5)) - 1]

// ───────────────────────────── Home ─────────────────────────────

@Composable
fun HomeScreen(store: Store, openEntry: Boolean) {
    val today = store.today()
    val list = store.entries
    val todayEntry = remember(list, today) { list.firstOrNull { it.day == today } }
    var sheetDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var sheetMood by rememberSaveable { mutableIntStateOf(0) }
    var auto by rememberSaveable { mutableStateOf(openEntry) }
    LaunchedEffect(auto) { if (auto) { auto = false; sheetDay = today; sheetMood = 0 } }
    val streak = remember(list, today) { Logic.streak(list.map { it.day }.toSet(), today) }
    val cs = MaterialTheme.colorScheme
    val select = LocalSelectTab.current

    TabBody { _ ->
        ScreenHeader("حال‌نامه", subtitle = jalaliLong(today))
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            HeroCard(onClick = { sheetDay = today; sheetMood = 0 }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        if (list.isEmpty()) {
                            Text("سلام! من ابری‌ام", style = MaterialTheme.typography.headlineSmall)
                            Text("هر شب ده ثانیه حالت را بنویس؛ من کمکت می‌کنم بفهمی چه چیزی حالت را خوب می‌کند.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            HeroNumber(streak, if (streak == 0) "رکورد پیاپی از نو شروع می‌شود" else "روز پیاپی ثبت حال", unit = if (streak == 0) null else "روز")
                        }
                        PillButton(
                            if (todayEntry == null) "ثبت حال امروز" else "ویرایش امروز",
                            { sheetDay = today; sheetMood = 0 }, icon = if (todayEntry == null) Icons.Rounded.Edit else Icons.Rounded.EditNote,
                            style = BtnStyle.Tonal,
                        )
                    }
                    CloudMascot(
                        when {
                            todayEntry == null && list.isEmpty() -> MascotMood.Sleepy
                            todayEntry == null -> MascotMood.Idle
                            todayEntry.mood >= 4 -> MascotMood.Celebrating
                            todayEntry.mood == 3 -> MascotMood.Happy
                            else -> MascotMood.Idle
                        },
                        size = 120.dp,
                    )
                }
            }

            if (todayEntry == null) {
                SectionTitle("حال امروزت چطور است؟")
                PastelCard(Modifier.fillMaxWidth()) { MoodPicker(0, { sheetDay = today; sheetMood = it }) }
            } else {
                PastelCard(Modifier.fillMaxWidth(), onClick = { sheetDay = today; sheetMood = 0 }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        MoodFace(todayEntry.mood, size = 56.dp)
                        Column(Modifier.weight(1f)) {
                            Text("امروز حالت «${MOOD_NAMES[todayEntry.mood - 1]}» بود", style = MaterialTheme.typography.titleMedium)
                            val sub = todayEntry.tags.sorted().take(3).joinToString("، ") { TAGS[it] }
                            Text(sub.ifEmpty { "برچسب یا یادداشتی اضافه کن" }, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            ChatBubble(Logic.promptFor(today), title = "پرسش امروز", modifier = Modifier.clickable { sheetDay = today; sheetMood = 0 })

            // This week, Saturday first
            SectionTitle("هفتهٔ من")
            PastelCard(Modifier.fillMaxWidth()) {
                val ws = Logic.weekStart(today)
                val byDay = remember(list) { list.associateBy { it.day } }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (i in 0 until 7) {
                        val d = ws + i
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(listOf("ش", "ی", "د", "س", "چ", "پ", "ج")[i], style = MaterialTheme.typography.labelMedium, color = if (i == 6) cs.error else cs.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            val e = byDay[d]
                            val mod = Modifier.clickable(enabled = d <= today) { sheetDay = d; sheetMood = 0 }
                            if (e != null) MoodFace(e.mood, mod, 36.dp)
                            else Box(mod.size(36.dp).clip(RoundedCornerShape(50)).background(cs.surfaceContainerHigh.copy(alpha = if (d > today) 0.4f else 1f)))
                        }
                    }
                }
                Text(Logic.weekSentence(list, today), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }

            if (list.isNotEmpty()) {
                val month = remember(list, today) { Logic.inRange(list, today - 29, today) }
                val avg = Logic.average(month)
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    StatTile("ثبت در ۳۰ روز", month.size.fa(), Icons.Rounded.EditCalendar, Modifier.weight(1f))
                    StatTile("میانگین حال", avg?.let { Logic.fmt1(it) } ?: "—", Icons.Rounded.Mood, Modifier.weight(1f), container = cs.tertiaryContainer)
                    StatTile("بیشترین رکورد", Logic.longestStreak(list.map { it.day }.toSet()).fa(), Icons.Rounded.LocalFireDepartment, Modifier.weight(1f), container = cs.secondaryContainer)
                }
                SectionTitle("آخرین ثبت‌ها", action = "همه", onAction = { select(3) })
                list.takeLast(3).reversed().forEach { e -> EntryRow(e, today) { sheetDay = e.day; sheetMood = 0 } }
            }
        }
    }
    sheetDay?.let { d -> EntrySheet(store, d, sheetMood) { sheetDay = null } }
}

@Composable
fun EntryRow(e: Entry, today: Long, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    PastelCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            MoodFace(e.mood, size = 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(jalaliLong(e.day), style = MaterialTheme.typography.titleSmall)
                    Text(daysAgoText(today - e.day), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
                if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (e.tags.isNotEmpty()) Text(e.tags.sorted().joinToString(" · ") { TAGS[it] }, style = MaterialTheme.typography.labelMedium, color = cs.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (e.grat.isNotEmpty()) Text("ممنون: " + e.grat.joinToString("، "), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ───────────────────────────── Calendar ─────────────────────────────

@Composable
fun CalendarScreen(store: Store) {
    val today = store.today()
    val tj = jalali(today)
    var jy by rememberSaveable { mutableIntStateOf(tj[0]) }
    var jm by rememberSaveable { mutableIntStateOf(tj[1]) }
    var yearMode by rememberSaveable { mutableStateOf(false) }
    var sheetDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val byDay = remember(store.entries) { store.entries.associateBy { it.day } }
    val cs = MaterialTheme.colorScheme

    TabBody { _ ->
        ScreenHeader("تقویم حال", subtitle = "هر روز یک رنگ")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            PillChoice(listOf(false to "ماه", true to "سال در رنگ"), yearMode, { yearMode = it }, Modifier.fillMaxWidth())
            PastelCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, {
                        if (yearMode) jy-- else if (jm == 1) { jm = 12; jy-- } else jm--
                    }, contentDescription = "قبلی")
                    Text(if (yearMode) jy.fa() else "${JALALI_MONTHS[jm - 1]} ${jy.fa()}", style = MaterialTheme.typography.titleLarge)
                    CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, {
                        if (yearMode) jy++ else if (jm == 12) { jm = 1; jy++ } else jm++
                    }, contentDescription = "بعدی")
                }
                if (yearMode) YearPixels(jy, byDay, today) else MonthGrid(jy, jm, today, byDay, { sheetDay = it })
            }
            // legend
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                for (m in 1..5) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MoodFace(m, size = 20.dp)
                    Text(MOOD_NAMES[m - 1], style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
            }
            val from = if (yearMode) startOfMonth(jy, 1) else startOfMonth(jy, jm)
            val to = if (yearMode) startOfMonth(jy + 1, 1) - 1 else from + jalaliMonthLength(jy, jm) - 1
            val inRange = Logic.inRange(store.entries, from, to)
            val avg = Logic.average(inRange)
            PastelCard(Modifier.fillMaxWidth(), color = cs.secondaryContainer) {
                if (inRange.isEmpty()) {
                    Text(if (yearMode) "در این سال هنوز حالی ثبت نشده. روی هر روز بزن تا حالش را بنویسی." else "در این ماه هنوز حالی ثبت نشده. روی هر روز بزن تا حالش را بنویسی.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val best = inRange.maxByOrNull { it.mood * 100000 - it.day }!!
                    Text("${inRange.size.fa()} روز ثبت شده · میانگین ${Logic.fmt1(avg!!)} از ۵ («${moodWord(avg)}»)", style = MaterialTheme.typography.titleSmall)
                    Text("بهترین روز: ${jalaliLong(best.day)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    sheetDay?.let { d -> EntrySheet(store, d, 0) { sheetDay = null } }
}

// ───────────────────────────── Insights ─────────────────────────────

@Composable
fun InsightsScreen(store: Store) {
    val today = store.today()
    val list = store.entries
    val cs = MaterialTheme.colorScheme
    TabBody { _ ->
        ScreenHeader("بینش", subtitle = "چه چیزی حالم را خوب می‌کند؟")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            if (list.size < 3) {
                Box(Modifier.fillMaxWidth().padding(top = Space.md), contentAlignment = Alignment.Center) { CloudMascot(MascotMood.Sleepy, size = 130.dp) }
                EmptyState(Icons.Rounded.Insights, "هنوز کم است", "بعد از ثبت حال سه روز، نمودار و تحلیل تو اینجا ساخته می‌شود. فقط ثبت‌های خودت را می‌خوانم و جایی ارسال نمی‌شود.")
                return@TabBody
            }
            val month = remember(list, today) { Logic.inRange(list, today - 29, today) }
            val avg = Logic.average(month) ?: Logic.average(list)!!
            HeroCard(seed = 9) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Text("میانگین حال ۳۰ روز اخیر", style = MaterialTheme.typography.labelLarge)
                        Text("${Logic.fmt1(avg)} از ۵", style = MaterialTheme.typography.displaySmall)
                        Text(Logic.weekSentence(list, today), style = MaterialTheme.typography.bodyMedium)
                    }
                    RingMeter((avg / 5).toFloat(), size = 92.dp, strokeWidth = 11.dp, colors = listOf(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color(0xFFFFE9A8))) {
                        MoodFace(Math.round(avg).toInt().coerceIn(1, 5), size = 44.dp)
                    }
                }
            }

            SectionTitle("روند ۳۰ روز")
            PastelCard(Modifier.fillMaxWidth()) {
                MoodLineChart(month.map { it.day }, month.map { it.mood }, today - 29, today, Modifier.fillMaxWidth().height(150.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(jalaliShort(today - 29), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    Text(jalaliShort(today), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
            }

            SectionTitle("چه چیزی حالم را خوب می‌کند؟")
            val impacts = remember(list) { Logic.tagImpacts(list) }
            PastelCard(Modifier.fillMaxWidth()) {
                if (impacts.isEmpty()) {
                    Text("وقتی هر برچسب دست‌کم سه بار در روزهای خوب و سه بار بدون آن ثبت شود، اثرش را اینجا می‌بینی.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                } else {
                    val top = impacts.first()
                    if (top.diff > 0.2) Text("روزهایی که «${TAGS[top.tag]}» داشته‌ای، حالت ${Logic.fmt1(top.diff)} امتیاز بالاتر بوده.", style = MaterialTheme.typography.titleSmall)
                    impacts.take(6).forEach { im -> ImpactRow(im) }
                    val worst = impacts.last()
                    if (impacts.size > 1 && worst.diff < -0.2) Text("در روزهای «${TAGS[worst.tag]}» حالت کمتر بوده؛ شاید جای یک استراحت یا گفتگو باشد.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
            }

            SectionTitle("روزهای هفته")
            PastelCard(Modifier.fillMaxWidth()) {
                val wd = remember(list) { Logic.weekdayAverages(list) }
                val names = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")
                val best = wd.indices.filter { wd[it] != null }.maxByOrNull { wd[it]!! }
                if (best != null) Text("بهترین روزهای تو معمولاً ${names[best]}‌ هاست.", style = MaterialTheme.typography.titleSmall)
                RoundedBarChart(
                    wd.mapIndexed { i, v -> BarEntry(names[i].take(3), (v ?: 0.0).toFloat()) },
                    height = 120.dp, highlight = best ?: -1, valueText = { if (it <= 0f) "" else Logic.fmt1(it.toDouble()) },
                )
            }

            SectionTitle("پراکندگی حال‌ها")
            PastelCard(Modifier.fillMaxWidth()) {
                val counts = IntArray(5).also { c -> list.forEach { c[it.mood - 1]++ } }
                Row(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(50))) {
                    counts.forEachIndexed { i, c -> if (c > 0) Box(Modifier.weight(c.toFloat()).fillMaxHeight().background(Brand.mood(i + 1))) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    counts.forEachIndexed { i, c ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MoodFace(i + 1, size = 28.dp)
                            Text(c.fa(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactRow(im: Logic.TagImpact) {
    val cs = MaterialTheme.colorScheme
    val pos = im.diff >= 0
    val col = if (pos) Brand.mood(3) else Brand.mood(1)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        GradientAvatar(TAGS[im.tag], size = 36.dp)
        Column(Modifier.weight(1f)) {
            Text(TAGS[im.tag], style = MaterialTheme.typography.titleSmall)
            Text("${im.n.fa()} روز", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
        val frac = (kotlin.math.abs(im.diff) / 2.0).coerceIn(0.05, 1.0).toFloat()
        Box(Modifier.width(80.dp).height(10.dp).clip(RoundedCornerShape(50)).background(cs.surfaceContainerHigh)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(col, col.copy(alpha = 0.6f)))))
        }
        Text((if (pos) "+" else "−") + Logic.fmt1(kotlin.math.abs(im.diff)), Modifier.width(44.dp), style = MaterialTheme.typography.titleSmall, color = col)
    }
}

// ───────────────────────────── Journal ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(store: Store) {
    val today = store.today()
    var q by rememberSaveable { mutableStateOf("") }
    var mood by rememberSaveable { mutableIntStateOf(0) }
    var sheetDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val shown = remember(store.entries, q, mood) { Logic.search(store.entries, q, mood).asReversed() }
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val say = remember { Say(host, scope) }

    CompositionLocalProvider(LocalSay provides say) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                ScreenHeader("دفتر من", subtitle = "${store.entries.size.fa()} روز نوشته‌ای")
                Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    PillSearchBar(q, { q = it }, Modifier.fillMaxWidth(), placeholder = "جستجو در یادداشت‌ها و برچسب‌ها")
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(mood == 0, { mood = 0 }, { Text("همه") })
                        for (m in 1..5) FilterChip(mood == m, { mood = if (mood == m) 0 else m }, { Text(MOOD_NAMES[m - 1]) })
                    }
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = Space.md, end = Space.md, top = Space.sm, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    if (shown.isEmpty()) {
                        item(key = "empty") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                CloudMascot(MascotMood.Sleepy, size = 120.dp)
                                if (store.entries.isEmpty()) EmptyState(Icons.Rounded.MenuBook, "دفتر هنوز خالی است", "اولین حالت را از صفحهٔ «امروز» ثبت کن؛ اینجا همهٔ روزهایت به‌ترتیب جمع می‌شود.")
                                else EmptyState(Icons.Rounded.SearchOff, "چیزی پیدا نشد", "عبارت دیگری امتحان کن یا فیلتر حال را بردار.", actionText = "پاک کردن فیلتر", onAction = { q = ""; mood = 0 })
                            }
                        }
                    }
                    items(shown, key = { it.day }) { e -> EntryRow(e, today) { sheetDay = e.day } }
                }
            }
            SnackbarHost(host, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = Space.md, end = Space.md))
        }
        sheetDay?.let { d -> EntrySheet(store, d, 0) { sheetDay = null } }
    }
}
