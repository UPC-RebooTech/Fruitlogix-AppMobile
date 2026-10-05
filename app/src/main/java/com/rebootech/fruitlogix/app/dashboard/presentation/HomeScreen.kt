package com.rebootech.fruitlogix.app.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.app.dashboard.data.FakeDashboardRepository
import com.rebootech.fruitlogix.app.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.app.dashboard.domain.KpiBadgeType
import com.rebootech.fruitlogix.app.dashboard.domain.KpiData
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.PredictiveAlert
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.PredictiveAlertStatus
import com.rebootech.fruitlogix.app.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.app.dashboard.domain.PriorityActionStyle
import com.rebootech.fruitlogix.app.dashboard.domain.SegmentedBarSegment
import com.rebootech.fruitlogix.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.app.dashboard.presentation.components.ActionCenterSection
import com.rebootech.fruitlogix.qualityControl.presentation.ComplianceCard
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.LiveFleetSection
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.Spacing

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
    onLanguageSelected: (com.rebootech.fruitlogix.shared.ui.components.AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.bg)
    ) {
        // 1. Top App Bar
        item {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar(
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

        // 3. Priority Actions (Compact Row of 3 Pill Chips)
        item {
            PriorityActionsSection(
                actions = state.priorityActions,
                readyCount = state.priorityReadyCount
            )
        }

        // 4. Predictive Alert Card
        item {
            state.predictiveAlert?.let { alert ->
                PredictiveAlertCard(alert = alert)
            }
        }

        // 5. KPI Grid 2×2
        item {
            KpiGrid(kpis = state.kpis)
        }

        // 6. Action Center
        item {
            ActionCenterSection(
                items = state.actionItems,
                onClearAllClick = {},
                onActionClick = {}
            )
        }

        // 7. Live Fleet
        item {
            LiveFleetSection(
                fleetUnits = state.fleetUnits,
                inTransitCount = state.fleetInTransitCount,
                onMapViewClick = {},
                onFleetUnitClick = {}
            )
        }

        // 8. Cold-Chain Compliance
        item {
            state.complianceSummary?.let { summary ->
                ComplianceCard(summary = summary)
            }
        }

        // Bottom spacer so bottom navigation bar never covers the last card
        item {
            Spacer(modifier = Modifier.height(88.dp))
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
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)
    ) {
        // Date line with green dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success)
            )
            Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
            Text(
                text = "${greeting.dateLine} • ${greeting.shiftLabel}",
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight
            )
        }

        Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

        // Welcome line + sensor button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.greeting_welcome, greeting.userName),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.displayLarge,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greeting.hubDescription,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
                )
            }
            // Sensor icon button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_sensor_waves,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// =============================================================================
// Section 3: Priority Actions (Compact 3-Pill Chip Row)
// =============================================================================
@Composable
private fun PriorityActionsSection(
    actions: List<PriorityAction>,
    readyCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.priority_actions_label),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.priority_ready_count, readyCount),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

        // Compact horizontal row of 3 pill chips (min height 48dp)
        LazyRow(
            contentPadding = PaddingValues(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actions) { action ->
                PriorityActionChip(action = action)
            }
        }
    }
}

