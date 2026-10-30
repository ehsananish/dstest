package ir.ehsananish.dstest.ds

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

/*
 * Graphic kit of the design system (DESIGN.md Parts D and E).
 * Shape modifiers, animated meters and charts, count-up numbers, confetti, press feedback.
 * Everything here is additive: nothing in Components.kt changed. All animation respects the
 * system "animation off" setting (see rememberAnimationsEnabled).
 */

/** False when the user (or the test emulator) turned system animations off; then everything snaps to its final state. */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val ctx = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}

/** The 8 avatar/tile gradient pairs (red-orange, orange-yellow, green-teal, cyan-blue, blue-indigo, violet-pink, pink-red, teal-green). */
object GradientPairs {
    val all: List<Pair<Color, Color>> = listOf(
        Color(0xFFFF6B5B) to Color(0xFFFF9F43),
        Color(0xFFFFA23B) to Color(0xFFFFCF4A),
        Color(0xFF3FBF7F) to Color(0xFF1FA7A0),
        Color(0xFF22B8D8) to Color(0xFF3B86F0),
        Color(0xFF4C8DF6) to Color(0xFF6A5AE0),
        Color(0xFF9A63E8) to Color(0xFFE15FB0),
        Color(0xFFEC5F8E) to Color(0xFFF2694A),
        Color(0xFF1FB59A) to Color(0xFF52CC6B),
    )

    fun at(index: Int): Pair<Color, Color> = all[((index % all.size) + all.size) % all.size]

    /** Stable pick for a name/id, so the same item always gets the same colours. */
    fun forKey(key: String): Pair<Color, Color> = at(key.hashCode())
}

/** Four-point sparkle star centred on (0,0) with the given outer radius. */
fun sparklePath(radius: Float): Path = Path().apply {
    val inner = radius * 0.28f
    for (i in 0 until 8) {
        val r = if (i % 2 == 0) radius else inner
        val a = (PI / 4 * i - PI / 2).toFloat()
        val x = cos(a) * r
        val y = sin(a) * r
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** Smooth closed organic blob around (cx, cy), built from quadratic curves between jittered points. */
private fun blobPath(cx: Float, cy: Float, radius: Float, random: Random): Path {
    val n = 7
    val pts = List(n) { i ->
        val a = (2 * PI * i / n).toFloat()
        val r = radius * (0.72f + random.nextFloat() * 0.45f)
        Offset(cx + cos(a) * r, cy + sin(a) * r)
    }
    return Path().apply {
        fun mid(a: Offset, b: Offset) = Offset((a.x + b.x) / 2, (a.y + b.y) / 2)
        val start = mid(pts[n - 1], pts[0])
        moveTo(start.x, start.y)
        for (i in 0 until n) {
            val p = pts[i]
            val m = mid(p, pts[(i + 1) % n])
            quadraticTo(p.x, p.y, m.x, m.y)
        }
        close()
    }
}

/**
 * 2-3 soft organic blobs at the corners behind the content (low alpha, never behind text that needs contrast).
 * [seed] changes the shapes, so two cards next to each other do not look identical.
 */
fun Modifier.blobBackground(color: Color, seed: Int = 1, alpha: Float = 0.14f, count: Int = 3): Modifier =
    drawWithCache {
        val random = Random(seed)
        val w = size.width
        val h = size.height
        val base = min(w, h)
        val corners = listOf(Offset(w * 0.95f, h * 0.05f), Offset(w * 0.02f, h * 0.95f), Offset(w * 0.55f, h * 1.02f))
        val paths = List(count.coerceIn(1, 3)) { i ->
            blobPath(corners[i].x, corners[i].y, base * (0.5f - 0.08f * i + random.nextFloat() * 0.12f), random)
        }
        onDrawBehind {
            paths.forEachIndexed { i, p -> drawPath(p, color.copy(alpha = alpha * (1f - 0.18f * i))) }
        }
    }

/** Header area whose bottom edge is a gentle wave filled with [brush]. Content goes on top; leave bottom padding for the wave. */
fun Modifier.waveHeader(brush: Brush, amplitude: Dp = 14.dp, waves: Float = 1.5f): Modifier =
    drawWithCache {
        val amp = amplitude.toPx()
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - amp * 2)
            val steps = 48
            for (i in steps downTo 0) {
                val x = size.width * i / steps
                val y = size.height - amp - sin(i.toFloat() / steps * waves * 2f * PI.toFloat()) * amp
                lineTo(x, y)
            }
            close()
        }
        onDrawBehind { drawPath(path, brush) }
    }

/** Regular grid of tiny dots (texture for hero cards and empty areas). Draws only inside the bounds. */
fun Modifier.dotsBackground(color: Color, spacing: Dp = 14.dp, radius: Dp = 1.4.dp, alpha: Float = 0.18f): Modifier =
    drawBehind {
        val step = spacing.toPx()
        val r = radius.toPx()
        val c = color.copy(alpha = alpha)
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) {
                drawCircle(c, r, Offset(x, y))
                x += step
            }
            y += step
        }
    }

