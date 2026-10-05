package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchSummary
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.FleetAlert
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Sensor
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorFilter
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorType
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.SectionHeader
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.Spacing

// =============================================================================
// Public entry point wired by AppNavHost
// =============================================================================

@Composable
fun FleetScreen(
    modifier: Modifier = Modifier,
    onDispatchClick: (String) -> Unit = {},
    viewModel: FleetViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    FleetScreenContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onSensorFilterSelected = viewModel::onSensorFilterSelected,
        onSensorClick = viewModel::onSensorClick,
        onDismissSensorSheet = viewModel::onDismissSensorSheet,
        onDispatchClick = onDispatchClick,
        onLimitsClick = { /* TODO: navigate to Thresholds screen */ },
        modifier = modifier
    )
}

// =============================================================================
// Root content
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FleetScreenContent(
    state: FleetUiState,
    onTabSelected: (Int) -> Unit,
    onSensorFilterSelected: (SensorFilter) -> Unit,
    onSensorClick: (Sensor) -> Unit,
    onDismissSensorSheet: () -> Unit,
    onDispatchClick: (String) -> Unit,
    onLimitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.bg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Screen header
            item {
                FleetScreenHeader(
                    onRouteCount = state.onRouteCount,
                    alertCount = state.alertCount,
                    onLimitsClick = onLimitsClick
                )
            }

            // 2. Static radar/map card
            item {
                FleetRadarCard(dispatches = state.dispatches)
            }

            // 3. Segmented tabs
            item {
                FleetSegmentedTabs(
                    selectedIndex = state.selectedTabIndex,
                    alertCount = state.alertCount,
                    dispatchCount = state.onRouteCount,
                    onTabSelected = onTabSelected
                )
            }

            // 4. Tab content
            when (state.selectedTabIndex) {
                0 -> {
                    // Active Dispatches
                    items(state.dispatches) { dispatch ->
                        DispatchCard(
                            dispatch = dispatch,
                            onClick = { onDispatchClick(dispatch.id) }
                        )
                    }
                }
                1 -> {
                    // Alerts
                    items(state.alerts) { alert ->
                        AlertRow(alert = alert)
                    }
                }
                2 -> {
                    // Sensors – header chips + filter + cards
                    item {
                        SensorsContent(
                            state = state,
                            onFilterSelected = onSensorFilterSelected,
                            onSensorClick = onSensorClick
                        )
                    }
                }
            }

            // 5. Footer note
            if (state.selectedTabIndex == 0) {
                item {
                    FleetFooter()
                }
            }
        }

        // Sensor detail bottom sheet
        state.selectedSensor?.let { sensor ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismissSensorSheet,
                sheetState = sheetState,
                containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
                contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.BottomSheet
            ) {
                SensorDetailSheet(sensor = sensor)
            }
        }
    }
}

// =============================================================================
// Screen header: title + KPI chips + Limits button
// =============================================================================

@Composable
private fun FleetScreenHeader(
    onRouteCount: Int,
    alertCount: Int,
    onLimitsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.bg)
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.fleet_screen_title),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.displayLarge,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.fleet_screen_subtitle),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
                )
            }
            // Limits icon button
            Box(
                modifier = Modifier
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
                    .clickable(onClick = onLimitsClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_limits,
                        contentDescription = stringResource(R.string.fleet_limits_btn),
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.fleet_limits_btn),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // KPI chips row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiChip(
                label = stringResource(R.string.fleet_kpi_on_route, onRouteCount),
                dotColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success
            )
            KpiChip(
                label = stringResource(R.string.fleet_kpi_alerts, alertCount),
                dotColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                textColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
            )
        }
    }
}

