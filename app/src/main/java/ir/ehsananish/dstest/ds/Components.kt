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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Soft pastel color blobs drawn behind headers and onboarding. */
@Composable
fun PastelBackdrop(modifier: Modifier = Modifier) {
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
    val shape = RoundedCornerShape(Radius.lg)
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f,
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
            .clip(CircleShape)
            .background(bg)
            .then(if (style == BtnStyle.Outlined) Modifier.border(Stroke.bold, cs.outline, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, enabled = enabled, onClick = onClick)
            .heightIn(min = 52.dp)
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
) {
    Box(
        modifier.size(44.dp).clip(CircleShape).background(container).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = contentColorFor(container), modifier = Modifier.size(22.dp))
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
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * 0.36f)).background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f))
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
    Row(modifier.fillMaxWidth().padding(top = Space.sm, bottom = Space.xxs), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
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
    Row(
        modifier.fillMaxWidth().clip(CircleShape).background(cs.surfaceContainerHigh).padding(Space.xxs),
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            val bg by animateColorAsState(if (on) cs.primary else Color.Transparent, label = "pill")
            val fg by animateColorAsState(if (on) cs.onPrimary else cs.onSurfaceVariant, label = "pillText")
            Box(
                Modifier.weight(1f).clip(CircleShape).background(bg).clickable { onSelect(value) }
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
