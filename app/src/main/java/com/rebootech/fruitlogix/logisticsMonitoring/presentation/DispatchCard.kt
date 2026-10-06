package com.rebootech.fruitlogix.logisticsMonitoring.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchSummary
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixShapeTokens
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.Spacing

/**
 * Dispatch Card component for the Active Dispatches list.
 *
 * @param dispatch Data model containing dispatch telemetry and status.
 * @param modifier Custom modifier for card container.
 * @param onClick Primary card click handler.
 * @param onDiagnosticClick Immediate diagnostic button handler (alert variant).
 */
@Composable
fun DispatchCard(
    dispatch: DispatchSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDiagnosticClick: () -> Unit = onClick
) {
    val isAlert = dispatch.status == DispatchStatus.TEMP_ALERT
    val isDelay = dispatch.status == DispatchStatus.WEIGH_STATION_DELAY

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = FruitLogixShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Thin red top border line for alert variant (FL-408)
            if (isAlert) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(FruitLogixTheme.colors.dangerStrong)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.sm)
            ) {
                // 1. Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Truck icon in a small rounded square
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FruitLogixTheme.colors.appbar),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(
                                id = if (isAlert) R.drawable.ic_logistics_alert else R.drawable.ic_logistics_truck,
                                contentDescription = null,
                                tint = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.xs))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.logistics_dispatch_unit, dispatch.id),
                                    style = TextStyle(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        lineHeight = 24.sp
                                    ),
                                    color = FruitLogixTheme.colors.textOnDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Plate chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FruitLogixTheme.colors.appbar)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dispatch.licensePlate,
                                        style = FruitLogixTheme.typography.bodySmall,
                                        color = FruitLogixTheme.colors.textMuted,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = dispatch.cargoDescription,
                                style = FruitLogixTheme.typography.bodySmall,
                                color = if (isAlert) FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.9f)
                                else FruitLogixTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status pill at top right (always on ONE line)
                    val (badgeText, badgeType) = when (dispatch.status) {
                        DispatchStatus.ON_TIME -> Pair(
                            stringResource(R.string.logistics_status_on_time),
                            StatusBadgeType.SUCCESS
                        )
                        DispatchStatus.TEMP_ALERT -> Pair(
                            stringResource(R.string.logistics_status_temp_alert),
                            StatusBadgeType.DANGER
                        )
                        DispatchStatus.WEIGH_STATION_DELAY -> Pair(
                            stringResource(R.string.logistics_status_weigh_delay),
                            StatusBadgeType.WARNING
                        )
                    }

                    StatusBadge(
                        text = badgeText,
                        type = badgeType,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Route row (rounded inner container, single line with ellipsis)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FruitLogixTheme.colors.appbar)
                        .padding(horizontal = Spacing.sm, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            id = R.drawable.ic_logistics_route,
                            contentDescription = null,
                            tint = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dispatch.routeLabel,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textOnDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Three metric tiles in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    // Tile 1: REEFER TEMP
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAlert) FruitLogixTheme.colors.danger.copy(alpha = 0.25f)
                                else FruitLogixTheme.colors.appbar
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.logistics_label_reefer_temp),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            AppIcon(
                                id = if (isAlert) R.drawable.ic_logistics_alert else R.drawable.ic_logistics_snowflake,
                                contentDescription = null,
                                tint = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dispatch.reeferTemp,
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                lineHeight = 24.sp
                            ),
                            color = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = dispatch.reeferTempTarget,
                            style = FruitLogixTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = FruitLogixTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Tile 2: HUMIDITY
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FruitLogixTheme.colors.appbar)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.logistics_label_humidity),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = FruitLogixTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            AppIcon(
                                id = R.drawable.ic_logistics_drop,
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val humidityParts = dispatch.humidity.split(" ")
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = humidityParts.firstOrNull() ?: dispatch.humidity,
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp
                                ),
                                color = FruitLogixTheme.colors.textOnDark,
                                maxLines = 1
                            )
                            if (humidityParts.size > 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = humidityParts.drop(1).joinToString(" "),
                                    style = TextStyle(
                                        fontFamily = RobotoFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    ),
                                    color = FruitLogixTheme.colors.textMuted,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Spacer to keep baseline alignment with adjacent tiles
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Tile 3: DOCK ETA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FruitLogixTheme.colors.appbar)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.logistics_label_dock_eta),
                                style = FruitLogixTheme.typography.labelSmall,
                                color = if (isAlert) FruitLogixTheme.colors.dangerStrong
                                else if (isDelay) FruitLogixTheme.colors.warning
                                else FruitLogixTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            AppIcon(
                                id = if (isDelay) R.drawable.ic_logistics_hourglass else R.drawable.ic_logistics_clock,
                                contentDescription = null,
                                tint = if (isAlert) FruitLogixTheme.colors.dangerStrong
                                else if (isDelay) FruitLogixTheme.colors.warning
                                else FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val etaParts = dispatch.dockEta.split(" ")
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = etaParts.firstOrNull() ?: dispatch.dockEta,
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp
                                ),
                                color = if (isAlert) FruitLogixTheme.colors.dangerStrong
                                else if (isDelay) FruitLogixTheme.colors.warning
                                else FruitLogixTheme.colors.textOnDark,
                                maxLines = 1
                            )
                            if (etaParts.size > 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = etaParts.drop(1).joinToString(" "),
                                    style = TextStyle(
                                        fontFamily = RobotoFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isAlert) FruitLogixTheme.colors.dangerStrong
                                    else if (isDelay) FruitLogixTheme.colors.warning
                                    else FruitLogixTheme.colors.textMuted,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (dispatch.etaDelta != null) {
                            Text(
                                text = dispatch.etaDelta,
                                style = FruitLogixTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = FruitLogixTheme.colors.warning,
                                maxLines = 1
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Progress row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAlert || isDelay) {
                        Text(
                            text = dispatch.progressLabel,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "${(dispatch.progressFraction * 100).toInt()}%",
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textOnDark,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.logistics_transit_progress,
                                (dispatch.progressFraction * 100).toInt()
                            ),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                        Text(
                            text = dispatch.progressLabel,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LimeProgressBar(
                    progress = dispatch.progressFraction,
                    progressColor = if (isAlert) FruitLogixTheme.colors.dangerStrong
                    else FruitLogixTheme.colors.primary,
                    trackColor = FruitLogixTheme.colors.appbar
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Footer row / 6. Alert variant button
                if (dispatch.showImmediateDiagnostic) {
                    Button(
                        onClick = onDiagnosticClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = FruitLogixShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FruitLogixTheme.colors.dangerStrong,
                            contentColor = FruitLogixTheme.colors.textOnDark
                        )
                    ) {
                        AppIcon(
                            id = R.drawable.ic_diagnostic,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.textOnDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.logistics_immediate_reefer_diagnostic),
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = FruitLogixTheme.colors.textOnDark
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dispatch.footerNote?.let { note ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(
                                    id = if (isDelay) R.drawable.ic_shield_check else R.drawable.ic_logistics_signal,
                                    contentDescription = null,
                                    tint = if (isDelay) FruitLogixTheme.colors.warning else FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = note,
                                    style = FruitLogixTheme.typography.bodySmall,
                                    color = if (isDelay) FruitLogixTheme.colors.textOnDark else FruitLogixTheme.colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.logistics_view_dispatch_detail),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onClick)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Previews for On-time, Alert and Delay states
// =============================================================================

@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3)
@Composable
private fun DispatchCardOnTimePreview() {
    FruitLogixTheme {
        DispatchCard(
            dispatch = DispatchSummary(
                id = "FL-102",
                licensePlate = "BQK-482",
                cargoDescription = "Ica grapes \u2022 16 Pallets (Grade A)",
                routeLabel = "Ica \u2192 Lima Central Hub",
                reeferTemp = "3.4\u00b0C",
                reeferTempTarget = "Target 3.0\u00b0C",
                humidity = "88% RH",
                dockEta = "14:30 PET",
                etaDelta = null,
                progressFraction = 0.72f,
                progressLabel = "284 / 395 km",
                status = DispatchStatus.ON_TIME,
                footerNote = "BLE Sensatag Sync OK",
                showImmediateDiagnostic = false
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3)
@Composable
private fun DispatchCardAlertPreview() {
    FruitLogixTheme {
        DispatchCard(
            dispatch = DispatchSummary(
                id = "FL-408",
                licensePlate = "AZT-901",
                cargoDescription = "Hass avocado \u2022 18 Pallets",
                routeLabel = "Chav\u00edn de Hu\u00e1ntar \u2192 Callao Cold Hub",
                reeferTemp = "3.2\u00b0C",
                reeferTempTarget = "Target 4.0\u00b0C",
                humidity = "84% RH",
                dockEta = "14:30 PET",
                etaDelta = "+12m",
                progressFraction = 0.82f,
                progressLabel = "182 / 220 km \u2022 +12m delay",
                status = DispatchStatus.TEMP_ALERT,
                footerNote = null,
                showImmediateDiagnostic = true
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3)
@Composable
private fun DispatchCardDelayPreview() {
    FruitLogixTheme {
        DispatchCard(
            dispatch = DispatchSummary(
                id = "FL-219",
                licensePlate = "CHD-330",
                cargoDescription = "Piura mango \u2022 20 Pallets",
                routeLabel = "Piura \u2192 Lima Central Hub",
                reeferTemp = "2.1\u00b0C",
                reeferTempTarget = "Nominal",
                humidity = "91% RH",
                dockEta = "16:45 PET",
                etaDelta = "+45m",
                progressFraction = 0.40f,
                progressLabel = "112 / 280 km \u2022 Weigh station delay",
                status = DispatchStatus.WEIGH_STATION_DELAY,
                footerNote = "Weigh Station Hold",
                showImmediateDiagnostic = false
            )
        )
    }
}
