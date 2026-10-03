package com.rebootech.fruitlogix.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.dashboard.domain.ActionItem
import com.rebootech.fruitlogix.dashboard.domain.ActionItemStyle
import com.rebootech.fruitlogix.ui.components.AppIcon
import com.rebootech.fruitlogix.ui.components.PrimaryButton
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.ui.theme.Spacing

@Composable
fun ActionCenterSection(
    items: List<ActionItem>,
    modifier: Modifier = Modifier,
    onClearAllClick: () -> Unit = {},
    onActionClick: (String) -> Unit = {}
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.action_center_title),
                    style = FruitLogixTheme.typography.titleLarge,
                    color = FruitLogixTheme.colors.textOnLight,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.surfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${items.size}",
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.action_center_clear_all),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClearAllClick)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Action Items (Max 2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(2).forEach { item ->
                ActionItemCard(
                    item = item,
                    onActionClick = { onActionClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ActionItemCard(
    item: ActionItem,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Input,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.06f),
            contentColor = FruitLogixTheme.colors.textOnLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Dark icon box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FruitLogixTheme.colors.surfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = item.iconRes,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.xs))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = item.titleRes),
                            style = FruitLogixTheme.typography.bodyLarge,
                            color = FruitLogixTheme.colors.textOnLight,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.hasRedDot) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FruitLogixTheme.colors.dangerStrong)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = item.subtitleRes),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = item.captionRes),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.xs))

            // Button (Lime or Dark)
            if (item.style == ActionItemStyle.LIME_BUTTON) {
                PrimaryButton(
                    text = stringResource(id = item.buttonTextRes),
                    onClick = onActionClick
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(FruitLogixTheme.shapes.Pill)
                        .background(FruitLogixTheme.colors.surfaceDark)
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = Spacing.sm, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = item.buttonTextRes),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