@Composable
private fun PriorityActionChip(action: PriorityAction) {
    val isLime = action.style == PriorityActionStyle.LIME
    val bgColor = if (isLime) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark
    val contentColor = if (isLime) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(bgColor)
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
            id = action.iconRes,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
        Text(
            text = stringResource(id = action.titleRes),
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =============================================================================
// Section 4: Predictive Alert Card
// =============================================================================
@Composable
private fun PredictiveAlertCard(alert: PredictiveAlert) {
    when (alert.status) {
        PredictiveAlertStatus.NOMINAL -> NominalAlertCard()
        PredictiveAlertStatus.BREACHED -> BreachedAlertCard(alert = alert)
        PredictiveAlertStatus.PREDICTIVE -> PredictiveAlertContentCard(alert = alert)
    }
}

@Composable
private fun NominalAlertCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_verified,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
            Text(
                text = stringResource(R.string.alert_nominal_title),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BreachedAlertCard(alert: PredictiveAlert) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.danger,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)) {
            // Overline title
            Row(verticalAlignment = Alignment.CenterVertically) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
                Text(
                    text = stringResource(R.string.alert_breached_label),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

            // Unit line + current temp
            Text(
                text = stringResource(R.string.alert_predictive_unit_line, alert.unitId, alert.cargoDescription),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${alert.currentCelsius}°C",
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography.kpiNumber,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.PrimaryButton(
                    text = stringResource(R.string.alert_review_reefer),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun PredictiveAlertContentCard(alert: PredictiveAlert) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)) {
            // Overline: PREDICTIVE ALERT (amber) + warning icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
                Text(
                    text = stringResource(R.string.alert_predictive_label),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Unit line
            Text(
                text = stringResource(R.string.alert_predictive_unit_line, alert.unitId, alert.cargoDescription),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

            // Big Poppins headline: "Will exceed 4.0°C in 12 min"
            Text(
                text = stringResource(
                    R.string.alert_predictive_headline,
                    "${alert.thresholdCelsius}°C",
                    alert.minutesToBreach
                ),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleLarge,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            )

            // Sub-line: "Rising 0.4°C/min • Now 3.2°C"
            val rateText = if (alert.ratePerMinute < 0.1) "0.4°C" else String.format("%.1f°C", alert.ratePerMinute)
            Text(
                text = stringResource(
                    R.string.alert_predictive_subline,
                    rateText,
                    "${alert.currentCelsius}°C"
                ),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

            // Canvas Sparkline
            val ceilingLabel = stringResource(R.string.alert_predictive_ceiling, "${alert.thresholdCelsius}°C")
            SparklineCanvas(
                readings = alert.readings,
                currentCelsius = alert.currentCelsius,
                thresholdCelsius = alert.thresholdCelsius,
                minutesToBreach = alert.minutesToBreach,
                ceilingLabel = ceilingLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

            // Bottom row: IMMEDIATE ACTION badge + Review reefer lime button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                    text = stringResource(R.string.alert_immediate_action),
                    type = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING
                )
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.PrimaryButton(
                    text = stringResource(R.string.alert_review_reefer),
                    onClick = {}
                )
            }
        }
    }
}

// =============================================================================
// Sparkline drawn with Compose Canvas (No third-party libraries)
// =============================================================================
@Composable
private fun SparklineCanvas(
    readings: List<TemperatureReading>,
    currentCelsius: Double,
    thresholdCelsius: Double,
    minutesToBreach: Int,
    ceilingLabel: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val lineColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
    val thresholdColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
    val dangerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong

    val labelStyle = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall.copy(color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted)
    val nowLabelStyle = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall.copy(
        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
    )
    val breachLabelStyle = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall.copy(
        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
        fontWeight = FontWeight.Bold,
        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
    )
    val xAxisStyle = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall.copy(
        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
        fontSize = 11.sp,
        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
    )

    val historyMinMinutesAgo = readings.maxOfOrNull { it.minutesAgo } ?: 45

    val nowLabelText = stringResource(R.string.alert_predictive_now_label, "${currentCelsius}°C")
    val plusMinText = stringResource(R.string.alert_predictive_plus_min, minutesToBreach)
    val minusMinText = stringResource(R.string.alert_predictive_minus_min, historyMinMinutesAgo)
    val nowCaptionText = stringResource(R.string.alert_predictive_now_caption)

    Canvas(modifier = modifier) {
        if (readings.isEmpty()) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height

        val totalMinutes = (historyMinMinutesAgo + minutesToBreach).toDouble()
        if (totalMinutes <= 0.0) return@Canvas

        val xAxisHeight = 20.dp.toPx()
        val topMargin = 22.dp.toPx()
        val leftMargin = 16.dp.toPx()
        val rightMargin = 48.dp.toPx()

        val usableWidth = canvasWidth - leftMargin - rightMargin
        val usableHeight = canvasHeight - topMargin - xAxisHeight

        val minTemp = (readings.minOfOrNull { it.celsius } ?: currentCelsius) - 0.4
        val maxTemp = thresholdCelsius + 0.6
        val tempRange = maxTemp - minTemp
        if (tempRange <= 0.0) return@Canvas

        fun getX(minutesAgo: Double): Float {
            val progress = (historyMinMinutesAgo - minutesAgo) / totalMinutes
            return (leftMargin + (progress * usableWidth)).toFloat()
        }

        fun getY(temp: Double): Float {
            val progress = (temp - minTemp) / tempRange
            return (topMargin + ((1.0 - progress) * usableHeight)).toFloat()
        }

        // 1. Dashed threshold ceiling line
        val thresholdY = getY(thresholdCelsius)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

        drawLine(
            color = thresholdColor,
            start = Offset(leftMargin, thresholdY),
            end = Offset(canvasWidth - rightMargin + 16.dp.toPx(), thresholdY),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )

        // Draw "Critical ceiling 4.0°C" label ABOVE threshold line at right end
        val ceilingLayout = textMeasurer.measure(
            text = ceilingLabel,
            style = labelStyle
        )
        val ceilingX = (canvasWidth - rightMargin + 16.dp.toPx() - ceilingLayout.size.width).coerceAtLeast(leftMargin)
        val ceilingY = thresholdY - ceilingLayout.size.height - 3.dp.toPx()
        drawText(
            textLayoutResult = ceilingLayout,
            topLeft = Offset(ceilingX, ceilingY)
        )

        // Sort readings from past to present
        val sortedReadings = readings.sortedByDescending { it.minutesAgo }

        // 2. Solid lime line for history
        val historyPath = Path().apply {
            sortedReadings.forEachIndexed { index, reading ->
                val px = getX(reading.minutesAgo.toDouble())
                val py = getY(reading.celsius)
                if (index == 0) {
                    moveTo(px, py)
                } else {
                    lineTo(px, py)
                }
            }
        }

        drawPath(
            path = historyPath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Current reading dot & label ("Now 3.2°C")
        val currentX = getX(0.0)
        val currentY = getY(currentCelsius)

        drawCircle(
            color = lineColor,
            radius = 5.dp.toPx(),
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color.Black,
            radius = 2.dp.toPx(),
            center = Offset(currentX, currentY)
        )

        val nowLayout = textMeasurer.measure(
            text = nowLabelText,
            style = nowLabelStyle
        )
        drawText(
            textLayoutResult = nowLayout,
            topLeft = Offset(
                currentX - (nowLayout.size.width / 2f),
                currentY + 6.dp.toPx()
            )
        )

        // 4. Dashed line for projection continuing trend until crossing threshold
        val breachX = getX(-minutesToBreach.toDouble())
        val breachY = getY(thresholdCelsius)

        drawLine(
            color = lineColor,
            start = Offset(currentX, currentY),
            end = Offset(breachX, breachY),
            strokeWidth = 3.dp.toPx(),
            pathEffect = dashEffect,
            cap = StrokeCap.Round
        )

        // 5. Small red dot where projection crosses threshold with "+12 min" label
        drawCircle(
            color = dangerColor,
            radius = 4.5.dp.toPx(),
            center = Offset(breachX, breachY)
        )

        val breachLayout = textMeasurer.measure(
            text = plusMinText,
            style = breachLabelStyle
        )
        drawText(
            textLayoutResult = breachLayout,
            topLeft = Offset(
                breachX + 6.dp.toPx(),
                breachY - (breachLayout.size.height / 2f)
            )
        )

        // 6. X-axis captions below chart: "-45 min", "Now", "+12 min" in Roboto 11sp textMuted
        val captionY = canvasHeight - xAxisHeight + 4.dp.toPx()

        val leftCapLayout = textMeasurer.measure(minusMinText, xAxisStyle)
        drawText(
            textLayoutResult = leftCapLayout,
            topLeft = Offset(leftMargin, captionY)
        )

        val nowCapLayout = textMeasurer.measure(nowCaptionText, xAxisStyle)
        drawText(
            textLayoutResult = nowCapLayout,
            topLeft = Offset(currentX - (nowCapLayout.size.width / 2f), captionY)
        )

        val rightCapLayout = textMeasurer.measure(plusMinText, xAxisStyle)
        drawText(
            textLayoutResult = rightCapLayout,
            topLeft = Offset(breachX - (rightCapLayout.size.width / 2f), captionY)
        )
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
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
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
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)
        ) {
            // Caption + icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kpi.caption.uppercase(),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = kpi.iconRes,
                        contentDescription = null,
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

            // Big value
            Text(
                text = kpi.value,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography.kpiNumber,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer area
            kpi.deltaText?.let { delta ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_chart_trend,
                        contentDescription = null,
                        tint = if (kpi.deltaIsPositive) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                        text = delta,
                        type = if (kpi.deltaIsPositive) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER,
                        maxLines = 1,
                        softWrap = false,
                        horizontalPadding = 6.dp,
                        verticalPadding = 2.dp
                    )
                }
            }

            kpi.progress?.let { progressVal ->
                Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar(
                    progress = progressVal
                )
            }

            kpi.footerLine1?.let { line1 ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line1,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                )
            }

            kpi.footerBadgeText?.let { badgeText ->
                Spacer(modifier = Modifier.height(4.dp))
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                    text = badgeText,
                    type = when (kpi.footerBadgeType) {
                        KpiBadgeType.SUCCESS -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS
                        KpiBadgeType.WARNING -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING
                        KpiBadgeType.DANGER -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER
                        KpiBadgeType.INFO -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.INFO
                        KpiBadgeType.NEUTRAL -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.NEUTRAL
                    }
                )
            }

            kpi.footerLine2?.let { line2 ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = line2,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                )
            }

            kpi.segmentedBarSegments?.let { segments ->
                Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
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
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.forEach { segment ->
            val color = when (segment.type) {
                KpiBadgeType.DANGER -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
                KpiBadgeType.WARNING -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
                KpiBadgeType.SUCCESS -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success
                KpiBadgeType.INFO -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.info
                KpiBadgeType.NEUTRAL -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted.copy(alpha = 0.3f)
            }
            Box(
                modifier = Modifier
                    .weight(segment.weight)
                    .height(6.dp)
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(color)
            )
        }
    }
}

