package com.rebootech.fruitlogix.app.dashboard.presentation.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.app.dashboard.domain.ActionItem
import com.rebootech.fruitlogix.app.dashboard.domain.ActionItemStyle
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.Spacing

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
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs)
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
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleLarge,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight,
                    fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${items.size}",
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.action_center_clear_all),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClearAllClick)
            )
        }

        Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

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
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Input,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.06f),
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
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
                        .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = item.iconRes,
                        contentDescription = null,
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = item.titleRes),
                            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyLarge,
                            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (item.hasRedDot) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = item.subtitleRes),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = item.captionRes),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

            // Action button stays on right with fixed width
            Box(
                modifier = Modifier.width(120.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (item.style == ActionItemStyle.LIME_BUTTON) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.PrimaryButton(
                        text = stringResource(id = item.buttonTextRes),
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
                            .clickable(onClick = onActionClick)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = item.buttonTextRes),
                            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
                            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
