package com.rebootech.fruitlogix.ui.components

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
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme

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
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val (backgroundColor, textColor) = when (type) {
        StatusBadgeType.SUCCESS -> Pair(
            FruitLogixTheme.colors.success.copy(alpha = 0.18f),
            FruitLogixTheme.colors.success
        )
        StatusBadgeType.WARNING -> Pair(
            FruitLogixTheme.colors.warning.copy(alpha = 0.18f),
            FruitLogixTheme.colors.warning
        )
        StatusBadgeType.DANGER -> Pair(
            FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.18f),
            FruitLogixTheme.colors.dangerStrong
        )
        StatusBadgeType.INFO -> Pair(
            FruitLogixTheme.colors.info.copy(alpha = 0.18f),
            FruitLogixTheme.colors.info
        )
        StatusBadgeType.NEUTRAL -> Pair(
            FruitLogixTheme.colors.textMuted.copy(alpha = 0.18f),
            FruitLogixTheme.colors.textMuted
        )
    }

    Box(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
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
                style = FruitLogixTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
private fun StatusBadgePreview() {
    FruitLogixTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(text = "Delivered", type = StatusBadgeType.SUCCESS)
            StatusBadge(text = "Customs Delay", type = StatusBadgeType.WARNING)
            StatusBadge(text = "Overdue", type = StatusBadgeType.DANGER)
            StatusBadge(text = "In Transit", type = StatusBadgeType.INFO)
            StatusBadge(text = "Draft", type = StatusBadgeType.NEUTRAL)
        }
    }
}
