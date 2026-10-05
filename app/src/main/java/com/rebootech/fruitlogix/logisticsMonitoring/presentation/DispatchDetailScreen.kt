package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ChartTimeFilter
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchDetail
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchTemperaturePoint
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.MilestoneStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.RouteMilestone
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchDetailScreen(
    unitId: String,
    onBackClick: () -> Unit = {},
    onNavigateToAlertDetail: (() -> Unit)? = null,
    viewModel: DispatchDetailViewModel = viewModel()
) {
    LaunchedEffect(unitId) {
        viewModel.loadDispatchDetail(unitId)
    }

    val uiState by viewModel.uiState.collectAsState()

    val currentDetail = (uiState as? DispatchDetailUiState.Success)?.detail
    val isAlert = currentDetail?.status == DispatchStatus.TEMP_ALERT
    val isDelay = currentDetail?.status == DispatchStatus.WEIGH_STATION_DELAY

    Scaffold(
        containerColor = FruitLogixTheme.colors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentDetail?.unitId ?: unitId,
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Status badge on Top Bar
                            when {
                                isAlert -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = FruitLogixTheme.colors.dangerStrong
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "TEMP ALERT",
                                                style = TextStyle(
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                isDelay -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = FruitLogixTheme.colors.warning
                                    ) {
                                        Text(
                                            text = "WEIGH DELAY",
                                            style = TextStyle(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = FruitLogixTheme.colors.surfaceDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = FruitLogixTheme.colors.primary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "ON TIME",
                                            style = TextStyle(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = FruitLogixTheme.colors.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "REEFER TELEMETRY",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FruitLogixTheme.colors.appbar
                )
            )
        },
        bottomBar = {
            if (currentDetail != null) {
                StickyBottomActions(
                    detail = currentDetail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FruitLogixTheme.colors.bg)
                        .navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FruitLogixTheme.colors.bg)
        ) {
            when (val state = uiState) {
                is DispatchDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FruitLogixTheme.colors.surfaceDark
                    )
                }
                is DispatchDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = FruitLogixTheme.colors.dangerStrong,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DispatchDetailUiState.Success -> {
                    DispatchDetailContent(
                        successState = state,
                        onTimeFilterSelected = { filter -> viewModel.setTimeFilter(filter) },
                        onToggleTooltip = { tooltip -> viewModel.toggleTooltip(tooltip) },
                        onNavigateToAlertDetail = onNavigateToAlertDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun DispatchDetailContent(
    successState: DispatchDetailUiState.Success,
    onTimeFilterSelected: (ChartTimeFilter) -> Unit,
    onToggleTooltip: (String?) -> Unit,
    onNavigateToAlertDetail: (() -> Unit)?
) {
    val detail = successState.detail
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // a. Order Summary Card + optional red "TEMP ALERT" badge
        OrderSummaryCard(detail = detail)

        // b. Compact Route Card (Canvas mini map & distance/ETA)
        CompactRouteCard(detail = detail)

        // c. Forecast Banner (amber, max 2 lines)
        if (detail.hasPredictiveAlert && detail.predictiveAlertMessage != null) {
            ForecastBanner(
                message = detail.predictiveAlertMessage,
                onBannerClick = onNavigateToAlertDetail
            )
        }

        // d. LIVE TEMPERATURE Card (dark #1F2D23 with Canvas Line Chart)
        LiveTemperatureCard(
            detail = detail,
            timeFilter = successState.timeFilter,
            selectedTooltip = successState.selectedTooltip,
            onTimeFilterSelected = onTimeFilterSelected,
            onToggleTooltip = onToggleTooltip
        )

        // e. Two Tiles: Humidity & Sensor Battery / Reefer Node
        TwoTelemetryTiles(detail = detail)

        // f. Checkpoints & Stops
        CheckpointsCard(milestones = detail.milestones)

        // Extra spacing so sticky bottom bar doesn't overlap scrollable end
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -----------------------------------------------------------------------------
// a. Order Summary Card
// -----------------------------------------------------------------------------
@Composable
private fun OrderSummaryCard(detail: DispatchDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${detail.orderId}",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Grade Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FruitLogixTheme.colors.primary
                    ) {
                        Text(
                            text = "GRADE A",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = FruitLogixTheme.colors.surfaceDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (detail.status == DispatchStatus.TEMP_ALERT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FruitLogixTheme.colors.dangerStrong
                        ) {
                            Text(
                                text = "TEMP ALERT",
                                style = TextStyle(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Cargo Box Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dispatch_truck),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = detail.cargoDescription,
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = FruitLogixTheme.colors.textMuted
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logistics_truck),
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Trans. Frigoríficos del Sur",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "•",
                    color = FruitLogixTheme.colors.textMuted,
                    fontSize = 12.sp
                )

                Text(
                    text = detail.driverName,
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // License Plate Chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = detail.licensePlate,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// b. Compact Route Card (Canvas vector map & progress)
// -----------------------------------------------------------------------------
@Composable
private fun CompactRouteCard(detail: DispatchDetail) {
    val isDelay = detail.delayText != null || detail.status == DispatchStatus.WEIGH_STATION_DELAY

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = Color.White
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top overlay bar inside card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // GPS Status Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(FruitLogixTheme.colors.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS SYNCED • 4s AGO",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Delay / On-Time Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDelay) FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.2f) else FruitLogixTheme.colors.primary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDelay) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (isDelay) R.drawable.ic_warning else R.drawable.ic_verified),
                            contentDescription = null,
                            tint = if (isDelay) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = detail.delayText ?: "ON TIME",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isDelay) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary
                        )
                    }
                }
            }

            // Route Graphic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF152219))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid / Map lines background
                    val gridColor = Color(0x1AFFFFFF)
                    for (i in 1..4) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, h * i / 5f),
                            end = Offset(w, h * i / 5f),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 1..6) {
                        drawLine(
                            color = gridColor,
                            start = Offset(w * i / 7f, 0f),
                            end = Offset(w * i / 7f, h),
                            strokeWidth = 1f
                        )
                    }

                    // Route curve path
                    val path = Path().apply {
                        moveTo(w * 0.08f, h * 0.75f)
                        cubicTo(
                            w * 0.35f, h * 0.25f,
                            w * 0.65f, h * 0.85f,
                            w * 0.92f, h * 0.35f
                        )
                    }

                    // Shadow/Glow path
                    drawPath(
                        path = path,
                        color = Color(0xFFC6EC47).copy(alpha = 0.25f),
                        style = Stroke(width = 8.dp.toPx())
                    )

                    // Main route line
                    drawPath(
                        path = path,
                        color = Color(0xFFC6EC47),
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
                        )
                    )

                    // Origin pin dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(w * 0.08f, h * 0.75f)
                    )

                    // Destination pin dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(w * 0.92f, h * 0.35f)
                    )

                    // Current Position Truck Pin
                    val currentX = w * (0.08f + 0.84f * detail.progressFraction)
                    val currentY = h * 0.52f

                    // Outer pulse ring
                    drawCircle(
                        color = Color(0xFFC6EC47).copy(alpha = 0.35f),
                        radius = 16.dp.toPx(),
                        center = Offset(currentX, currentY)
                    )
                    // Inner truck pin background
                    drawCircle(
                        color = Color(0xFFC6EC47),
                        radius = 10.dp.toPx(),
                        center = Offset(currentX, currentY)
                    )
                    drawCircle(
                        color = Color(0xFF1F2D23),
                        radius = 5.dp.toPx(),
                        center = Offset(currentX, currentY)
                    )
                }

                // Location badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = Color(0xEE1F2D23),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, Color(0x33C6EC47), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = detail.currentLocationLabel,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Bottom Route Stats inside card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DESTINATION ETA",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Text(
                        text = detail.dockEta,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${detail.distanceProgress} (${(detail.progressFraction * 100).toInt()}%)",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = FruitLogixTheme.colors.primary
                    )
                    Text(
                        text = detail.destinationName,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// c. Forecast Banner (amber, max 2 lines)
// -----------------------------------------------------------------------------
@Composable
private fun ForecastBanner(
    message: String,
    onBannerClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onBannerClick?.invoke() },
        color = Color(0xFFFDE8E8),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFD32F2F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color(0xFFB71C1C),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Secondary thermo compressor auto-override armed. Compressor running at 94% continuous payload load.",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    ),
                    color = Color(0xFFC62828)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// d. LIVE TEMPERATURE Card (dark #1F2D23 + Canvas Chart with Y axis, safe band, threshold labels above, lime/red line)
// -----------------------------------------------------------------------------
@Composable
private fun LiveTemperatureCard(
    detail: DispatchDetail,
    timeFilter: ChartTimeFilter,
    selectedTooltip: String?,
    onTimeFilterSelected: (ChartTimeFilter) -> Unit,
    onToggleTooltip: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Pulsing dot + LIVE TEMPERATURE + Filter chips 1H / 6H / FULL TRIP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingLiveDot()
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE TEMPERATURE",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }

                // Time Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartFilterChip(
                        label = "1H",
                        isSelected = timeFilter == ChartTimeFilter.ONE_HOUR,
                        onClick = { onTimeFilterSelected(ChartTimeFilter.ONE_HOUR) }
                    )
                    ChartFilterChip(
                        label = "6H",
                        isSelected = timeFilter == ChartTimeFilter.SIX_HOURS,
                        onClick = { onTimeFilterSelected(ChartTimeFilter.SIX_HOURS) }
                    )
                    ChartFilterChip(
                        label = "FULL TRIP",
                        isSelected = timeFilter == ChartTimeFilter.FULL_TRIP,
                        onClick = { onTimeFilterSelected(ChartTimeFilter.FULL_TRIP) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Temperature Display + Rate of rise badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = detail.currentTemp,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = Color.White
                    )
                }

                // Rate of rise chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (detail.rateOfRise.contains("+0.0")) FruitLogixTheme.colors.primary.copy(alpha = 0.2f) else FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↗ ▲ ${detail.rateOfRise}",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (detail.rateOfRise.contains("+0.0")) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.dangerStrong
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart with Y axis labels, shaded safe band, threshold labels ABOVE lines, lime line, red excursion segment/marker
            DetailedTemperatureChartCanvas(
                points = detail.detailedTemperaturePoints,
                timeFilter = timeFilter,
                selectedTooltip = selectedTooltip,
                onToggleTooltip = onToggleTooltip,
                minThreshold = detail.minTempThreshold,
                maxThreshold = detail.maxTempThreshold
            )
        }
    }
}

