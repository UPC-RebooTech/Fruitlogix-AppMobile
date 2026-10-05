package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Arrival
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ArrivalStatus
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.components.ArrivalCard
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.components.GeofenceAlertSheet
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import kotlinx.coroutines.launch

// Placeholder route constant owned by orderManagement bounded context
// TODO: Replace with OrderManagementRoutes.ScanReception when orderManagement exposes its reception route
const val ORDER_RECEPTION_ROUTE = "orders/reception/{unitId}"
fun orderReceptionRoute(unitId: String) = "orders/reception/$unitId"

/**
 * Entry point for the Inbound Arrivals Screen.
 */
@Composable
fun ArrivalsScreen(
    onBackClick: () -> Unit = {},
    onNavigateToReception: (String) -> Boolean = { false },
    viewModel: ArrivalsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ArrivalsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onFilterSelected = viewModel::selectFilter,
        onNavigateToReception = onNavigateToReception,
        onViewRouteClick = { /* Placeholder view route action */ },
        onSimulateGeofenceClick = { viewModel.simulateGeofenceEntry("FL-102") },
        onDismissGeofenceSheet = viewModel::dismissGeofenceSheet,
        modifier = modifier
    )
}

/**
 * Stateless Content Composable for Inbound Arrivals Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalsScreenContent(
    uiState: ArrivalsUiState,
    onBackClick: () -> Unit,
    onFilterSelected: (ArrivalsFilter) -> Unit,
    onNavigateToReception: (String) -> Boolean,
    onViewRouteClick: (String) -> Unit,
    onSimulateGeofenceClick: () -> Unit,
    onDismissGeofenceSheet: () -> Unit,
    showDebugButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val paleMint = Color(0xFFE8F3E3)
    val textPrimary = Color(0xFF1B2E1E)
    val textMuted = Color(0xFF5A7060)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val comingSoonMsg = stringResource(id = R.string.reception_coming_soon)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.arrivals_title),
                        style = FruitLogixTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = paleMint
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1F2D23),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = paleMint,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Subtitle header
                item {
                    Text(
                        text = stringResource(id = R.string.arrivals_subtitle),
                        style = FruitLogixTheme.typography.bodyMedium.copy(
                            color = textMuted,
                            fontSize = 14.sp
                        )
                    )
                }

                // 2. Working Filter Chips: All, Approaching, At gate
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ArrivalsFilterChip(
                            label = stringResource(id = R.string.arrivals_filter_all),
                            count = uiState.arrivals.size,
                            isSelected = uiState.selectedFilter == ArrivalsFilter.ALL,
                            onClick = { onFilterSelected(ArrivalsFilter.ALL) }
                        )

                        ArrivalsFilterChip(
                            label = stringResource(id = R.string.arrivals_filter_approaching),
                            count = uiState.arrivals.count { it.status == ArrivalStatus.APPROACHING },
                            isSelected = uiState.selectedFilter == ArrivalsFilter.APPROACHING,
                            onClick = { onFilterSelected(ArrivalsFilter.APPROACHING) }
                        )

                        ArrivalsFilterChip(
                            label = stringResource(id = R.string.arrivals_filter_at_gate),
                            count = uiState.arrivals.count { it.status == ArrivalStatus.AT_GATE },
                            isSelected = uiState.selectedFilter == ArrivalsFilter.AT_GATE,
                            onClick = { onFilterSelected(ArrivalsFilter.AT_GATE) }
                        )
                    }
                }

                // 3. Debug "Simulate geofence entry" ghost button
                if (showDebugButton) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SORTED BY DISTANCE (CLOSEST FIRST)",
                                style = FruitLogixTheme.typography.labelSmall.copy(
                                    color = textMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            OutlinedButton(
                                onClick = onSimulateGeofenceClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textMuted
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB5CBB0)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚡ " + stringResource(id = R.string.arrivals_simulate_geofence),
                                    style = FruitLogixTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Inbound Arrival Cards (Max 3, sorted by distance)
                if (uiState.filteredArrivals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2D23))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_logistics_truck),
                                    contentDescription = null,
                                    tint = Color(0xFFA0B2A6),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No trucks approaching",
                                    style = FruitLogixTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.arrivals_empty),
                                    style = FruitLogixTheme.typography.bodySmall.copy(
                                        color = Color(0xFFA0B2A6)
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.filteredArrivals,
                        key = { it.unitId }
                    ) { arrival ->
                        ArrivalCard(
                            arrival = arrival,
                            onStartReceptionClick = { unitId ->
                                val success = onNavigateToReception(orderReceptionRoute(unitId))
                                if (!success) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(comingSoonMsg)
                                    }
                                }
                            },
                            onViewRouteClick = onViewRouteClick
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            // 5. Geofence Alert Sheet Overlay
            if (uiState.showGeofenceSheet && uiState.simulatedAlertArrival != null) {
                GeofenceAlertSheet(
                    arrival = uiState.simulatedAlertArrival,
                    onDismiss = onDismissGeofenceSheet,
                    onStartReception = { unitId ->
                        onDismissGeofenceSheet()
                        val success = onNavigateToReception(orderReceptionRoute(unitId))
                        if (!success) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(comingSoonMsg)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Filter Chip Composable for Arrivals screen.
 */