/** A few twinkling four-point stars scattered over the area; static when animations are off. */
fun Modifier.sparkleOverlay(color: Color, seed: Int = 7, count: Int = 5, maxSize: Dp = 14.dp): Modifier = composed {
    val animated = rememberAnimationsEnabled()
    val phase = if (animated) {
        val t = rememberInfiniteTransition(label = "sparkle")
        t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "p").value
    } else 0.25f
    drawWithCache {
        val random = Random(seed)
        val maxPx = maxSize.toPx()
        val spots = List(count) {
            Triple(Offset(random.nextFloat() * size.width, random.nextFloat() * size.height), (0.45f + random.nextFloat() * 0.55f) * maxPx, random.nextFloat())
        }
        val star = sparklePath(1f)
        onDrawBehind {
            spots.forEach { (pos, s, offset) ->
                val k = (sin(((phase + offset) % 1f) * 2f * PI.toFloat()) + 1f) / 2f
                val sz = s * (0.55f + 0.45f * k)
                withTransform({ translate(pos.x, pos.y); scale(sz, sz, Offset.Zero) }) {
                    drawPath(star, color.copy(alpha = 0.25f + 0.65f * k))
                }
            }
        }
    }
}

/** Concentric circles from a corner point, a quiet decorative layer for hero cards. */
fun Modifier.ringsBackground(color: Color, alpha: Float = 0.12f, centerX: Float = 1f, centerY: Float = 0f): Modifier =
    drawBehind {
        val c = Offset(size.width * centerX, size.height * centerY)
        val base = min(size.width, size.height)
        for (i in 1..4) drawCircle(color.copy(alpha = alpha * (1.1f - i * 0.2f)), base * 0.22f * i, c, style = DrawStroke(width = 2.dp.toPx()))
    }

/**
 * Press feedback without consuming the touch (put it before `clickable`): scales to [pressedScale] with a spring
 * and optionally gives a light haptic tick on touch down.
 */
fun Modifier.pressScale(haptic: Boolean = false, pressedScale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val feedback = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (pressed) pressedScale else 1f,
        spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "pressScale",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(haptic) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                if (haptic) feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                try {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                    } while (event.changes.any { it.pressed })
                } finally {
                    pressed = false
                }
            }
        }
}

/** Brand gradient colours for charts and meters: primary to a neighbour (tertiary mixed in). */
@Composable
fun brandGradientColors(): List<Color> {
    val cs = MaterialTheme.colorScheme
    val dark = LocalIsDark.current
    val a = if (dark) cs.primaryContainer else cs.primary
    val b = androidx.compose.ui.graphics.lerp(a, if (dark) cs.tertiaryContainer else cs.tertiary, 0.55f)
    return listOf(a, b)
}

/**
 * Ring progress meter: animated arc with round caps and a sweep gradient. [progress] is 0..1.
 * [content] is centred inside the ring (usually a CountUpText).
 */
@Composable
fun RingMeter(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 128.dp,
    strokeWidth: Dp = 14.dp,
    colors: List<Color> = brandGradientColors(),
    track: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated = rememberAnimationsEnabled()
    var started by remember { mutableStateOf(!animated) }
    LaunchedEffect(Unit) { started = true }
    val p by animateFloatAsState(
        if (started) progress.coerceIn(0f, 1f) else 0f,
        if (animated) spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessVeryLow) else tween(0),
        label = "ring",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val inset = sw / 2
            val arcSize = Size(this.size.width - sw, this.size.height - sw)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = DrawStroke(sw))
            if (p > 0.004f) {
                val first = colors.first()
                val last = colors.last()
                val brush = Brush.sweepGradient(
                    colorStops = arrayOf(0f to first, p.coerceAtLeast(0.02f) to last, 1f to last),
                    center = center,
                )
                rotate(-90f, center) {
                    drawArc(brush, 0f, 360f * p, false, Offset(inset, inset), arcSize, style = DrawStroke(sw, cap = StrokeCap.Round))
                }
            }
        }
        content()
    }
}

/** One bar of a [RoundedBarChart]. */
data class BarEntry(val label: String, val value: Float)

/**
 * Axis-light bar chart: rounded gradient bars growing in with a stagger, labels underneath, the [highlight]ed bar
 * in full brand gradient (default: the tallest) and its value above it. No grid lines.
 */
