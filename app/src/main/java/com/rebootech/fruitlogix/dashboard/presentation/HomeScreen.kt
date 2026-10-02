package com.rebootech.fruitlogix.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.dashboard.domain.CriticalAlert
import com.rebootech.fruitlogix.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.dashboard.domain.KpiBadgeType
import com.rebootech.fruitlogix.dashboard.domain.KpiData
import com.rebootech.fruitlogix.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.dashboard.domain.PriorityActionStyle
import com.rebootech.fruitlogix.dashboard.domain.SegmentedBarSegment
import com.rebootech.fruitlogix.ui.components.AppIcon
import com.rebootech.fruitlogix.ui.components.AppLanguage
import com.rebootech.fruitlogix.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.ui.components.PrimaryButton
import com.rebootech.fruitlogix.ui.components.StatusBadge
import com.rebootech.fruitlogix.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.ui.theme.Spacing

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    HomeScreenContent(
        state = state,
        onLanguageSelected = viewModel::onLanguageSelected,
        modifier = modifier
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
    ) {
        // 1. Top App Bar
        item {
            FruitLogixTopBar(
                title = stringResource(R.string.topbar_title),
                subtitle = stringResource(R.string.topbar_subtitle),
                selectedLanguage = state.selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onNotificationClick = {},
                onProfileClick = {}
            )
        }

        // 2. Greeting Section
        item {
            state.greeting?.let { greeting ->
                GreetingSection(greeting = greeting)
            }
        }

        // 3. Priority Actions
        item {
            PriorityActionsSection(
                actions = state.priorityActions,
                readyCount = state.priorityReadyCount
            )
        }

        // 4. Critical Alert
        item {
            state.criticalAlert?.let { alert ->
                CriticalAlertCard(alert = alert)
            }
        }

        // 5. KPI Grid 2×2
        item {
            KpiGrid(kpis = state.kpis)
        }

        // Bottom spacer for nav bar clearance
        item {
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

// =============================================================================
// Section 2: Greeting
// =============================================================================
@Composable
private fun GreetingSection(greeting: GreetingInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
    ) {
        // Date line with green dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.success)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "${greeting.dateLine} • ${greeting.shiftLabel}",
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textOnLight
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Welcome line + sensor button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.greeting_welcome, greeting.userName),
                    style = FruitLogixTheme.typography.displayLarge,
                    color = FruitLogixTheme.colors.textOnLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greeting.hubDescription,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
            // Sensor icon button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FruitLogixTheme.colors.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_sensor_waves,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.textOnDark,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// =============================================================================
// Section 3: Priority Actions
// =============================================================================
@Composable
private fun PriorityActionsSection(
    actions: List<PriorityAction>,
    readyCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.priority_actions_label),
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textMuted,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.priority_ready_count, readyCount),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Horizontal scroll of action cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actions) { action ->
                PriorityActionCard(action = action)
            }
        }
    }
}

