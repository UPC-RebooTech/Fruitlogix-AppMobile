package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.harvestStatusBadge
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.harvestStatusLabel

/**
 * Next planned harvest (light list card).
 * Date and status are formatted from data (LocalDateTime + HarvestStatus) in the selected language.
 */
@Composable
fun UpcomingHarvestCard(
    harvest: PlannedHarvest?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (harvest == null) return
    val scheme = MaterialTheme.colorScheme

    FruitLogixCard(
        variant = CardVariant.White,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(FruitLogixTheme.spacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(FruitLogixTheme.shapes.IconBox)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_leaf,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
            }

            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.producer_home_next_harvest).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                Text(
                    text = harvest.productName,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Text(
                    text = "${formatRelativeDateTime(harvest.harvestedAt)} · ${stringResource(R.string.producer_home_harvest_expected, harvest.expectedKg)}",
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                StatusBadge(
                    text = stringResource(harvestStatusLabel(harvest.status)),
                    type = harvestStatusBadge(harvest.status),
                    maxLines = 1,
                    softWrap = false
                )
                if (!harvest.crewName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                    Text(
                        text = harvest.crewName,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