@Composable
private fun KpiChip(
    label: String,
    dotColor: Color,
    textColor: Color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnLight
) {
    Row(
        modifier = Modifier
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================================
// Static radar / map canvas card
// =============================================================================

@Composable
private fun FleetRadarCard(dispatches: List<DispatchSummary>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)
            .height(180.dp),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1B12)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Drawn with Canvas: dark background, lime route lines, truck dots
            val limeColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
            val dangerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
            val mutedColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Background grid lines (subtle)
                val gridColor = Color(0xFF1A2D1E)
                for (i in 1..4) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, h * i / 5f),
                        end = Offset(w, h * i / 5f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(w * i / 5f, 0f),
                        end = Offset(w * i / 5f, h),
                        strokeWidth = 1f
                    )
                }

                // Route line: Ica (bottom-left) -> Lima (center-right)
                val icaPoint = Offset(w * 0.12f, h * 0.78f)
                val limaHub = Offset(w * 0.62f, h * 0.35f)
                val callaoHub = Offset(w * 0.56f, h * 0.25f)
                val piuraPoint = Offset(w * 0.18f, h * 0.15f)

                // Panamericana Sur route (FL-102): Ica -> Lima
                val routePath1 = Path().apply {
                    moveTo(icaPoint.x, icaPoint.y)
                    cubicTo(
                        w * 0.25f, h * 0.72f,
                        w * 0.45f, h * 0.55f,
                        limaHub.x, limaHub.y
                    )
                }
                drawPath(
                    path = routePath1,
                    color = limeColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Carretera Central route (FL-408): Chavin -> Callao (dashed, red alert)
                val routePath2 = Path().apply {
                    moveTo(w * 0.72f, h * 0.72f)
                    cubicTo(
                        w * 0.70f, h * 0.55f,
                        w * 0.65f, h * 0.40f,
                        callaoHub.x, callaoHub.y
                    )
                }
                drawPath(
                    path = routePath2,
                    color = dangerColor.copy(alpha = 0.8f),
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                )

                // Panamericana Norte route (FL-219): Piura -> Lima
                val routePath3 = Path().apply {
                    moveTo(piuraPoint.x, piuraPoint.y)
                    cubicTo(
                        w * 0.30f, h * 0.18f,
                        w * 0.50f, h * 0.25f,
                        limaHub.x, limaHub.y
                    )
                }
                drawPath(
                    path = routePath3,
                    color = mutedColor.copy(alpha = 0.5f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )

                // Hub dot – Lima Central Hub
                drawCircle(
                    color = limeColor,
                    radius = 8f,
                    center = limaHub
                )
                drawCircle(
                    color = Color(0xFF0E1B12),
                    radius = 4f,
                    center = limaHub
                )

                // Truck dot FL-102 (lime, on-time)
                val fl102Pos = Offset(w * 0.38f, h * 0.52f)
                drawCircle(color = Color(0xFF0E1B12), radius = 14f, center = fl102Pos)
                drawCircle(
                    color = limeColor,
                    radius = 12f,
                    center = fl102Pos,
                    style = Stroke(width = 2f)
                )
                drawCircle(color = limeColor.copy(alpha = 0.3f), radius = 12f, center = fl102Pos, style = Fill)

                // Truck dot FL-408 (red, alert)
                val fl408Pos = Offset(w * 0.70f, h * 0.60f)
                drawCircle(color = dangerColor, radius = 14f, center = fl408Pos)
                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 5f, center = fl408Pos)

                // Truck dot FL-219 (muted, delayed)
                val fl219Pos = Offset(w * 0.28f, h * 0.25f)
                drawCircle(color = Color(0xFF0E1B12), radius = 14f, center = fl219Pos)
                drawCircle(
                    color = mutedColor,
                    radius = 12f,
                    center = fl219Pos,
                    style = Stroke(width = 2f)
                )
                drawCircle(color = mutedColor.copy(alpha = 0.2f), radius = 12f, center = fl219Pos, style = Fill)
            }

            // LIVE badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(Color(0xFF0E3B1A))
                    .border(1.dp, _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success.copy(alpha = 0.5f), _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.fleet_radar_live),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // GPS active badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.fleet_radar_gps_active),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Normal
                )
            }

            // Truck labels overlay (FL-102, FL-408, FL-219)
            // Static positioned text boxes approximating mockup positions
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TruckTag(label = "FL-102", temp = "3.4°C", isAlert = false)
                TruckTag(label = "FL-408", temp = "4.3°C", isAlert = true)
                TruckTag(label = "FL-219", temp = "2.1°C", isAlert = false)
            }
        }
    }
}