@Composable
private fun PriorityActionCard(action: PriorityAction) {
    val (bgColor, contentColor, iconBgColor) = when (action.style) {
        PriorityActionStyle.DARK -> Triple(
            FruitLogixTheme.colors.surfaceDark,
            FruitLogixTheme.colors.textOnDark,
            FruitLogixTheme.colors.appbar
        )
        PriorityActionStyle.LIME -> Triple(
            FruitLogixTheme.colors.primary,
            FruitLogixTheme.colors.onPrimary,
            FruitLogixTheme.colors.onPrimary.copy(alpha = 0.15f)
        )
        PriorityActionStyle.DANGER -> Triple(
            FruitLogixTheme.colors.danger,
            FruitLogixTheme.colors.textOnDark,
            FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.25f)
        )
    }

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = bgColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.sm),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = action.iconRes,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = action.title,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = action.subtitle,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// =============================================================================
// Section 4: Critical Alert Card
// =============================================================================
@Composable
private fun CriticalAlertCard(alert: CriticalAlert) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.danger,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            // Top: Title + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_thermostat,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.textOnDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = alert.title,
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(
                    text = alert.badgeText,
                    type = StatusBadgeType.DANGER
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Unit label + temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = alert.unitLabel,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.temperatureValue,
                    style = FruitLogixExtraTypography.kpiNumber,
                    color = FruitLogixTheme.colors.dangerStrong,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Route description
            Text(
                text = alert.routeDescription,
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textOnDark.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Bottom row: location + action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_navigation,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.textOnDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.locationLine,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textOnDark.copy(alpha = 0.8f)
                    )
                }
                PrimaryButton(
                    text = alert.actionButtonText,
                    onClick = {},
                    trailingIcon = {
                        AppIcon(
                            id = R.drawable.ic_arrow_forward,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

// =============================================================================
// Section 5: KPI Grid 2x2
// =============================================================================
@Composable
private fun KpiGrid(kpis: List<KpiData>) {
    if (kpis.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        kpis.chunked(2).forEach { rowKpis ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowKpis.forEach { kpi ->
                    Box(modifier = Modifier.weight(1f)) {
                        KpiTile(kpi = kpi)
                    }
                }
                // Fill with empty space if odd count
                if (rowKpis.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KpiTile(kpi: KpiData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.sm)
        ) {
            // Caption + icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kpi.caption.uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = kpi.iconRes,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Big value
            Text(
                text = kpi.value,
                style = FruitLogixExtraTypography.kpiNumber,
                color = FruitLogixTheme.colors.textOnDark,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer area
            kpi.deltaText?.let { delta ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_chart_trend,
                        contentDescription = null,
                        tint = if (kpi.deltaIsPositive) FruitLogixTheme.colors.success else FruitLogixTheme.colors.dangerStrong,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusBadge(
                        text = delta,
                        type = if (kpi.deltaIsPositive) StatusBadgeType.SUCCESS else StatusBadgeType.DANGER
                    )
                }
            }

            kpi.progress?.let { progressVal ->
                Spacer(modifier = Modifier.height(Spacing.xs))
                LimeProgressBar(progress = progressVal)
            }

            kpi.footerLine1?.let { line1 ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line1,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            kpi.footerBadgeText?.let { badgeText ->
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(
                    text = badgeText,
                    type = when (kpi.footerBadgeType) {
                        KpiBadgeType.SUCCESS -> StatusBadgeType.SUCCESS
                        KpiBadgeType.WARNING -> StatusBadgeType.WARNING
                        KpiBadgeType.DANGER -> StatusBadgeType.DANGER
                        KpiBadgeType.INFO -> StatusBadgeType.INFO
                        KpiBadgeType.NEUTRAL -> StatusBadgeType.NEUTRAL
                    }
                )
            }

            kpi.footerLine2?.let { line2 ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = line2,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            kpi.segmentedBarSegments?.let { segments ->
                Spacer(modifier = Modifier.height(Spacing.xs))
                SegmentedBar(segments = segments)
            }
        }
    }
}

@Composable
private fun SegmentedBar(segments: List<SegmentedBarSegment>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(FruitLogixTheme.shapes.Pill),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.forEach { segment ->
            val color = when (segment.type) {
                KpiBadgeType.DANGER -> FruitLogixTheme.colors.dangerStrong
                KpiBadgeType.WARNING -> FruitLogixTheme.colors.warning
                KpiBadgeType.SUCCESS -> FruitLogixTheme.colors.success
                KpiBadgeType.INFO -> FruitLogixTheme.colors.info
                KpiBadgeType.NEUTRAL -> FruitLogixTheme.colors.textMuted.copy(alpha = 0.3f)
            }
            Box(
                modifier = Modifier
                    .weight(segment.weight)
                    .height(6.dp)
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(color)
            )
        }
    }
}

// =============================================================================
// Preview
// =============================================================================
@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    val previewState = HomeUiState(
        isLoading = false,
        greeting = GreetingInfo(
            dateLine = "Thursday, Oct 1, 2026",
            shiftLabel = "Active Morning Shift",
            userName = "Carlos",
            hubDescription = "Fruit Distribution Tactical Hub • Pacific Sector"
        ),
        priorityActions = listOf(
            PriorityAction("Assign Producer", "Link new lots", R.drawable.ic_assign_producer, PriorityActionStyle.DARK),
            PriorityAction("Dispatch Fleet", "3 reefers ready", R.drawable.ic_dispatch_truck, PriorityActionStyle.LIME),
            PriorityAction("Alerts", "2 anomalies", R.drawable.ic_alert_diamond, PriorityActionStyle.DANGER)
        ),
        priorityReadyCount = 3,
        criticalAlert = CriticalAlert(
            title = "CRITICAL THERMAL DEVIATION",
            badgeText = "IMMEDIATE ACTION",
            unitLabel = "Unit FL-408",
            temperatureValue = "6.8°C",
            routeDescription = "Michoacán Hass • Max limit: 4.0°C (+2.8°C rising)",
            locationLine = "KM 184 • Toluca Expy",
            actionButtonText = "REVIEW REEFER"
        ),
        kpis = listOf(
            KpiData("Active Orders", R.drawable.ic_orders, "48", "+12.5% vs yesterday", true, 0.72f),
            KpiData("Daily Deliveries", R.drawable.ic_verified, "32", footerLine1 = "Avg ETA: 42 min", footerBadgeText = "On time", footerBadgeType = KpiBadgeType.SUCCESS),
            KpiData("Fruit Quality", R.drawable.ic_shield_check, "98.4%", footerLine1 = "Last 200 lots", footerLine2 = "Grade A Certified"),
            KpiData(
                "IoT Alerts", R.drawable.ic_sensor_waves, "03",
                footerLine1 = "2 Critical • 1 Warning",
                segmentedBarSegments = listOf(
                    SegmentedBarSegment(2f, KpiBadgeType.DANGER),
                    SegmentedBarSegment(1f, KpiBadgeType.WARNING)
                )
            )
        )
    )

    FruitLogixTheme {
        HomeScreenContent(
            state = previewState,
            onLanguageSelected = {}
        )
    }
}