@Composable
private fun ArrivalsFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedBg = Color(0xFFC2E85A)
    val unselectedBg = Color(0xFFD8E7D3)
    val selectedText = Color(0xFF1B2E1E)
    val unselectedText = Color(0xFF3B4D40)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) selectedBg else unselectedBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = FruitLogixTheme.typography.labelMedium.copy(
                    color = if (isSelected) selectedText else unselectedText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0xFF1B2E1E) else Color(0xFFB5CBB0))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    style = FruitLogixTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.White else selectedText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "ArrivalsScreen - List View", showBackground = true, backgroundColor = 0xFFE8F3E3)
@Composable
private fun ArrivalsScreenPreview_List() {
    val repo = FakeFleetRepository()
    val state = ArrivalsUiState(
        arrivals = repo.getArrivals(),
        selectedFilter = ArrivalsFilter.ALL
    )
    FruitLogixTheme {
        ArrivalsScreenContent(
            uiState = state,
            onBackClick = {},
            onFilterSelected = {},
            onNavigateToReception = { false },
            onViewRouteClick = {},
            onSimulateGeofenceClick = {},
            onDismissGeofenceSheet = {}
        )
    }
}

@Preview(name = "ArrivalsScreen - Empty View", showBackground = true, backgroundColor = 0xFFE8F3E3)
@Composable
private fun ArrivalsScreenPreview_Empty() {
    val state = ArrivalsUiState(
        arrivals = emptyList(),
        selectedFilter = ArrivalsFilter.ALL
    )
    FruitLogixTheme {
        ArrivalsScreenContent(
            uiState = state,
            onBackClick = {},
            onFilterSelected = {},
            onNavigateToReception = { false },
            onViewRouteClick = {},
            onSimulateGeofenceClick = {},
            onDismissGeofenceSheet = {}
        )
    }
}

@Preview(name = "GeofenceAlertSheet - Mockup Match Preview", showBackground = true, backgroundColor = 0xFF1F2D23)
@Composable
private fun GeofenceAlertSheetPreview() {
    val sampleArrival = Arrival(
        unitId = "FL-102",
        licensePlate = "BQK-482",
        driverName = "Jorge Huamán",
        cargoDescription = "Ica grapes",
        palletsCount = 16,
        orderId = "FX-1040",
        assignedDock = "Dock B",
        reeferTemp = "3.4°C",
        humidity = "88% RH",
        distanceKm = 1.8f,
        distanceLabel = "1.8 km to warehouse",
        status = ArrivalStatus.APPROACHING,
        etaMinutes = 6,
        speedKmh = "18 km/h"
    )

    FruitLogixTheme {
        GeofenceAlertSheet(
            arrival = sampleArrival,
            onDismiss = {},
            onStartReception = {}
        )
    }
}