@Composable
private fun TruckTag(label: String, temp: String, isAlert: Boolean) {
    val bg = if (isAlert) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.danger else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.85f)
    val tc = if (isAlert) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Column {
            Text(
                text = label,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = temp,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                color = tc,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// =============================================================================
// Segmented tabs
// =============================================================================

@Composable
private fun FleetSegmentedTabs(
    selectedIndex: Int,
    alertCount: Int,
    dispatchCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = 12.dp)
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val tabs = listOf(
            Pair(stringResource(R.string.fleet_tab_dispatches, dispatchCount), null),
            Pair(stringResource(R.string.fleet_tab_alerts), alertCount),
            Pair(stringResource(R.string.fleet_tab_sensors), null)
        )
        tabs.forEachIndexed { index, (label, badgeCount) ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(
                        if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
                        else Color.Transparent
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary
                        else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (badgeCount != null && badgeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary.copy(alpha = 0.3f)
                                    else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                                color = if (isSelected) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary
                                else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Dispatch cards
// =============================================================================

// Note: DispatchCard is extracted to DispatchCard.kt


// =============================================================================
// Alerts tab content
// =============================================================================

@Composable
private fun AlertRow(alert: FleetAlert) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = 4.dp),
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = when (alert.severity) {
                        DispatchStatus.TEMP_ALERT -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
                        else -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = alert.title,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = alert.unitId,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
                text = when (alert.severity) {
                    DispatchStatus.TEMP_ALERT -> stringResource(R.string.dispatch_status_temp_alert)
                    DispatchStatus.WEIGH_STATION_DELAY -> stringResource(R.string.dispatch_status_weigh_delay)
                    DispatchStatus.ON_TIME -> stringResource(R.string.dispatch_status_on_time)
                },
                type = when (alert.severity) {
                    DispatchStatus.TEMP_ALERT -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER
                    DispatchStatus.WEIGH_STATION_DELAY -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING
                    DispatchStatus.ON_TIME -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS
                }
            )
        }
    }
}

// =============================================================================
// Sensors screen (Screen 2)
// =============================================================================

@Composable
internal fun SensorsContent(
    state: FleetUiState,
    onFilterSelected: (SensorFilter) -> Unit,
    onSensorClick: (Sensor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Status chip row: 12 ONLINE / 2 LOW BATTERY / 1 OFFLINE
        SensorStatusChips(
            onlineCount = state.onlineSensorCount,
            lowBatteryCount = state.lowBatterySensorCount,
            offlineCount = state.offlineSensorCount
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        SensorFilterChips(
            activeFilter = state.sensorFilter,
            onFilterSelected = onFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sensor cards
        state.filteredSensors.forEach { sensor ->
            SensorCard(
                sensor = sensor,
                onClick = { onSensorClick(sensor) }
            )
        }
    }
}

@Composable
private fun SensorStatusChips(
    onlineCount: Int,
    lowBatteryCount: Int,
    offlineCount: Int
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SensorStatusPill(
                label = stringResource(R.string.sensor_status_online, onlineCount),
                dotColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                textColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
            )
        }
        item {
            SensorStatusPill(
                label = stringResource(R.string.sensor_status_low_batt, lowBatteryCount),
                dotColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                textColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
            )
        }
        item {
            SensorStatusPill(
                label = stringResource(R.string.sensor_status_offline, offlineCount),
                dotColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                textColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
            )
        }
    }
}

@Composable
private fun SensorStatusPill(label: String, dotColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
            .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SensorFilterChips(
    activeFilter: SensorFilter,
    onFilterSelected: (SensorFilter) -> Unit
) {
    val filters = listOf(
        Pair(SensorFilter.ALL, stringResource(R.string.sensor_filter_all)),
        Pair(SensorFilter.ON_ROUTE, stringResource(R.string.sensor_filter_on_route)),
        Pair(SensorFilter.IN_WAREHOUSE, stringResource(R.string.sensor_filter_warehouse)),
        Pair(SensorFilter.OFFLINE, stringResource(R.string.sensor_filter_offline))
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (filter, label) ->
            val isActive = filter == activeFilter
            Box(
                modifier = Modifier
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill)
                    .background(
                        if (isActive) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
                        else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = if (isActive) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary
                    else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SensorCard(
    sensor: Sensor,
    onClick: () -> Unit
) {
    val isOffline = sensor.status == SensorStatus.OFFLINE
    val cardBg = if (isOffline) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.danger else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)) {
            // Header: sensor ID + status badge + type icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sensor.id,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SensorStatusBadge(status = sensor.status)
                }
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = sensorTypeIcon(sensor.type),
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sensor type description
            Text(
                text = sensorTypeLabel(sensor.type),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Linked truck / order or warehouse location
            if (sensor.linkedTruckId != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_dispatch_truck,
                        contentDescription = null,
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.fleet_dispatch_unit, sensor.linkedTruckId),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    sensor.linkedOrderId?.let { orderId ->
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.sensor_order_id, orderId),
                            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                        )
                    }
                }
                Text(
                    text = sensor.cargoOrLocation,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = if (isOffline) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.8f)
                    else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                )
            } else {
                // Warehouse location
                sensor.warehouseLocation?.let { loc ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                            id = R.drawable.ic_warehouse,
                            contentDescription = null,
                            tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = loc,
                            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra detail blocks (TH-04: chill core + humidity)
            if (sensor.detailLabel1 != null && sensor.detailValue1 != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SensorDetailBlock(
                        label = sensor.detailLabel1,
                        value = sensor.detailValue1,
                        iconRes = R.drawable.ic_reefer,
                        modifier = Modifier.weight(1f)
                    )
                    if (sensor.detailLabel2 != null && sensor.detailValue2 != null) {
                        SensorDetailBlock(
                            label = sensor.detailLabel2,
                            value = sensor.detailValue2,
                            iconRes = R.drawable.ic_humidity,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Last signal + battery bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sensor.lastSignal,
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = if (isOffline) _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
                    else _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BatteryLabel(
                    percent = sensor.batteryPercent,
                    isOffline = isOffline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            BatteryBar(percent = sensor.batteryPercent, isOffline = isOffline)

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            if (isOffline) {
                Button(
                    onClick = { /* TODO: troubleshoot action */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong,
                        contentColor = Color.White
                    )
                ) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_troubleshoot,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.sensor_btn_troubleshoot),
                        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium
                    )
                }
            } else if (sensor.linkedTruckId == null) {
                Button(
                    onClick = { /* TODO: link to order */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary
                    )
                ) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_link,
                        contentDescription = null,
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.sensor_btn_link_order),
                        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorDetailBlock(
    label: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium,
            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
            fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SensorStatusBadge(status: SensorStatus) {
    val (text, type) = when (status) {
        SensorStatus.ONLINE -> Pair(stringResource(R.string.sensor_status_label_online), _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.SUCCESS)
        SensorStatus.LOW_BATTERY -> Pair(stringResource(R.string.sensor_status_label_low_batt), _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.WARNING)
        SensorStatus.OFFLINE -> Pair(stringResource(R.string.sensor_status_label_offline), _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.DANGER)
        SensorStatus.STANDBY -> Pair(stringResource(R.string.sensor_status_label_standby), _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType.NEUTRAL)
    }
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.StatusBadge(
        text = text,
        type = type
    )
}

@Composable
private fun BatteryLabel(percent: Int, isOffline: Boolean) {
    val color = when {
        isOffline -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
        percent <= 20 -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
        percent <= 40 -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
        else -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
    }
    Text(
        text = stringResource(R.string.sensor_battery_label, percent),
        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.RobotoFontFamily
    )
}

@Composable
private fun BatteryBar(percent: Int, isOffline: Boolean) {
    val color = when {
        isOffline -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
        percent <= 20 -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.dangerStrong
        percent <= 40 -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning
        else -> _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
    }
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar(
        progress = percent / 100f,
        progressColor = color,
        trackColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar
    )
}

private fun sensorTypeIcon(type: SensorType): Int = when (type) {
    SensorType.TEMPERATURE -> R.drawable.ic_thermostat
    SensorType.HUMIDITY -> R.drawable.ic_humidity
    SensorType.GPS -> R.drawable.ic_gps
}

@Composable
private fun sensorTypeLabel(type: SensorType): String = when (type) {
    SensorType.TEMPERATURE -> stringResource(R.string.sensor_type_temperature)
    SensorType.HUMIDITY -> stringResource(R.string.sensor_type_humidity)
    SensorType.GPS -> stringResource(R.string.sensor_type_gps)
}

// =============================================================================
// Sensor detail bottom sheet (static / placeholder)
// =============================================================================

@Composable
private fun SensorDetailSheet(sensor: Sensor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)
            .padding(bottom = 32.dp)
    ) {
        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.SectionHeader(
            title = sensor.id,
            subtitle = sensorTypeLabel(sensor.type)
        )

        Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
            colors = CardDefaults.cardColors(
                containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar,
                contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
            )
        ) {
            Column(modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)) {
                SheetDetailRow(label = stringResource(R.string.sensor_sheet_status), value = sensor.status.name)
                SheetDetailRow(label = stringResource(R.string.sensor_sheet_truck), value = sensor.linkedTruckId ?: "—")
                SheetDetailRow(label = stringResource(R.string.sensor_sheet_order), value = sensor.linkedOrderId ?: "—")
                SheetDetailRow(label = stringResource(R.string.sensor_sheet_last_signal), value = sensor.lastSignal)
                SheetDetailRow(label = stringResource(R.string.sensor_sheet_battery), value = stringResource(R.string.sensor_battery_label, sensor.batteryPercent))
            }
        }
    }
}

