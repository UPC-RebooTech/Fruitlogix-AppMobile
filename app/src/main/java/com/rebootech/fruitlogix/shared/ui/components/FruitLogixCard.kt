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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

enum class CardVariant {
    Dark,
    Light,
    White,
    Tint
}

/**
 * Universal Card component for FruitLogix Design System supporting Dark, Light, White and Tint surface variants.
 *
 * @param modifier Modifier to apply to the card container
 * @param variant Visual variant (Dark, Light, White, Tint)
 * @param shape Card shape (defaults to FruitLogixTheme.shapes.Card 24dp)
 * @param border Optional border stroke
 * @param elevation Card elevation specification
 * @param onClick Optional click handler
 * @param content Inner composable content slot with ColumnScope
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
    val (containerColor, contentColor) = when (variant) {
        CardVariant.Dark -> Pair(
            FruitLogixTheme.colors.surfaceDark,
            FruitLogixTheme.colors.textOnDark
        )
        CardVariant.Light -> Pair(
            FruitLogixTheme.colors.surfaceLight,
            FruitLogixTheme.colors.textOnLight
        )
        CardVariant.White -> Pair(
            FruitLogixTheme.colors.white,
            FruitLogixTheme.colors.textOnLight
        )
        CardVariant.Tint -> Pair(
            FruitLogixTheme.colors.surfaceTint,
            FruitLogixTheme.colors.textOnLight
        )
    }

    val resolvedBorder = border ?: if (variant == CardVariant.Tint) {
        BorderStroke(1.dp, FruitLogixTheme.colors.primary.copy(alpha = 0.35f))
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

@Preview
@Composable
private fun FruitLogixCardPreview() {
    FruitLogixTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FruitLogixCard(
                variant = CardVariant.Dark,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text("Dark Card", modifier = Modifier.padding(16.dp))
            }

            FruitLogixCard(
                variant = CardVariant.Light,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text("Light Card", modifier = Modifier.padding(16.dp))
            }

            FruitLogixCard(
                variant = CardVariant.White,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text("White Card", modifier = Modifier.padding(16.dp))
            }

            FruitLogixCard(
                variant = CardVariant.Tint,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tint Card", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
