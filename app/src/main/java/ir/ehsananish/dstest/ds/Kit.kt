package ir.ehsananish.dstest.ds

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/*
 * Telegram-like building blocks of the design system (DESIGN.md Parts B, C, F).
 * Additive: they sit beside the older Components.kt and never replace it.
 */

/**
 * Circle with a two-colour gradient and an initial, emoji or icon. The gradient is picked from [GradientPairs]
 * by hashing [seed] (or forced with [pair]), so the same name always gets the same colour.
 * Use it for people, categories, accounts, habits, medicines: anything that would be a grey icon.
 */
@Composable
fun GradientAvatar(
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    label: String? = null,
    icon: ImageVector? = null,
    pair: Int? = null,
) {
    val (a, b) = if (pair != null) GradientPairs.at(pair) else GradientPairs.forKey(seed)
    Box(
        modifier.size(size).clip(CircleShape).background(Brush.linearGradient(listOf(a, b))),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
        } else {
            val text = label ?: seed.trim().take(1)
            Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontSize = (size.value * 0.42f).sp, lineHeight = (size.value * 0.5f).sp))
        }
    }
}

/**
 * Rounded-square tile holding a white icon on a solid colour or a gradient ([colors] with 1 or 2 entries;
 * default = one of [GradientPairs] by [pair], or the theme primary when [pair] is null).
 * Telegram-settings style; use it instead of a bare grey glyph.
 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    pair: Int? = null,
    colors: List<Color>? = null,
    circle: Boolean = false,
    contentDescription: String? = null,
) {
    val cs = MaterialTheme.colorScheme
    val list = colors ?: pair?.let { GradientPairs.at(it).let { p -> listOf(p.first, p.second) } } ?: listOf(cs.primary)
    val shape = if (circle) CircleShape else RoundedCornerShape(size * 0.3f)
    val fill = if (list.size == 1) Modifier.background(list[0]) else Modifier.background(Brush.linearGradient(list))
    Box(modifier.size(size).clip(shape).then(fill), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}

/** Gradient colours for a [HeroCard]: strong primary to tertiary in light mode, deep tinted containers in dark mode. */
@Composable
fun heroColors(): List<Color> {
    val cs = MaterialTheme.colorScheme
    return if (LocalIsDark.current) {
        listOf(cs.primaryContainer, lerp(cs.primaryContainer, cs.tertiaryContainer, 0.35f))
    } else {
        listOf(cs.primary, lerp(lerp(cs.primary, cs.tertiary, 0.3f), Color.Black, 0.22f))
    }
}

/** Text/icon colour that reads well on [heroColors]. */
@Composable
fun heroContentColor(): Color = if (LocalIsDark.current) MaterialTheme.colorScheme.onPrimaryContainer else Color.White

/**
 * The first thing on a home screen (law D2): a big rounded gradient field with decorative blobs, rings and dots,
 * and a free [content] slot for the big number, a sentence and the main action. Content colour is set for you;
 * read it with LocalContentColor. Pass [colors] for an app-specific brand gradient.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = heroColors(),
    contentColor: Color = heroContentColor(),
    seed: Int = 3,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(if (isBreeze()) 16.dp else Radius.xl)
    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .blobBackground(Color.White, seed = seed, alpha = 0.16f)
            .ringsBackground(Color.White, alpha = 0.22f, centerX = 0f, centerY = 0f)
            .dotsBackground(Color.White, alpha = 0.16f)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.xs), content = content)
        }
    }
}

/** Convenience for the hero's big number: count-up numeral with a caption line. */
@Composable
fun HeroNumber(value: Int, caption: String, modifier: Modifier = Modifier, unit: String? = null) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
            CountUpText(value, style = MaterialTheme.typography.displayMedium, color = LocalContentColor.current)
            if (unit != null) {
                Text(unit, Modifier.padding(bottom = 10.dp), style = MaterialTheme.typography.titleMedium, color = LocalContentColor.current.copy(alpha = 0.85f))
            }
        }
        Text(caption, style = MaterialTheme.typography.bodyMedium, color = LocalContentColor.current.copy(alpha = 0.9f))
    }
}

/**
 * Message-style bubble (tips, coach messages, history entries). 18 dp corners with one 4 dp "tail" corner.
 * [mine] = false: soft surface, tail at the start side; true: brand tint, tail at the end side. [time] is a tiny stamp.
 */
