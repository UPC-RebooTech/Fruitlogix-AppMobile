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
import com.rebootech.fruitlogix.dashboard.domain.FleetStatusType
import com.rebootech.fruitlogix.dashboard.domain.FleetUnitSummary
import com.rebootech.fruitlogix.ui.components.AppIcon
import com.rebootech.fruitlogix.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.ui.theme.Spacing

@Composable
fun LiveFleetSection(
    fleetUnits: List<FleetUnitSummary>,
    inTransitCount: Int,
    modifier: Modifier = Modifier,
    onMapViewClick: () -> Unit = {},
    onFleetUnitClick: (String) -> Unit = {}
) {
    if (fleetUnits.isEmpty()) return

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
                    text = stringResource(R.string.fleet_title),
                    style = FruitLogixTheme.typography.titleLarge,
                    color = FruitLogixTheme.colors.textOnLight,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.fleet_in_transit_count, inTransitCount),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            Text(
                text = stringResource(R.string.fleet_map_view),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onMapViewClick)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Fleet Unit Cards (Max 3)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            fleetUnits.take(3).forEach { unit ->
                FleetUnitCard(
                    unit = unit,
                    onClick = {
                        // TODO: Navigate to fleet detail screen
                        onFleetUnitClick(unit.unitId)
                    }
                )
            }
        }
    }
}

@Composable
private fun FleetUnitCard(
    unit: FleetUnitSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            // Header row: FL-102 • Model + status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = unit.unitId,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.primary,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${unit.truckModel}",
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = FruitLogixTheme.colors.textOnDark.copy(alpha = 0.8f)
                    )
                }

                val statusDotColor = if (unit.statusType == FleetStatusType.ON_TIME) {
                    FruitLogixTheme.colors.success
                } else {
                    FruitLogixTheme.colors.dangerStrong
                }
                val statusTextColor = if (unit.statusType == FleetStatusType.ON_TIME) {
                    FruitLogixTheme.colors.success
                } else {
                    FruitLogixTheme.colors.dangerStrong
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = unit.statusTextRes),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Sub-line: Route description
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    id = R.drawable.ic_navigation,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit.routeDescription,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // 3 Telemetry Columns: REEFER TEMP, HUMIDITY, DEST ETA / DELAY DELTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.fleet_label_reefer_temp),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = unit.reeferTemp,
                        style = FruitLogixExtraTypography.kpiNumber,
                        color = FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.fleet_label_humidity),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = unit.humidity,
                        style = FruitLogixExtraTypography.kpiNumber,
                        color = FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    val labelRes = if (unit.isDelay) R.string.fleet_label_delay_delta else R.string.fleet_label_dest_eta
                    val valColor = if (unit.isDelay) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textOnDark
                    Text(
                        text = stringResource(labelRes),
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = unit.destEtaOrDelay,
                        style = FruitLogixExtraTypography.kpiNumber,
                        color = valColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Mileage + Progress Bar line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.fleet_mileage_format, unit.mileageText),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
                Text(
                    text = unit.progressLabel,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LimeProgressBar(progress = unit.progressPercent)
        }
    }
}
