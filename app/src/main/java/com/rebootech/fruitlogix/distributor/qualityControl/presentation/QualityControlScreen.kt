package com.rebootech.fruitlogix.distributor.qualityControl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotSource
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageCause
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageStage
import com.rebootech.fruitlogix.distributor.qualityControl.presentation.components.FieldInspectionSheet
import com.rebootech.fruitlogix.distributor.qualityControl.presentation.components.RejectLotSheet
import com.rebootech.fruitlogix.distributor.qualityControl.presentation.components.VoiceIncidentSheet
import com.rebootech.fruitlogix.distributor.qualityControl.presentation.components.rejectionLabel
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SegmentedTabs
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatPen
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

/**
 * Quality control in two tabs: lots (approve / reject, plus field inspection and voice
 * incident as actions) and monthly shrinkage.
 */
@Composable
fun QualityControlScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QualityControlViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.asString(context))
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item { BackHeader(title = stringResource(R.string.qc_title), subtitle = stringResource(R.string.qc_subtitle), onBack = onBack) }
            item {
                SegmentedTabs(
                    options = listOf(
                        QualityTab.LOTS to stringResource(R.string.qc_tab_lots, state.lots.size),
                        QualityTab.SHRINKAGE to stringResource(R.string.qc_tab_shrinkage)
                    ),
                    selected = state.tab,
                    onSelect = viewModel::onTabChange
                )
            }
            when (state.tab) {
                QualityTab.LOTS -> lotsTab(state, viewModel)
                QualityTab.SHRINKAGE -> shrinkageTab(state, viewModel)
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    state.sheet?.let { QualitySheetHost(it, state, viewModel) }
}

// Lots ---------------------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.lotsTab(state: QualityUiState, viewModel: QualityControlViewModel) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            SecondaryButton(
                text = stringResource(R.string.qc_action_field),
                onClick = { viewModel.openSheet(QualitySheet.FieldInspection) },
                modifier = Modifier.weight(1f),
                leadingIcon = { AppIcon(id = R.drawable.ic_task_check, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
            )
            SecondaryButton(
                text = stringResource(R.string.qc_action_voice),
                onClick = { viewModel.openSheet(QualitySheet.VoiceIncident) },
                modifier = Modifier.weight(1f),
                leadingIcon = { AppIcon(id = R.drawable.ic_mic, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
            )
        }
    }
    item {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            SelectableChip(
                label = stringResource(R.string.dist_orders_filter_count, stringResource(R.string.qc_filter_all), state.lots.size),
                selected = state.status == null,
                onClick = { viewModel.onStatusChange(null) }
            )
            LotStatus.entries.forEach { status ->
                SelectableChip(
                    label = stringResource(R.string.dist_orders_filter_count, stringResource(lotStatusLabel(status)), state.count(status)),
                    selected = state.status == status,
                    onClick = { viewModel.onStatusChange(status) }
                )
            }
        }
    }
    val visible = state.visibleLots
    if (visible.isEmpty()) {
        item { EmptyState(title = stringResource(R.string.qc_empty), message = "") }
    } else {
        items(visible, key = { it.id }) { lot ->
            LotCard(
                lot = lot,
                state = state,
                onApprove = { viewModel.approve(lot) },
                onReject = { viewModel.openSheet(QualitySheet.Reject(lot)) }
            )
        }
    }
}

private fun lotStatusLabel(status: LotStatus) = when (status) {
    LotStatus.PENDING -> R.string.qc_status_pending
    LotStatus.APPROVED -> R.string.qc_status_approved
    LotStatus.REJECTED -> R.string.qc_status_rejected
}

@Composable
private fun celsius(value: Double) = "${formatDecimal(value)} °C"

@Composable
private fun LotCard(lot: QualityLot, state: QualityUiState, onApprove: () -> Unit, onReject: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val (badgeType, badgeRes) = when (lot.status) {
        LotStatus.PENDING -> StatusBadgeType.WARNING to R.string.qc_status_pending
        LotStatus.APPROVED -> StatusBadgeType.SUCCESS to R.string.qc_status_approved
        LotStatus.REJECTED -> StatusBadgeType.DANGER to R.string.qc_status_rejected
    }
    val threshold = state.thresholdFor(lot.productName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(lot.lotCode, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface, modifier = Modifier.weight(1f))
            StatusBadge(text = stringResource(badgeRes), type = badgeType, maxLines = 1, softWrap = false)
        }
        if (lot.pendingSync) {
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(text = stringResource(R.string.qc_pending_sync), type = StatusBadgeType.INFO, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        when (lot.source) {
            LotSource.VOICE_INCIDENT -> Text(lot.incidentDescription.orEmpty(), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
            else -> Text(lot.productName, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        }
        Text(
            text = when (lot.source) {
                LotSource.RECEPTION -> listOfNotNull(
                    stringResource(R.string.qc_from_producer, lot.producerName),
                    lot.orderId?.let { stringResource(R.string.qc_order, it) }
                ).joinToString(" · ")
                LotSource.FIELD_INSPECTION -> stringResource(R.string.qc_source_field, formatTimeAgo(lot.recordedAt))
                LotSource.VOICE_INCIDENT -> stringResource(R.string.qc_source_voice, formatTimeAgo(lot.recordedAt))
            },
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        if (lot.celsius != null || lot.humidityPercent != null) {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            lot.celsius?.let { t ->
                val outOfRange = threshold != null && (t < threshold.minTempCelsius || t > threshold.maxTempCelsius)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(id = R.drawable.ic_thermostat, contentDescription = null, tint = if (outOfRange) scheme.error else colors.coldContent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.qc_temp, celsius(t)) + (threshold?.let {
                            " · " + stringResource(R.string.qc_range, stringResource(R.string.sensors_temp_range, celsius(it.minTempCelsius), celsius(it.maxTempCelsius)))
                        } ?: ""),
                        style = FruitLogixTheme.extraTypography.dataMonoSmall,
                        color = if (outOfRange) scheme.error else scheme.onSurfaceVariant
                    )
                }
            }
            lot.humidityPercent?.let { h ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(id = R.drawable.ic_humidity, contentDescription = null, tint = colors.coldContent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.qc_humidity, h.toInt()), style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
                }
            }
        }

        lot.voiceNoteSeconds?.let { seconds ->
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Row(
                modifier = Modifier
                    .whiteCard(FruitLogixTheme.shapes.Tag)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(id = R.drawable.ic_mic, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.qc_voice_note, String.format(java.util.Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)),
                    style = FruitLogixTheme.typography.labelMedium,
                    color = scheme.primary
                )
            }
        }

        lot.rejection?.let { reason ->
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            val text = listOf(stringResource(rejectionLabel(reason.kind)), reason.note).filter { it.isNotBlank() }.joinToString(" · ")
            Text(stringResource(R.string.qc_rejected_because, text), style = FruitLogixTheme.typography.bodyMedium, color = scheme.error)
        }

        if (lot.status == LotStatus.PENDING) {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                SecondaryButton(text = stringResource(R.string.qc_reject), onClick = onReject, modifier = Modifier.weight(1f))
                PrimaryButton(text = stringResource(R.string.qc_approve), onClick = onApprove, modifier = Modifier.weight(1f))
            }
        }
    }
}

