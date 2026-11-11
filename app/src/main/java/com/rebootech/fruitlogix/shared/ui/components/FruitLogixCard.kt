package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Card rule:
 * - [Dark] (inverseSurface): only KPI tiles and highlighted blocks (e.g. the next action).
 * - [White] (surfaceContainerLowest) / [Light] (surfaceContainerLow): lists and regular content.
 * - [Tint] (secondaryContainer): soft secondary highlights.
 * All variants use theme roles, so a future dark theme needs no screen changes.
 */
enum class CardVariant {
    /** inverseSurface. KPI tiles and highlighted blocks only. */
    Dark,

    /** surfaceContainerLow. Grouped content. */
    Light,

    /** surfaceContainerLowest with hairline border. Default for lists and detail blocks. */
    White,

    /** secondaryContainer. Soft secondary highlights. */
    Tint
}

/**
 * Universal card for the FruitLogix design system.
 * Flat by default: hierarchy comes from surface tone, not shadows.
 *
 * @param variant Visual variant (Dark, Light, White, Tint)
 * @param shape Card shape (defaults to 18dp; use FruitLogixTheme.shapes.HeroCard for hero blocks)
 */
@Composable
fun FruitLogixCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Dark,
    shape: Shape = FruitLogixTheme.shapes.Card,
    border: BorderStroke? = null,
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (variant) {
        CardVariant.Dark -> scheme.inverseSurface to scheme.inverseOnSurface
        CardVariant.Light -> scheme.surfaceContainerLow to scheme.onSurface
        CardVariant.White -> scheme.surfaceContainerLowest to scheme.onSurface
        CardVariant.Tint -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }

    val resolvedBorder = border ?: if (variant == CardVariant.White) {
        BorderStroke(1.dp, scheme.outlineVariant)
    } else {
        null
    }

    Card(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = shape,
        border = resolvedBorder,
        elevation = elevation,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(content = content)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7FAF6)
@Composable
private fun FruitLogixCardPreview() {
    FruitLogixTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CardVariant.entries.forEach { variant ->
                FruitLogixCard(
                    variant = variant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text("${variant.name} card", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
