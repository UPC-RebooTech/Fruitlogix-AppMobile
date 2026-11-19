package com.rebootech.fruitlogix.shared.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Light section card with a title, used by detail screens. */
@Composable
fun DetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FruitLogixCard(variant = CardVariant.White, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

/** Icon box + label (small, muted) + value. Value can use the data font. */
@Composable
fun DetailRow(
    @DrawableRes iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = FruitLogixTheme.typography.bodyLarge
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.iconBox)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = FruitLogixTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = valueStyle,
                color = scheme.onSurface
            )
        }
    }
}
