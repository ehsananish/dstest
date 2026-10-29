package ir.ehsananish.dstest.ds

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Soft pastel color blobs drawn behind headers and onboarding. */
@Composable
fun PastelBackdrop(modifier: Modifier = Modifier) {
    if (isBreeze()) return
    val cs = MaterialTheme.colorScheme
    val alpha = if (LocalIsDark.current) 0.35f else 0.9f
    Canvas(modifier) {
        fun blob(c: Color, x: Float, y: Float, r: Float) {
            val center = Offset(x, y)
            drawCircle(
                brush = Brush.radialGradient(listOf(c.copy(alpha = alpha), c.copy(alpha = 0f)), center, r),
                radius = r,
                center = center,
            )
        }
        blob(cs.primaryContainer, size.width * 0.85f, size.height * 0.1f, size.width * 0.75f)
        blob(cs.tertiaryContainer, size.width * 0.05f, size.height * 0.4f, size.width * 0.6f)
        blob(cs.secondaryContainer, size.width * 0.6f, size.height * 0.8f, size.width * 0.5f)
    }
}

@Composable
fun isBreeze(): Boolean = LocalStyle.current == DesignStyle.Breeze

/** Color of the top app bar in Breeze style (accent in light mode, raised navy in dark mode). */
@Composable
fun headerBarColor(): Color =
    if (LocalIsDark.current) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primary

/** The saturated color behind a pastel container (used for Breeze's solid icon tiles). */
@Composable
private fun strongFor(container: Color): Color {
    val cs = MaterialTheme.colorScheme
    return when (container) {
        cs.primaryContainer -> cs.primary
        cs.secondaryContainer -> cs.secondary
        cs.tertiaryContainer -> cs.tertiary
        else -> container
    }
}

/** Rounded tonal card; the main building block of every screen. */
@Composable
fun PastelCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = contentColorFor(color),
    outlined: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(if (isBreeze()) 14.dp else Radius.lg)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = if (outlined) BorderStroke(Stroke.hairline, MaterialTheme.colorScheme.outlineVariant) else null,
    ) {
        Column(Modifier.padding(Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm), content = content)
    }
}

enum class BtnStyle { Filled, Tonal, Outlined }