@Composable
fun ChatBubble(
    text: String,
    modifier: Modifier = Modifier,
    mine: Boolean = false,
    time: String? = null,
    title: String? = null,
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (mine) cs.primaryContainer else cs.surfaceContainerHigh
    val fg = if (mine) cs.onPrimaryContainer else cs.onSurface
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (mine) 18.dp else 4.dp,
        bottomEnd = if (mine) 4.dp else 18.dp,
    )
    Column(modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
        Column(
            Modifier.widthIn(max = 300.dp).clip(shape).background(bg).padding(horizontal = 14.dp, vertical = 9.dp),
        ) {
            if (title != null) Text(title, style = MaterialTheme.typography.labelMedium, color = cs.primary)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = fg)
            if (time != null) {
                Text(
                    time, Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/** Small bold accent-coloured section label above a [SettingsGroup] (Telegram Settings look). */
@Composable
fun BlueSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.fillMaxWidth().padding(start = Space.md + Space.xs, end = Space.md, top = Space.md, bottom = Space.xs),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** Rounded grouped card for [SettingsGroupRow]s, with an optional [title] header above it. */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        if (title != null) BlueSectionHeader(title)
        val shape = RoundedCornerShape(if (isBreeze()) 14.dp else Radius.lg)
        Column(
            Modifier.fillMaxWidth().clip(shape).background(MaterialTheme.colorScheme.surfaceContainerLowest),
            content = content,
        )
    }
}

/**
 * One row of a [SettingsGroup]: [IconTile] on the start side, title, optional subtitle, value + chevron (or any
 * [trailing] such as a switch) on the end side. Set [divider] = false on the last row.
 */
@Composable
fun SettingsGroupRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    pair: Int? = null,
    tileColors: List<Color>? = null,
    divider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val lineColor = cs.outlineVariant
    val inset = Space.md + 34.dp + Space.md
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .drawBehind {
                if (divider) {
                    val y = size.height - 0.5.dp.toPx()
                    val startX = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) 0f else inset.toPx()
                    val endX = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) size.width - inset.toPx() else size.width
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(startX, y), androidx.compose.ui.geometry.Offset(endX, y), 1.dp.toPx())
                }
            }
            .heightIn(min = 56.dp)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        IconTile(icon, size = 34.dp, pair = pair, colors = tileColors)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = cs.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        }
    }
}

/** Floating pill-shaped search field with a search icon and a clear button. */
@Composable
fun PillSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "جستجو",
) {
    val cs = MaterialTheme.colorScheme
    val shape = if (isBreeze()) RoundedCornerShape(12.dp) else CircleShape
    Row(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = cs.primary.copy(alpha = 0.25f), spotColor = cs.primary.copy(alpha = 0.25f))
            .clip(shape)
            .background(cs.surfaceContainerLowest)
            .heightIn(min = 48.dp)
            .padding(horizontal = Space.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Icon(Icons.Rounded.Search, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = cs.onSurface),
                cursorBrush = SolidColor(cs.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable { onValueChange("") },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Close, "پاک کردن", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
        }
    }
}

enum class MascotMood { Idle, Happy, Sleepy, Celebrating }

/**
 * Stage for the app's own mascot (law D6). The ds does not draw a character; the app passes a drawing lambda that
 * receives the [mood] and [blink] (true for ~150 ms every 3-5 s) and draws itself with Canvas into the box.
 * The frame adds a soft glow disc, idle floating (+-4 dp), sparkles when happy/celebrating and "Zz" when sleepy.
 * Static when system animations are off.
 */
@Composable
fun MascotFrame(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    glow: Color = MaterialTheme.colorScheme.primaryContainer,
    mascot: @Composable BoxScope.(mood: MascotMood, blink: Boolean) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val animated = rememberAnimationsEnabled()
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(animated, mood) {
        if (!animated || mood == MascotMood.Sleepy) { blink = false; return@LaunchedEffect }
        while (true) {
            delay(3000L + (System.nanoTime() % 2000L))
            blink = true
            delay(150)
            blink = false
        }
    }
    val bob = if (animated) {
        val t = rememberInfiniteTransition(label = "mascot")
        val period = if (mood == MascotMood.Celebrating) 700 else if (mood == MascotMood.Sleepy) 3200 else 2200
        t.animateFloat(-1f, 1f, infiniteRepeatable(tween(period, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob").value
    } else 0f
    val amp = if (mood == MascotMood.Celebrating) 8f else if (mood == MascotMood.Sleepy) 2f else 4f
    Box(
        modifier.size(size).let {
            if (mood == MascotMood.Happy || mood == MascotMood.Celebrating) it.sparkleOverlay(cs.tertiary, seed = mood.ordinal + 11, count = 6, maxSize = 12.dp) else it
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(size * 0.86f).clip(CircleShape).background(Brush.radialGradient(listOf(glow.copy(alpha = 0.95f), glow.copy(alpha = 0.25f)))))
        Box(
            Modifier.size(size * 0.7f).graphicsLayer { translationY = bob * amp * density },
            contentAlignment = Alignment.Center,
        ) { mascot(mood, blink) }
        if (mood == MascotMood.Sleepy) {
            Text("ززز", Modifier.align(Alignment.TopEnd).padding(top = 4.dp), style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
        }
    }
}
