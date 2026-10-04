package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Reusable Section Header component.
 *
 * @param title Section title text
 * @param modifier Modifier to apply
 * @param subtitle Optional subtitle description
 * @param actionText Optional clickable action text (e.g. "View All")
 * @param onActionClick Action text click callback
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleLarge,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight,
                fontWeight = FontWeight.SemiBold
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(start = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.sm, top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun SectionHeaderPreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.SectionHeader(
                title = "Active Fleet",
                subtitle = "12 trucks currently en route",
                actionText = "View All",
                onActionClick = {}
            )
        }
    }
}