/** Pill-shaped button with a bouncy press animation. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: BtnStyle = BtnStyle.Filled,
    enabled: Boolean = true,
) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = when (style) {
        BtnStyle.Filled -> cs.primary to cs.onPrimary
        BtnStyle.Tonal -> cs.secondaryContainer to cs.onSecondaryContainer
        BtnStyle.Outlined -> Color.Transparent to cs.primary
    }
    val breeze = isBreeze()
    val shape = if (breeze) RoundedCornerShape(10.dp) else CircleShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) (if (breeze) 0.98f else 0.94f) else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
    Row(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .clip(shape)
            .background(bg)
            .then(if (style == BtnStyle.Outlined) Modifier.border(Stroke.bold, if (breeze) cs.primary else cs.outline, shape) else Modifier)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, enabled = enabled, onClick = onClick)
            .heightIn(min = if (breeze) 48.dp else 52.dp)
            .padding(horizontal = Space.lg, vertical = Space.sm),
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Text(text, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentDescription: String? = null,
    tint: Color = contentColorFor(container).takeOrElse { LocalContentColor.current },
) {
    Box(
        modifier.size(44.dp).clip(CircleShape).background(container).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/** Icon on a soft rounded-square background. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = contentColorFor(container),
    size: Dp = 48.dp,
) {
    // Breeze: solid saturated tile with a white icon; circles for large badges.
    val breeze = isBreeze()
    val strong = strongFor(container)
    val bg = if (breeze) strong else container
    val fg = if (breeze && strong != container) Color.White else tint
    val shape = when {
        breeze && size >= 72.dp -> CircleShape
        breeze -> RoundedCornerShape(size * 0.28f)
        else -> RoundedCornerShape(size * 0.36f)
    }
    Box(modifier.size(size).clip(shape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(size * if (breeze) 0.55f else 0.5f))
    }
}

/** Big bold page title, optional back button and actions. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (isBreeze()) {
        val bar = headerBarColor()
        val onBar = if (LocalIsDark.current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        CompositionLocalProvider(LocalContentColor provides onBar) {
            Row(
                modifier.fillMaxWidth().background(bar).heightIn(min = 60.dp)
                    .padding(horizontal = Space.xs, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, container = Color.Transparent, contentDescription = "برگشت", tint = onBar)
                } else {
                    Spacer(Modifier.width(Space.sm))
                }
                Column(Modifier.weight(1f).padding(horizontal = Space.xs)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = onBar, maxLines = 1)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = onBar.copy(alpha = 0.75f), maxLines = 1)
                    }
                }
                actions()
            }
        }
        Spacer(Modifier.height(Space.sm))
        return
    }
    Column(modifier.fillMaxWidth().padding(horizontal = Space.lg, vertical = Space.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, contentDescription = "برگشت")
                Spacer(Modifier.width(Space.sm))
            }
            Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            actions()
        }
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    val breeze = isBreeze()
    Row(modifier.fillMaxWidth().padding(top = Space.sm, bottom = Space.xxs), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = if (breeze) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = if (breeze) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            modifier = Modifier.weight(1f).padding(horizontal = if (breeze) Space.xs else 0.dp),
        )
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction)
                    .padding(horizontal = Space.sm, vertical = Space.xxs),
            )
        }
    }
}

/** Segmented control made of pills. */
@Composable
fun <T> PillChoice(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val breeze = isBreeze()
    val outer = if (breeze) RoundedCornerShape(10.dp) else CircleShape
    val inner = if (breeze) RoundedCornerShape(8.dp) else CircleShape
    Row(
        modifier.fillMaxWidth().clip(outer).background(cs.surfaceContainerHigh).padding(Space.xxs),
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            val bg by animateColorAsState(if (on) cs.primary else Color.Transparent, label = "pill")
            val fg by animateColorAsState(if (on) cs.onPrimary else cs.onSurfaceVariant, label = "pillText")
            Box(
                Modifier.weight(1f).clip(inner).background(bg).clickable { onSelect(value) }
                    .padding(vertical = Space.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = fg, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    if (isBreeze()) {
        Column(
            modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerLowest).padding(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.xxs),
        ) {
            IconBadge(icon, size = 36.dp, container = container)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val fg = contentColorFor(container)
    Column(
        modifier.clip(RoundedCornerShape(Radius.lg)).background(container).padding(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = fg)
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg.copy(alpha = 0.8f))
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.fillMaxWidth().padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        IconBadge(icon, size = 88.dp, container = MaterialTheme.colorScheme.tertiaryContainer)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null) PillButton(actionText, onAction, style = BtnStyle.Tonal)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        IconBadge(icon, size = 42.dp, container = container)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)

/** Floating pill navigation bar; the selected item grows to show its label. */
@Composable
fun PillBottomBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    if (isBreeze()) {
        // Flat full-width bar with every label visible.
        Column(modifier.fillMaxWidth().background(cs.surfaceContainerLowest)) {
            Box(Modifier.fillMaxWidth().height(Stroke.hairline).background(cs.outlineVariant))
            Row(Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp)) {
                items.forEachIndexed { i, item ->
                    val c by animateColorAsState(if (i == selected) cs.primary else cs.onSurfaceVariant, label = "navB")
                    Column(
                        Modifier.weight(1f).fillMaxHeight().clickable { onSelect(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(item.icon, null, tint = c, modifier = Modifier.size(24.dp))
                        Text(item.label, style = MaterialTheme.typography.labelSmall, color = c, maxLines = 1)
                    }
                }
            }
        }
        return
    }
    Row(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = Space.md, vertical = Space.sm)
            .fillMaxWidth()
            .shadow(12.dp, CircleShape, ambientColor = cs.primary.copy(alpha = 0.3f), spotColor = cs.primary.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(cs.surfaceContainerLowest)
            .padding(Space.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, item ->
            val on = i == selected
            val bg by animateColorAsState(if (on) cs.primaryContainer else Color.Transparent, label = "nav")
            Row(
                Modifier.clip(CircleShape).background(bg).clickable { onSelect(i) }
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    .padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                Icon(item.icon, item.label, tint = if (on) cs.onPrimaryContainer else cs.onSurfaceVariant, modifier = Modifier.size(24.dp))
                if (on) Text(item.label, style = MaterialTheme.typography.labelLarge, color = cs.onPrimaryContainer, maxLines = 1)
            }
        }
    }
}