// Shrinkage (US73) ------------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.shrinkageTab(state: QualityUiState, viewModel: QualityControlViewModel) {
    item {
        val locale = appLocale()
        val format = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            state.months.forEach { month ->
                SelectableChip(
                    label = month.format(format).replaceFirstChar { it.titlecase(locale) },
                    selected = month == state.month,
                    onClick = { viewModel.onMonthChange(month) }
                )
            }
        }
    }
    val summary = state.shrinkage
    if (summary.isEmpty) {
        item {
            EmptyState(
                title = stringResource(R.string.qc_shrink_empty_title),
                message = stringResource(R.string.qc_shrink_empty_message),
                icon = { AppIcon(id = R.drawable.ic_verified, contentDescription = null, tint = FruitLogixTheme.colors.successContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconBox)) }
            )
        }
        return
    }
    item {
        val number = NumberFormat.getIntegerInstance(appLocale())
        val scheme = MaterialTheme.colorScheme
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            KpiCard(
                caption = stringResource(R.string.qc_shrink_total),
                value = stringResource(R.string.qc_shrink_kg, number.format(summary.totalKg)),
                icon = { AppIcon(id = R.drawable.ic_inventory, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) },
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                caption = stringResource(R.string.qc_shrink_cost),
                value = formatPen(summary.totalCost, decimals = 0),
                icon = { AppIcon(id = R.drawable.ic_invoices, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    items(summary.records, key = { it.id }) { ShrinkageCard(it) }
}

@Composable
private fun ShrinkageCard(record: ShrinkageRecord) {
    val scheme = MaterialTheme.colorScheme
    val number = NumberFormat.getIntegerInstance(appLocale())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(
                text = stringResource(
                    when (record.stage) {
                        ShrinkageStage.HARVEST -> R.string.qc_stage_harvest
                        ShrinkageStage.TRANSPORT -> R.string.qc_stage_transport
                        ShrinkageStage.RECEPTION -> R.string.qc_stage_reception
                    }
                ),
                type = StatusBadgeType.NEUTRAL,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.qc_shrink_line, number.format(record.lostKg), formatPen(record.costPen)),
                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                color = scheme.error
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(record.productName, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        Text(
            text = stringResource(
                when (record.cause) {
                    ShrinkageCause.THERMAL_EXCURSION -> R.string.qc_cause_thermal
                    ShrinkageCause.MECHANICAL_DAMAGE -> R.string.qc_cause_damage
                    ShrinkageCause.UNDERSIZE -> R.string.qc_cause_undersize
                    ShrinkageCause.ROUTE_DELAY -> R.string.qc_cause_delay
                }
            ),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

// Sheets --------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualitySheetHost(sheet: QualitySheet, state: QualityUiState, viewModel: QualityControlViewModel) {
    val scheme = MaterialTheme.colorScheme
    val error = state.sheetError?.let { stringResource(it) }
    val title = when (sheet) {
        is QualitySheet.Reject -> stringResource(R.string.qc_reject_title, sheet.lot.lotCode)
        QualitySheet.FieldInspection -> stringResource(R.string.qc_action_field)
        QualitySheet.VoiceIncident -> stringResource(R.string.qc_action_voice)
    }
    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg)
        ) {
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
            when (sheet) {
                is QualitySheet.Reject -> RejectLotSheet(error = error, onReject = { kind, note -> viewModel.reject(sheet.lot, kind, note) })
                QualitySheet.FieldInspection -> FieldInspectionSheet(errors = state.fieldErrors, onSave = viewModel::saveFieldInspection)
                QualitySheet.VoiceIncident -> VoiceIncidentSheet(error = error, onSave = viewModel::saveVoiceIncident)
            }
        }
    }
}