@Composable
private fun SheetDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
        )
        Text(
            text = value,
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =============================================================================
// Footer
// =============================================================================

@Composable
private fun FleetFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.fleet_footer_gateways),
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
            )
        }
        Text(
            text = stringResource(R.string.fleet_footer_updated),
            style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
            color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(
    name = "FleetScreen - Active Dispatches",
    showBackground = true,
    backgroundColor = 0xFFE8F3E3,
    widthDp = 390,
    heightDp = 1400
)
@Composable
private fun FleetScreenPreview_Dispatches() {
    val repo = com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository()
    val previewState = FleetUiState(
        isLoading = false,
        onRouteCount = 3,
        alertCount = 2,
        dispatches = repo.getActiveDispatches(),
        alerts = repo.getFleetAlerts(),
        allSensors = repo.getAllSensors(),
        selectedTabIndex = 0
    )
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        FleetScreenContent(
            state = previewState,
            onTabSelected = {},
            onSensorFilterSelected = {},
            onSensorClick = {},
            onDismissSensorSheet = {},
            onDispatchClick = {},
            onLimitsClick = {}
        )
    }
}

@Preview(
    name = "FleetScreen - Sensors Tab",
    showBackground = true,
    backgroundColor = 0xFFE8F3E3,
    widthDp = 390,
    heightDp = 1800
)
@Composable
private fun FleetScreenPreview_Sensors() {
    val repo = com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository()
    val previewState = FleetUiState(
        isLoading = false,
        onRouteCount = 3,
        alertCount = 2,
        dispatches = repo.getActiveDispatches(),
        alerts = repo.getFleetAlerts(),
        allSensors = repo.getAllSensors(),
        selectedTabIndex = 2
    )
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        FleetScreenContent(
            state = previewState,
            onTabSelected = {},
            onSensorFilterSelected = {},
            onSensorClick = {},
            onDismissSensorSheet = {},
            onDispatchClick = {},
            onLimitsClick = {}
        )
    }
}