// =============================================================================
// Previews for all 3 States
// =============================================================================
@Preview(name = "HomeScreen - Predictive State", showBackground = true, backgroundColor = 0xFFE8F3E3, widthDp = 390, heightDp = 1800)
@Composable
private fun HomeScreenPreview_Predictive() {
    val repo = FakeDashboardRepository()
    val data = repo.getDashboardData()
    val previewState = HomeUiState(
        isLoading = false,
        greeting = data.greeting,
        priorityActions = data.priorityActions,
        priorityReadyCount = 3,
        predictiveAlert = data.predictiveAlert,
        kpis = data.kpis,
        actionItems = data.actionItems,
        fleetUnits = data.fleetUnits,
        fleetInTransitCount = data.fleetInTransitCount,
        complianceSummary = data.complianceSummary
    )

    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        HomeScreenContent(
            state = previewState,
            onLanguageSelected = {}
        )
    }
}

@Preview(name = "HomeScreen - Nominal State", showBackground = true, backgroundColor = 0xFFE8F3E3, widthDp = 390, heightDp = 1800)
@Composable
private fun HomeScreenPreview_Nominal() {
    val repo = FakeDashboardRepository()
    val data = repo.getDashboardData()
    val previewState = HomeUiState(
        isLoading = false,
        greeting = data.greeting,
        priorityActions = data.priorityActions,
        priorityReadyCount = 3,
        predictiveAlert = repo.getNominalAlertData(),
        kpis = data.kpis,
        actionItems = data.actionItems,
        fleetUnits = data.fleetUnits,
        fleetInTransitCount = data.fleetInTransitCount,
        complianceSummary = data.complianceSummary
    )

    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        HomeScreenContent(
            state = previewState,
            onLanguageSelected = {}
        )
    }
}

@Preview(name = "HomeScreen - Breached State", showBackground = true, backgroundColor = 0xFFE8F3E3, widthDp = 390, heightDp = 1800)
@Composable
private fun HomeScreenPreview_Breached() {
    val repo = FakeDashboardRepository()
    val data = repo.getDashboardData()
    val previewState = HomeUiState(
        isLoading = false,
        greeting = data.greeting,
        priorityActions = data.priorityActions,
        priorityReadyCount = 3,
        predictiveAlert = repo.getBreachedAlertData(),
        kpis = data.kpis,
        actionItems = data.actionItems,
        fleetUnits = data.fleetUnits,
        fleetInTransitCount = data.fleetInTransitCount,
        complianceSummary = data.complianceSummary
    )

    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        HomeScreenContent(
            state = previewState,
            onLanguageSelected = {}
        )
    }
}
