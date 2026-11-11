package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Filter chip used across list screens: Bosque (secondary) when selected,
 * surfaceContainerHigh otherwise. 48dp touch target, single line.
 */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(if (selected) scheme.secondary else scheme.surfaceContainerHigh)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = if (selected) scheme.onSecondary else scheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}
