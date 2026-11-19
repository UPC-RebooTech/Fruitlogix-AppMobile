package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderDateRange
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderFilters
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private enum class DateFilterOption {
    ALL_TIME,
    TODAY,
    THIS_WEEK,
    CUSTOM
}

private val SelectedChipBg: Color @Composable get() = MaterialTheme.colorScheme.secondary
private val UnselectedChipBg: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
private val BadgeSelectedBg: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
private val BadgeUnselectedBg: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderFilterSheet(
    initialFilters: OrderFilters,
    allOrders: List<Order>,
    statusCounts: Map<OrderStatus?, Int>,
    onDismiss: () -> Unit,
    onApplyFilters: (OrderFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val today = remember { LocalDate.now() }
    val startOfWeek = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val endOfWeek = remember(today) { today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) }
    val weekNumber = remember(today) {
        val weekFields = WeekFields.of(Locale.getDefault())
        today.get(weekFields.weekOfYear())
    }

    val availableClients = remember(allOrders) {
        allOrders.map { it.clientName }.distinct().sorted()
    }

    // Determine initial date option
    val initialDateOption = remember(initialFilters.dateRange) {
        val range = initialFilters.dateRange
        when {
            range == null -> DateFilterOption.ALL_TIME
            range.start == today && range.end == today -> DateFilterOption.TODAY
            range.start == startOfWeek && range.end == endOfWeek -> DateFilterOption.THIS_WEEK
            else -> DateFilterOption.CUSTOM
        }
    }

    var selectedDateOption by remember { mutableStateOf(initialDateOption) }
    var draftDateRange by remember { mutableStateOf(initialFilters.dateRange) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var draftStatuses by remember { mutableStateOf(initialFilters.statuses) }
    var draftClientIds by remember { mutableStateOf(initialFilters.clientIds) }

    // Calculate active filters count in the sheet
    val dateFilterActive = draftDateRange != null
    val clientFilterActiveCount = if (draftClientIds.isNotEmpty() && draftClientIds.size < availableClients.size) {
        draftClientIds.size
    } else {
        0
    }
    val totalActiveCount = (if (dateFilterActive) 1 else 0) + draftStatuses.size + clientFilterActiveCount

    // Date Range Picker Dialog
    if (showCustomDatePicker) {
        val initialStartMillis = draftDateRange?.start?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val initialEndMillis = draftDateRange?.end?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStartMillis,
            initialSelectedEndDateMillis = initialEndMillis
        )

        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                            val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                            draftDateRange = OrderDateRange(start, end)
                            selectedDateOption = DateFilterOption.CUSTOM
                        }
                        showCustomDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.producer_orders_filter_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text(stringResource(R.string.producer_orders_filter_date_cancel))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = stringResource(R.string.producer_orders_filter_select_date_range),
                        modifier = Modifier.padding(16.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. HEADER (Fixed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.producer_orders_filter_title),
                            style = FruitLogixTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .clip(FruitLogixTheme.shapes.Pill)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.producer_orders_filter_active_count, totalActiveCount),
                                style = FruitLogixTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.producer_orders_filter_subtitle),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Close Button (Touch target >= 48dp)
                val closeDescription = stringResource(R.string.producer_orders_cd_close_filters)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            onClick = onDismiss,
                            role = Role.Button
                        )
                        .semantics {
                            contentDescription = closeDescription
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_close,
                            contentDescription = stringResource(R.string.producer_orders_filter_close),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // ----------------------------------------------------
                // SECCIÓN 1: DATE RANGE
                // ----------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppIcon(
                            id = R.drawable.ic_schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.producer_orders_filter_date_range),
                            style = FruitLogixTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(R.string.producer_orders_filter_week_format, weekNumber),
                        style = FruitLogixTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SelectedChipBg
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All time
                    FilterPillChip(
                        label = stringResource(R.string.producer_orders_filter_date_all_time),
                        isSelected = selectedDateOption == DateFilterOption.ALL_TIME,
                        onClick = {
                            selectedDateOption = DateFilterOption.ALL_TIME
                            draftDateRange = null
                        }
                    )

                    // Today
                    FilterPillChip(
                        label = stringResource(R.string.producer_orders_filter_date_today),
                        isSelected = selectedDateOption == DateFilterOption.TODAY,
                        onClick = {
                            selectedDateOption = DateFilterOption.TODAY
                            draftDateRange = OrderDateRange(today, today)
                        }
                    )

                    // This week
                    FilterPillChip(
                        label = stringResource(R.string.producer_orders_filter_date_this_week),
                        isSelected = selectedDateOption == DateFilterOption.THIS_WEEK,
                        onClick = {
                            selectedDateOption = DateFilterOption.THIS_WEEK
                            draftDateRange = OrderDateRange(startOfWeek, endOfWeek)
                        }
                    )

                    // Custom range
                    FilterPillChip(
                        label = stringResource(R.string.producer_orders_filter_date_custom_range),
                        isSelected = selectedDateOption == DateFilterOption.CUSTOM,
                        onClick = {
                            showCustomDatePicker = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date banner card
                val dateDisplayRange = draftDateRange
                val dateBannerText = if (dateDisplayRange != null) {
                    val locale = LocalConfiguration.current.locales.get(0)
                    val formatter = DateTimeFormatter.ofPattern("MMM dd", locale)
                    val yearFormatter = DateTimeFormatter.ofPattern("yyyy", locale)
                    "${dateDisplayRange.start.format(formatter)} – ${dateDisplayRange.end.format(formatter)}, ${dateDisplayRange.end.format(yearFormatter)}"
                } else {
                    stringResource(R.string.producer_orders_filter_date_all_time)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { showCustomDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIcon(
                                id = R.drawable.ic_schedule,
                                contentDescription = null,
                                tint = SelectedChipBg,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = dateBannerText,
                                style = FruitLogixTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        AppIcon(
                            id = R.drawable.ic_edit,
                            contentDescription = stringResource(R.string.producer_orders_filter_date_custom_range),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ----------------------------------------------------
                // SECCIÓN 2: ORDER STATUS
                // ----------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppIcon(
                            id = R.drawable.ic_dispatch_truck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.producer_orders_filter_order_status),
                            style = FruitLogixTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.producer_orders_filter_status_selected_format,
                            draftStatuses.size
                        ),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val officialStatuses = listOf(
                        OrderStatus.PENDING,
                        OrderStatus.IN_PREPARATION,
                        OrderStatus.IN_TRANSIT,
                        OrderStatus.DELIVERED,
                        OrderStatus.REJECTED,
                        OrderStatus.CANCELLED
                    )

                    for (status in officialStatuses) {
                        val isSelected = status in draftStatuses
                        val count = statusCounts[status] ?: 0
                        val statusLabel = when (status) {
                            OrderStatus.PENDING -> stringResource(R.string.producer_orders_chip_pending)
                            OrderStatus.IN_PREPARATION -> stringResource(R.string.producer_orders_chip_in_preparation)
                            OrderStatus.IN_TRANSIT -> stringResource(R.string.producer_orders_chip_in_transit)
                            OrderStatus.DELIVERED -> stringResource(R.string.producer_orders_chip_delivered)
                            OrderStatus.REJECTED -> stringResource(R.string.producer_orders_chip_rejected)
                            OrderStatus.CANCELLED -> stringResource(R.string.producer_orders_chip_cancelled)
                        }

                        FilterPillChip(
                            label = statusLabel,
                            count = if (count > 0 || isSelected) count else null,
                            isSelected = isSelected,
                            onClick = {
                                draftStatuses = if (isSelected) {
                                    draftStatuses - status
                                } else {
                                    draftStatuses + status
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ----------------------------------------------------
                // SECCIÓN 3: CLIENT DESTINATION
                // ----------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppIcon(
                            id = R.drawable.ic_warehouse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.producer_orders_filter_client_destination),
                            style = FruitLogixTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Select All text action
                    Text(
                        text = stringResource(R.string.producer_orders_filter_select_all),
                        style = FruitLogixTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SelectedChipBg,
                        modifier = Modifier
                            .clickable {
                                draftClientIds = if (draftClientIds.size == availableClients.size) {
                                    emptySet()
                                } else {
                                    availableClients.toSet()
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Container Card with Checkboxes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    val isAllClientsChecked = draftClientIds.isEmpty() || draftClientIds.size == availableClients.size

                    // "All clients" row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .clickable {
                                draftClientIds = if (isAllClientsChecked) {
                                    emptySet()
                                } else {
                                    availableClients.toSet()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                AppIcon(
                                    id = R.drawable.ic_warehouse,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = stringResource(R.string.producer_orders_filter_all_clients),
                                style = FruitLogixTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Checkbox(
                            checked = isAllClientsChecked,
                            onCheckedChange = { checked ->
                                draftClientIds = if (checked) {
                                    availableClients.toSet()
                                } else {
                                    emptySet()
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SelectedChipBg,
                                checkmarkColor = Color.White,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // Individual client rows
                    for (client in availableClients) {
                        val activeOrdersCount = allOrders.count {
                            it.clientName == client &&
                            it.status in setOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.IN_TRANSIT)
                        }

                        val isChecked = client in draftClientIds

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .clickable {
                                    draftClientIds = if (isChecked) {
                                        draftClientIds - client
                                    } else {
                                        draftClientIds + client
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AppIcon(
                                        id = R.drawable.ic_producers,
                                        contentDescription = null,
                                        tint = SelectedChipBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = client,
                                        style = FruitLogixTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.producer_orders_filter_active_orders_format,
                                            activeOrdersCount
                                        ),
                                        style = FruitLogixTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    draftClientIds = if (checked) {
                                        draftClientIds + client
                                    } else {
                                        draftClientIds - client
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SelectedChipBg,
                                    checkmarkColor = Color.White,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ----------------------------------------------------
            // 3. FIXED FOOTER (Outside Scroll)
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary Button: Clear
                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .height(50.dp)
                            .clip(FruitLogixTheme.shapes.Pill)
                            .background(UnselectedChipBg)
                            .clickable {
                                selectedDateOption = DateFilterOption.ALL_TIME
                                draftDateRange = null
                                draftStatuses = emptySet()
                                draftClientIds = emptySet()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppIcon(
                                id = R.drawable.ic_diagnostic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.producer_orders_filter_clear),
                                style = FruitLogixTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Primary Button: Apply Filters (N)
                    Box(
                        modifier = Modifier
                            .weight(0.62f)
                            .height(50.dp)
                            .clip(FruitLogixTheme.shapes.Pill)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                val appliedFilters = initialFilters.copy(
                                    statuses = draftStatuses,
                                    dateRange = draftDateRange,
                                    clientIds = if (draftClientIds.size == availableClients.size) {
                                        emptySet()
                                    } else {
                                        draftClientIds
                                    }
                                )
                                onApplyFilters(appliedFilters)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.producer_orders_filter_apply_format,
                                    totalActiveCount
                                ),
                                style = FruitLogixTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            AppIcon(
                                id = R.drawable.ic_arrow_forward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPillChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val bg = if (isSelected) SelectedChipBg else UnselectedChipBg
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    val chipDescription = stringResource(
        if (isSelected) R.string.producer_orders_cd_chip_selected else R.string.producer_orders_cd_chip_not_selected,
        label
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(FruitLogixTheme.shapes.Pill)
            .clickable(
                onClick = onClick,
                role = Role.Checkbox
            )
            .semantics {
                contentDescription = chipDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(38.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(bg)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelected) {
                    AppIcon(
                        id = R.drawable.ic_task_check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = label,
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )

                if (count != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) BadgeSelectedBg else BadgeUnselectedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            style = TextStyleForBadge(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextStyleForBadge() = androidx.compose.ui.text.TextStyle(
    fontSize = 11.sp,
    lineHeight = 11.sp
)

