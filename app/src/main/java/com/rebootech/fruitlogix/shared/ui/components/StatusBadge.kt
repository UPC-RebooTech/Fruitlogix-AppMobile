package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.isOnDarkSurface

enum class StatusBadgeType {
    /** Delivered, approved, on time (Maduro) */
    SUCCESS,

    /** Needs attention, due today, delay (Mango) */
    WARNING,

    /** Rejected, overdue, critical (Granada) */
    DANGER,

    /** Informational, in transit, cold chain (Glaciar) */
    INFO,

    /** Pending, draft, neutral states (Menta) */
    NEUTRAL,

    /** Ready, featured (Lima) */
    HIGHLIGHT
}

private data class BadgeColors(val container: Color, val content: Color, val dot: Color)

@Composable
private fun badgeColors(type: StatusBadgeType, onDark: Boolean): BadgeColors {
    val c = FruitLogixTheme.colors
    val scheme = MaterialTheme.colorScheme
    return if (onDark) {
        val accent = when (type) {
            StatusBadgeType.SUCCESS -> c.success
            StatusBadgeType.WARNING -> c.warning
            StatusBadgeType.DANGER -> c.dangerStrong
            StatusBadgeType.INFO -> c.info
            StatusBadgeType.NEUTRAL -> c.textMuted
            StatusBadgeType.HIGHLIGHT -> c.primary
        }
        BadgeColors(container = accent.copy(alpha = 0.18f), content = accent, dot = accent)
    } else {
        when (type) {
            StatusBadgeType.SUCCESS -> BadgeColors(c.successContainer, c.onSuccessContainer, c.successContent)
            StatusBadgeType.WARNING -> BadgeColors(c.attentionContainer, c.onAttentionContainer, c.attentionContent)
            StatusBadgeType.DANGER -> BadgeColors(scheme.errorContainer, scheme.onErrorContainer, scheme.error)
            StatusBadgeType.INFO -> BadgeColors(c.coldContainer, c.onColdContainer, c.coldContent)
            StatusBadgeType.NEUTRAL -> BadgeColors(scheme.secondaryContainer, scheme.onSecondaryContainer, scheme.onSurfaceVariant)
            StatusBadgeType.HIGHLIGHT -> BadgeColors(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.onPrimaryContainer)
        }
    }
}

/**
 * Status tag: 8dp corners, leading dot (or icon) + text, so the state never depends on color alone.
 * Colors adapt automatically to light or dark surfaces.
 *
 * @param text Badge text (sentence case reads better than uppercase for older users)
 * @param type Semantic state
 * @param icon Optional leading icon; replaces the dot when provided
 * @param showDot Show the leading dot when there is no icon
 * @param onDarkSurface Force the dark-surface variant; null = detect from the container
 */
@Composable
fun StatusBadge(
    text: String,
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp,
    showDot: Boolean = true,
    onDarkSurface: Boolean? = null,
    icon: (@Composable () -> Unit)? = null
) {
    val colors = badgeColors(type, onDarkSurface ?: isOnDarkSurface())

    Box(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Tag)
            .background(colors.container)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                icon != null -> {
                    icon()
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
                }
                showDot -> {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.dot)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Text(
                text = text,
                color = colors.content,
                style = FruitLogixTheme.typography.labelMedium,
                maxLines = maxLines,
                softWrap = softWrap
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7FAF6)
@Composable
private fun StatusBadgePreview() {
    FruitLogixTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(text = "Entregado", type = StatusBadgeType.SUCCESS)
            StatusBadge(text = "Vence hoy", type = StatusBadgeType.WARNING)
            StatusBadge(text = "Rechazado", type = StatusBadgeType.DANGER)
            StatusBadge(text = "En ruta", type = StatusBadgeType.INFO)
            StatusBadge(text = "Pendiente", type = StatusBadgeType.NEUTRAL)
            StatusBadge(text = "Listo para cargar", type = StatusBadgeType.HIGHLIGHT)
        }
    }
}
