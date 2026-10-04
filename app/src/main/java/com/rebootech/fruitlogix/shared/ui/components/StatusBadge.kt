package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

enum class StatusBadgeType {
    SUCCESS,
    WARNING,
    DANGER,
    INFO,
    NEUTRAL
}

/**
 * Status Badge component: pill shape, uppercase small text, translucent tinted background.
 *
 * @param text Badge text
 * @param type Semantic state type (SUCCESS, WARNING, DANGER, INFO, NEUTRAL)
 * @param modifier Modifier to apply
 * @param icon Optional leading icon composable
 */
@Composable
fun StatusBadge(
    text: String,
    type: com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    horizontalPadding: androidx.compose.ui.unit.Dp = 10.dp,
    verticalPadding: androidx.compose.ui.unit.Dp = 4.dp,
    icon: (@Composable () -> Unit)? = null
) {
    val (backgroundColor, textColor) = when (type) {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS -> Pair(
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success.copy(alpha = 0.18f),
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success
        )
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING -> Pair(
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning.copy(alpha = 0.18f),
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
        )
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER -> Pair(
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.18f),
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
        )
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.INFO -> Pair(
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.info.copy(alpha = 0.18f),
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.info
        )
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.NEUTRAL -> Pair(
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted.copy(alpha = 0.18f),
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
        )
    }

    Box(
        modifier = modifier
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = maxLines,
                softWrap = softWrap
            )
        }
    }
}

@Preview
@Composable
private fun StatusBadgePreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = "Delivered",
                type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS
            )
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = "Customs Delay",
                type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING
            )
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = "Overdue",
                type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER
            )
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = "In Transit",
                type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.INFO
            )
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = "Draft",
                type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.NEUTRAL
            )
        }
    }
}