@Composable
fun RoundedBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    highlight: Int = entries.indices.maxByOrNull { entries[it].value } ?: -1,
    colors: List<Color> = brandGradientColors(),
    valueText: (Float) -> String = { it.toInt().fa() },
) {
    val cs = MaterialTheme.colorScheme
    val animated = rememberAnimationsEnabled()
    var started by remember { mutableStateOf(!animated) }
    LaunchedEffect(Unit) { started = true }
    val top = max(entries.maxOfOrNull { it.value } ?: 0f, 0.0001f)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(height), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            entries.forEachIndexed { i, e ->
                val target = if (started) (e.value / top).coerceIn(0f, 1f) else 0f
                val frac by animateFloatAsState(
                    target,
                    if (animated) tween(600, delayMillis = i * 50, easing = FastOutSlowInEasing) else tween(0),
                    label = "bar$i",
                )
                val isHi = i == highlight
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    if (isHi) Text(valueText(e.value), style = MaterialTheme.typography.labelSmall, color = cs.onSurface)
                    Box(
                        Modifier
                            .fillMaxWidth(0.62f)
                            .fillMaxHeight(if (isHi) (frac * 0.88f).coerceAtLeast(0.03f) else (frac * 0.94f).coerceAtLeast(0.03f))
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (isHi) Brush.verticalGradient(colors)
                                else Brush.verticalGradient(colors.map { it.copy(alpha = 0.32f) }),
                            ),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            entries.forEachIndexed { i, e ->
                Text(
                    e.label,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == highlight) cs.onSurface else cs.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Number that counts up from 0 the first time it is shown (and animates between values later). Persian digits. */
@Composable
fun CountUpText(
    target: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = Color.Unspecified,
    durationMillis: Int = 800,
    format: (Int) -> String = { it.fa() },
) {
    val animated = rememberAnimationsEnabled()
    var started by remember { mutableStateOf(!animated) }
    LaunchedEffect(Unit) { started = true }
    val value by animateIntAsState(
        if (started) target else 0,
        if (animated) tween(durationMillis, easing = FastOutSlowInEasing) else tween(0),
        label = "countUp",
    )
    Text(format(value), modifier, color = color, style = style)
}

private class Particle(
    val x: Float, val y: Float, val vx: Float, val vy: Float,
    val size: Float, val spin: Float, val kind: Int, val color: Color,
)

/**
 * Short celebration: 48 particles (circles, rounded rects, stars) burst from [originX]/[originY] (fractions of the
 * area), fall with gravity and fade within about 1.6 s. Change [trigger] (e.g. increment an Int) to fire it; it
 * also gives a haptic. Place it over content in a Box; it never intercepts touches. With animations off nothing is drawn,
 * unless [previewFrame] (0..1) asks for one frozen frame (used by the gallery).
 */
@Composable
fun ConfettiBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary,
        GradientPairs.at(1).first, GradientPairs.at(5).second, GradientPairs.at(3).first,
    ),
    originX: Float = 0.5f,
    originY: Float = 0.35f,
    haptic: Boolean = true,
    previewFrame: Float? = null,
) {
    val animated = rememberAnimationsEnabled()
    val feedback = LocalHapticFeedback.current
    var t by remember { mutableFloatStateOf(previewFrame ?: 1f) }
    val seedTrigger = if (previewFrame != null) 1 else trigger
    val particles = remember(seedTrigger, colors) {
        val r = Random(seedTrigger * 7919 + 13)
        List(48) {
            val angle = (-PI / 2 + (r.nextFloat() - 0.5f) * PI * 1.25).toFloat()
            val speed = 260f + r.nextFloat() * 420f
            Particle(
                originX, originY, cos(angle) * speed, sin(angle) * speed,
                6f + r.nextFloat() * 8f, (r.nextFloat() - 0.5f) * 720f, r.nextInt(3), colors[r.nextInt(colors.size)],
            )
        }
    }
    val star = remember { sparklePath(1f) }
    LaunchedEffect(trigger) {
        if (previewFrame != null || trigger <= 0 || !animated) { t = if (previewFrame != null) previewFrame else 1f; return@LaunchedEffect }
        if (haptic) feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        val start = androidx.compose.runtime.withFrameNanos { it }
        t = 0f
        while (t < 1f) {
            val now = androidx.compose.runtime.withFrameNanos { it }
            t = ((now - start) / 1_600_000_000f).coerceAtMost(1f)
        }
    }
    if (t >= 1f && previewFrame == null) return
    Canvas(modifier.fillMaxSize()) {
        val sec = t * 1.6f
        val density = this.density
        particles.forEach { p ->
            val x = p.x * size.width + p.vx * sec * density * 0.5f
            val y = p.y * size.height + (p.vy * sec + 0.5f * 1000f * sec * sec) * density * 0.5f
            val fade = if (t > 0.65f) 1f - (t - 0.65f) / 0.35f else 1f
            val s = p.size * density
            val c = p.color.copy(alpha = fade.coerceIn(0f, 1f))
            withTransform({ translate(x, y); rotate(p.spin * sec, Offset.Zero) }) {
                when (p.kind) {
                    0 -> drawCircle(c, s / 2, Offset.Zero)
                    1 -> drawRoundRect(c, Offset(-s / 2, -s / 3), Size(s, s * 0.66f), CornerRadius(s / 6))
                    else -> withTransform({ scale(s, s, Offset.Zero) }) { drawPath(star, c) }
                }
            }
        }
    }
}

/** Draws a [DrawScope] vertical fade-in helper-free: internal convenience for hero scenes. */
internal fun DrawScope.softGlow(color: Color, center: Offset, radius: Float, alpha: Float = 0.5f) {
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)), center, radius), radius, center)
}