@Composable
private fun PulsingLiveDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(FruitLogixTheme.colors.primary.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun ChartFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        color = if (isSelected) FruitLogixTheme.colors.primary else Color.White.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = if (isSelected) FruitLogixTheme.colors.surfaceDark else FruitLogixTheme.colors.textMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DetailedTemperatureChartCanvas(
    points: List<DispatchTemperaturePoint>,
    timeFilter: ChartTimeFilter,
    selectedTooltip: String?,
    onToggleTooltip: (String?) -> Unit,
    minThreshold: Float,
    maxThreshold: Float
) {
    if (points.isEmpty()) return

    // Filter points based on selected time chip
    val visiblePoints = when (timeFilter) {
        ChartTimeFilter.ONE_HOUR -> points.takeLast(2.coerceAtMost(points.size))
        ChartTimeFilter.SIX_HOURS -> points.takeLast(4.coerceAtMost(points.size))
        ChartTimeFilter.FULL_TRIP -> points
    }

    val textMeasurer = rememberTextMeasurer()

    val yMinScale = 1.5f
    val yMaxScale = 5.0f
    val yRange = yMaxScale - yMinScale

    val limeColor = Color(0xFFC6EC47)
    val redColor = Color(0xFFFF5252)
    val gridLineColor = Color(0x33FFFFFF)
    val textMutedColor = Color(0xFF8A9E8F)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(visiblePoints) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    val paddingLeft = 36.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val chartWidth = width - paddingLeft - paddingRight

                    // Check if tap hit near any excursion point
                    visiblePoints.forEachIndexed { index, pt ->
                        val step = chartWidth / (visiblePoints.size - 1).coerceAtLeast(1)
                        val ptX = paddingLeft + index * step
                        if (kotlin.math.abs(offset.x - ptX) < 40.dp.toPx()) {
                            if (pt.isExcursion && pt.excursionTooltip != null) {
                                onToggleTooltip(pt.excursionTooltip)
                            }
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 36.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 24.dp.toPx() // Room for threshold label ABOVE line
        val paddingBottom = 28.dp.toPx() // Room for X-axis labels

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        fun getY(celsius: Float): Float {
            val norm = (celsius - yMinScale) / yRange
            return paddingTop + chartHeight * (1f - norm)
        }

        fun getX(index: Int): Float {
            val step = chartWidth / (visiblePoints.size - 1).coerceAtLeast(1)
            return paddingLeft + index * step
        }

        // 1. Shaded Safe Band between 2.0°C and 4.0°C
        val yMinThresh = getY(minThreshold)
        val yMaxThresh = getY(maxThreshold)
        drawRect(
            color = Color(0x18C6EC47),
            topLeft = Offset(paddingLeft, yMaxThresh),
            size = Size(chartWidth, yMinThresh - yMaxThresh)
        )

        // 2. Y Axis Labels (2°C, 3°C, 4°C)
        val yLabels = listOf(2.0f to "2°C", 3.0f to "3°C", 4.0f to "4°C")
        yLabels.forEach { (tempVal, labelText) ->
            val yPos = getY(tempVal)

            // Draw horizontal grid line
            drawLine(
                color = gridLineColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(width - paddingRight, yPos),
                strokeWidth = 1f
            )

            // Draw Y label text on left
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 10.sp,
                    color = textMutedColor
                ),
                topLeft = Offset(4.dp.toPx(), yPos - 7.dp.toPx())
            )
        }

        // 3. Dashed Threshold Lines & Labels ABOVE the lines
        // Top Dashed Max Threshold Line (4.0°C)
        drawLine(
            color = Color(0x80C6EC47),
            start = Offset(paddingLeft, yMaxThresh),
            end = Offset(width - paddingRight, yMaxThresh),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
        // Label ABOVE 4.0°C line
        drawText(
            textMeasurer = textMeasurer,
            text = "4.0°C CRITICAL MAX",
            style = TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = limeColor
            ),
            topLeft = Offset(paddingLeft + 4.dp.toPx(), yMaxThresh - 16.dp.toPx())
        )

        // Bottom Dashed Min Threshold Line (2.0°C)
        drawLine(
            color = Color(0x80C6EC47),
            start = Offset(paddingLeft, yMinThresh),
            end = Offset(width - paddingRight, yMinThresh),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
        // Label ABOVE 2.0°C line
        drawText(
            textMeasurer = textMeasurer,
            text = "2.0°C MIN SAFE",
            style = TextStyle(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = textMutedColor
            ),
            topLeft = Offset(paddingLeft + 4.dp.toPx(), yMinThresh - 16.dp.toPx())
        )

        // 4. Line Chart Segments: LIME for in-range points, RED for excursion segment
        if (visiblePoints.size >= 2) {
            for (i in 0 until visiblePoints.size - 1) {
                val p1 = visiblePoints[i]
                val p2 = visiblePoints[i + 1]

                val x1 = getX(i)
                val y1 = getY(p1.celsius)
                val x2 = getX(i + 1)
                val y2 = getY(p2.celsius)

                val segmentIsExcursion = p1.isExcursion || p2.isExcursion ||
                        p1.celsius > maxThreshold || p2.celsius > maxThreshold ||
                        p1.celsius < minThreshold || p2.celsius < minThreshold

                val segColor = if (segmentIsExcursion) redColor else limeColor

                drawLine(
                    color = segColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // 5. Point Markers & Excursion Ring
        visiblePoints.forEachIndexed { i, pt ->
            val x = getX(i)
            val y = getY(pt.celsius)

            if (pt.isExcursion) {
                // Red glowing outer ring
                drawCircle(
                    color = redColor.copy(alpha = 0.35f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
                // Red solid marker
                drawCircle(
                    color = redColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw Tooltip Box if active or selected
                val tooltipMsg = pt.excursionTooltip ?: selectedTooltip
                if (tooltipMsg != null) {
                    val tooltipText = tooltipMsg
                    val textLayoutResult = textMeasurer.measure(
                        text = tooltipText,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    )
                    val ttW = textLayoutResult.size.width + 16.dp.toPx()
                    val ttH = textLayoutResult.size.height + 8.dp.toPx()
                    val ttX = (x - ttW / 2f).coerceIn(paddingLeft, width - paddingRight - ttW)
                    val ttY = y - ttH - 10.dp.toPx()

                    drawRoundRect(
                        color = Color(0xEE1F2D23),
                        topLeft = Offset(ttX, ttY),
                        size = Size(ttW, ttH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = redColor,
                        topLeft = Offset(ttX, ttY),
                        size = Size(ttW, ttH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(ttX + 8.dp.toPx(), ttY + 4.dp.toPx())
                    )
                }
            } else {
                // In-range lime marker
                drawCircle(
                    color = limeColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFF1F2D23),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // X-Axis Time Label below
            val xLabel = pt.timeLabel
            val labelMeas = textMeasurer.measure(
                text = xLabel,
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 9.sp,
                    color = if (pt.isExcursion) redColor else textMutedColor,
                    fontWeight = if (pt.isExcursion) FontWeight.Bold else FontWeight.Normal
                )
            )
            val lblX = x - labelMeas.size.width / 2f
            drawText(
                textLayoutResult = labelMeas,
                topLeft = Offset(lblX, height - paddingBottom + 8.dp.toPx())
            )
        }
    }
}

// -----------------------------------------------------------------------------
// e. Two Telemetry Tiles: Humidity & Sensor Battery
// -----------------------------------------------------------------------------
@Composable
private fun TwoTelemetryTiles(detail: DispatchDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tile 1: Humidity
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FruitLogixTheme.colors.surfaceDark,
                contentColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logistics_drop),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "HUMIDITY",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Text(
                        text = detail.humidity,
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Nominal safe",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 9.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
            }
        }

        // Tile 2: Sensor Battery / Reefer Node
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FruitLogixTheme.colors.surfaceDark,
                contentColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_battery),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "REEFER NODE",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Text(
                        text = "${detail.batteryPercent}%",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Solar Assisted",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 9.sp
                        ),
                        color = FruitLogixTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// f. Checkpoints & Stops (3-4 steps updated to Peru route)
// -----------------------------------------------------------------------------
@Composable
private fun CheckpointsCard(milestones: List<RouteMilestone>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Checkpoints & Stops",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "${milestones.size} STATIONS",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            milestones.forEachIndexed { index, milestone ->
                MilestoneItemRow(
                    milestone = milestone,
                    isLast = index == milestones.size - 1
                )
            }
        }
    }
}

@Composable
private fun MilestoneItemRow(milestone: RouteMilestone, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val iconRes = when (milestone.status) {
                MilestoneStatus.COMPLETED -> R.drawable.ic_verified
                MilestoneStatus.IN_TRANSIT -> R.drawable.ic_logistics_truck
                MilestoneStatus.WARNING -> R.drawable.ic_warning
                MilestoneStatus.PENDING -> R.drawable.ic_verified
            }

            val iconColor = when (milestone.status) {
                MilestoneStatus.COMPLETED -> FruitLogixTheme.colors.primary
                MilestoneStatus.IN_TRANSIT -> FruitLogixTheme.colors.primary
                MilestoneStatus.WARNING -> FruitLogixTheme.colors.dangerStrong
                MilestoneStatus.PENDING -> FruitLogixTheme.colors.textMuted
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(FruitLogixTheme.colors.textMuted.copy(alpha = 0.25f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = milestone.title,
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = milestone.timestamp,
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp
                    ),
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            if (milestone.detailNote != null) {
                Text(
                    text = milestone.detailNote,
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp
                    ),
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. STICKY BOTTOM ACTIONS (lime Contact Driver, dark View Sensor, outlined Open Chat)
// -----------------------------------------------------------------------------
@Composable
private fun StickyBottomActions(
    detail: DispatchDetail,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier,
        color = FruitLogixTheme.colors.bg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Lime Button: Contact Driver (ACTION_DIAL)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${detail.driverPhone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FruitLogixTheme.colors.primary,
                    contentColor = FruitLogixTheme.colors.surfaceDark
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_contact_driver),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FruitLogixTheme.colors.surfaceDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Contact driver",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }

            // Dark Button: View sensor
            Button(
                onClick = { /* Placeholder action for View sensor */ },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FruitLogixTheme.colors.surfaceDark,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sensor_waves),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FruitLogixTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "View sensor",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            // Outlined Button: Open chat
            OutlinedButton(
                onClick = { /* Placeholder action for Open chat */ },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FruitLogixTheme.colors.surfaceDark
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, FruitLogixTheme.colors.surfaceDark)
            ) {
                Text(
                    text = "Open chat",
                    style = TextStyle(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Previews for Alert & On-Time States
// -----------------------------------------------------------------------------
@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3, name = "Alert State (FL-408)")
@Composable
private fun DispatchDetailAlertPreview() {
    val repo = FakeFleetRepository()
    val detail = repo.getDispatchDetail("FL-408")

    FruitLogixTheme {
        DispatchDetailContent(
            successState = DispatchDetailUiState.Success(detail = detail),
            onTimeFilterSelected = {},
            onToggleTooltip = {},
            onNavigateToAlertDetail = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8F3E3, name = "On-Time State (FL-102)")
@Composable
private fun DispatchDetailOnTimePreview() {
    val repo = FakeFleetRepository()
    val detail = repo.getDispatchDetail("FL-102")

    FruitLogixTheme {
        DispatchDetailContent(
            successState = DispatchDetailUiState.Success(detail = detail),
            onTimeFilterSelected = {},
            onToggleTooltip = {},
            onNavigateToAlertDetail = {}
        )
    }
}
