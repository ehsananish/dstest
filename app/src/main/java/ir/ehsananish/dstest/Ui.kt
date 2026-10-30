package ir.ehsananish.dstest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.ehsananish.dstest.ds.MascotFrame
import ir.ehsananish.dstest.ds.MascotMood
import ir.ehsananish.dstest.ds.Space
import ir.ehsananish.dstest.ds.fa
import ir.ehsananish.dstest.ds.pressScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Brand layer: the five mood colours (cold and calm → warm and bright) used for faces, calendar and charts. */
object Brand {
    val moodColors = listOf(Color(0xFF6E6BD8), Color(0xFF4FA3E3), Color(0xFF3DBE9B), Color(0xFFF2B33D), Color(0xFFFF7A59))
    fun mood(m: Int): Color = moodColors[(m - 1).coerceIn(0, 4)]
    fun moodSoft(m: Int): Color = mood(m).copy(alpha = 0.22f)
    /** For a fractional average. */
    fun moodAt(avg: Double): Color = mood(Math.round(avg).toInt().coerceIn(1, 5))
}

/** Snackbar helper shared by the tab screens. */
class Say(val host: SnackbarHostState, private val scope: CoroutineScope) {
    fun say(msg: String, action: String? = null, onAction: () -> Unit = {}) {
        scope.launch {
            host.currentSnackbarData?.dismiss()
            val r = host.showSnackbar(msg, actionLabel = action, duration = SnackbarDuration.Short)
            if (r == SnackbarResult.ActionPerformed) onAction()
        }
    }
}

val LocalSay = staticCompositionLocalOf<Say?> { null }

/** Scrolling tab body with a snackbar host above the floating bottom bar. */
@Composable
fun TabBody(scroll: Boolean = true, content: @Composable ColumnScope.(Say) -> Unit) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val say = remember { Say(host, scope) }
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSay provides say) {
            Column(
                Modifier.fillMaxSize().then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(bottom = if (scroll) 110.dp else 0.dp),
            ) { content(say) }
        }
        SnackbarHost(host, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = Space.md, end = Space.md))
    }
}

fun daysAgoText(days: Long): String = when {
    days <= 0L -> "امروز"
    days == 1L -> "دیروز"
    else -> "${days.fa()} روز پیش"
}

