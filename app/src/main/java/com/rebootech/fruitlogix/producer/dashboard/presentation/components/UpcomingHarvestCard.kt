package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun UpcomingHarvestCard(
    harvest: PlannedHarvest?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (harvest == null) return

    FruitLogixCard(
        variant = CardVariant.Tint,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROXIMA COSECHA",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Bold
                )

                AppIcon(
                    id = R.drawable.ic_schedule,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.textOnLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${harvest.productName} • ${harvest.dateFormatted} • ~${harvest.expectedKg} kg",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.textOnLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    text = harvest.statusText,
                    type = StatusBadgeType.NEUTRAL,
                    maxLines = 1,
                    softWrap = false
                )

                if (!harvest.crewName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = harvest.crewName,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