/** «ابری», the little cloud that is the face of the app. Mood follows the app state. */
@Composable
fun CloudMascot(mood: MascotMood, modifier: Modifier = Modifier, size: Dp = 140.dp) {
    val cs = MaterialTheme.colorScheme
    val body = Color.White
    val line = Color(0xFF3B3560)
    val cheek = Color(0xFFFF9EB5)
    MascotFrame(mood, modifier, size, glow = cs.primaryContainer) { m, blink ->
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val shade = cs.primary.copy(alpha = 0.18f)
            // cloud silhouette: three puffs and a base
            drawCircle(shade, w * 0.27f, Offset(w * 0.32f, w * 0.62f + 6f))
            drawCircle(shade, w * 0.33f, Offset(w * 0.55f, w * 0.5f + 6f))
            drawCircle(shade, w * 0.25f, Offset(w * 0.76f, w * 0.64f + 6f))
            drawCircle(body, w * 0.27f, Offset(w * 0.32f, w * 0.62f))
            drawCircle(body, w * 0.33f, Offset(w * 0.55f, w * 0.48f))
            drawCircle(body, w * 0.25f, Offset(w * 0.76f, w * 0.62f))
            drawRoundRect(body, Offset(w * 0.12f, w * 0.58f), Size(w * 0.80f, w * 0.30f), CornerRadius(w * 0.15f))
            val ey = w * 0.60f
            val lx = w * 0.40f
            val rx = w * 0.64f
            val sw = w * 0.035f
            when {
                m == MascotMood.Sleepy || blink -> {
                    drawArc(line, 0f, 180f, false, Offset(lx - w * 0.04f, ey - w * 0.02f), Size(w * 0.08f, w * 0.05f), style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(line, 0f, 180f, false, Offset(rx - w * 0.04f, ey - w * 0.02f), Size(w * 0.08f, w * 0.05f), style = Stroke(sw, cap = StrokeCap.Round))
                }
                m == MascotMood.Happy || m == MascotMood.Celebrating -> {
                    drawArc(line, 180f, 180f, false, Offset(lx - w * 0.04f, ey - w * 0.03f), Size(w * 0.08f, w * 0.07f), style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(line, 180f, 180f, false, Offset(rx - w * 0.04f, ey - w * 0.03f), Size(w * 0.08f, w * 0.07f), style = Stroke(sw, cap = StrokeCap.Round))
                }
                else -> {
                    drawCircle(line, w * 0.032f, Offset(lx, ey))
                    drawCircle(line, w * 0.032f, Offset(rx, ey))
                    drawCircle(Color.White, w * 0.01f, Offset(lx + w * 0.01f, ey - w * 0.01f))
                    drawCircle(Color.White, w * 0.01f, Offset(rx + w * 0.01f, ey - w * 0.01f))
                }
            }
            drawCircle(cheek.copy(alpha = 0.7f), w * 0.04f, Offset(w * 0.33f, w * 0.68f))
            drawCircle(cheek.copy(alpha = 0.7f), w * 0.04f, Offset(w * 0.71f, w * 0.68f))
            val my = w * 0.70f
            if (m == MascotMood.Celebrating) {
                val mouth = Path().apply {
                    moveTo(w * 0.46f, my); quadraticBezierTo(w * 0.52f, my + w * 0.14f, w * 0.58f, my); close()
                }
                drawPath(mouth, line)
            } else {
                val dip = when (m) { MascotMood.Happy -> w * 0.07f; MascotMood.Sleepy -> w * 0.0f; else -> w * 0.04f }
                val mouth = Path().apply { moveTo(w * 0.46f, my); quadraticBezierTo(w * 0.52f, my + dip, w * 0.58f, my) }
                drawPath(mouth, line, style = Stroke(sw, cap = StrokeCap.Round))
            }
        }
    }
}

/** Round gradient face for mood 1..5 (law D8: never a bare glyph). */
@Composable
fun MoodFace(mood: Int, modifier: Modifier = Modifier, size: Dp = 48.dp, selected: Boolean = true) {
    val c = Brand.mood(mood)
    val bg = if (selected) Brush.linearGradient(listOf(c, c.copy(alpha = 0.78f))) else Brush.linearGradient(listOf(c.copy(alpha = 0.22f), c.copy(alpha = 0.22f)))
    val ink = if (selected) Color.White else c
    Canvas(modifier.size(size).clip(CircleShape).background(bg)) {
        val w = this.size.width
        val sw = w * 0.07f
        val ex = w * 0.30f
        val ey = w * 0.40f
        if (mood == 5) {
            drawArc(ink, 180f, 180f, false, Offset(ex - w * 0.07f, ey - w * 0.05f), Size(w * 0.14f, w * 0.12f), style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(ink, 180f, 180f, false, Offset(w - ex - w * 0.07f, ey - w * 0.05f), Size(w * 0.14f, w * 0.12f), style = Stroke(sw, cap = StrokeCap.Round))
        } else {
            drawCircle(ink, w * 0.065f, Offset(ex, ey))
            drawCircle(ink, w * 0.065f, Offset(w - ex, ey))
        }
        val curve = when (mood) { 1 -> -0.2f; 2 -> -0.09f; 3 -> 0f; 4 -> 0.1f; else -> 0.2f }
        val my = w * 0.66f
        val p = Path().apply { moveTo(w * 0.32f, my); quadraticBezierTo(w * 0.5f, my + w * curve * 1.6f, w * 0.68f, my) }
        drawPath(p, ink, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (mood == 1) { // a tear
            drawCircle(Color.White.copy(alpha = 0.85f), w * 0.04f, Offset(w * 0.22f, w * 0.52f))
        }
    }
}

/** Five tappable faces. [selected] 0 = none. */
@Composable
fun MoodPicker(selected: Int, onPick: (Int) -> Unit, modifier: Modifier = Modifier, faceSize: Dp = 52.dp) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (m in 1..5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                MoodFace(
                    m,
                    Modifier.pressScale(haptic = true).clickable { onPick(m) }.semantics { contentDescription = "حال ${MOOD_NAMES[m - 1]}" },
                    faceSize, selected = selected == 0 || selected == m,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    MOOD_NAMES[m - 1], style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
                    color = if (selected == m) Brand.mood(m) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 30-day mood line: gradient fill, round caps, a dot per logged day coloured by mood. */
@Composable
fun MoodLineChart(days: List<Long>, moods: List<Int>, from: Long, to: Long, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val line = cs.primary
    Canvas(modifier) {
        val padX = 10.dp.toPx()
        val padY = 14.dp.toPx()
        val h = size.height - 2 * padY
        val span = (to - from).coerceAtLeast(1).toFloat()
        fun x(d: Long) = padX + (d - from) / span * (size.width - 2 * padX)
        fun y(m: Int) = padY + (5 - m) / 4f * h
        for (m in 1..5 step 2) {
            drawLine(cs.outlineVariant.copy(alpha = 0.5f), Offset(padX, y(m)), Offset(size.width - padX, y(m)), 1.dp.toPx())
        }
        if (days.isEmpty()) return@Canvas
        val path = Path()
        days.indices.forEach { i -> if (i == 0) path.moveTo(x(days[i]), y(moods[i])) else path.lineTo(x(days[i]), y(moods[i])) }
        val fill = Path().apply {
            addPath(path)
            lineTo(x(days.last()), size.height - padY); lineTo(x(days.first()), size.height - padY); close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(line.copy(alpha = 0.28f), line.copy(alpha = 0f))))
        drawPath(path, line, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        days.indices.forEach { i ->
            drawCircle(cs.surface, 6.dp.toPx(), Offset(x(days[i]), y(moods[i])))
            drawCircle(Brand.mood(moods[i]), 4.dp.toPx(), Offset(x(days[i]), y(moods[i])))
        }
    }
}

/** Jalali month grid, Saturday first. Days with an entry are filled with the mood colour. */
@Composable
fun MonthGrid(jy: Int, jm: Int, today: Long, byDay: Map<Long, Entry>, onDay: (Long) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val first = startOfMonth(jy, jm)
    val len = jalaliMonthLength(jy, jm)
    val lead = Logic.satOffset(first)
    val rows = (lead + len + 6) / 7
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until 7) {
                    val dnum = r * 7 + c - lead + 1
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dnum in 1..len) {
                            val day = first + dnum - 1
                            val e = byDay[day]
                            val future = day > today
                            val bg = when {
                                e != null -> Brand.mood(e.mood)
                                else -> cs.surfaceContainerHigh.copy(alpha = if (future) 0.4f else 1f)
                            }
                            Box(
                                Modifier.fillMaxSize().clip(CircleShape).background(bg)
                                    .then(if (day == today) Modifier.background(Color.Transparent) else Modifier)
                                    .clickable(enabled = !future) { onDay(day) }
                                    .semantics { contentDescription = "${dnum.fa()} ${JALALI_MONTHS[jm - 1]}" + (e?.let { "، حال ${MOOD_NAMES[it.mood - 1]}" } ?: "") },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (day == today) Canvas(Modifier.fillMaxSize()) { drawCircle(cs.primary, size.minDimension / 2 - 1.5.dp.toPx(), style = Stroke(2.5.dp.toPx())) }
                                Text(
                                    dnum.fa(), style = MaterialTheme.typography.labelLarge,
                                    color = if (e != null) Color.White else if (future) cs.onSurface.copy(alpha = 0.35f) else cs.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** «سال در رنگ»: twelve rows (months) of small squares, one per day. */
@Composable
fun YearPixels(jy: Int, byDay: Map<Long, Entry>, today: Long, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (m in 1..12) {
            val first = startOfMonth(jy, m)
            val len = jalaliMonthLength(jy, m)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(JALALI_MONTHS[m - 1], Modifier.width(62.dp), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1)
                Canvas(Modifier.weight(1f).height(14.dp)) {
                    val gap = 2.dp.toPx()
                    val cell = (size.width - gap * 30) / 31f
                    for (d in 1..len) {
                        val day = first + d - 1
                        val e = byDay[day]
                        val col = when {
                            e != null -> Brand.mood(e.mood)
                            day > today -> cs.surfaceContainerHigh.copy(alpha = 0.4f)
                            else -> cs.surfaceContainerHigh
                        }
                        // RTL: day 1 at the right edge
                        val x = size.width - d * cell - (d - 1) * gap
                        drawRoundRect(col, Offset(x, 0f), Size(cell, size.height), CornerRadius(cell / 3))
                    }
                }
            }
        }
    }
}
