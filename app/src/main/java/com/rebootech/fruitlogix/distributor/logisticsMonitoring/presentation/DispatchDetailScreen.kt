package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ColdChainCertificate
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlertStatus
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.CertificateSheetContent
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.DelaySheetContent
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.DeliverySheetContent
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.ReeferSheetContent
import com.rebootech.fruitlogix.shared.ui.components.SegmentedTabs
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.StatusSheetContent
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.TemperatureTrendChart
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components.TraceabilitySheetContent
import com.rebootech.fruitlogix.shared.core.model.UiText
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.SectionHeader
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate
import java.time.LocalTime

/**
 * Dispatch detail in five sections: header (with Call) → temperature with forecast →
 * route with checkpoints → humidity and battery → actions. Each action opens a sheet.
 */
@Composable
fun DispatchDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DispatchDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val item = state.item

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.resolve(context))
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            if (item == null) {
                BackHeader(title = "", onBack = onBackClick)
                Text(stringResource(R.string.dispatch_not_found), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                return@Column
            }
            val d = item.dispatch
            val (badge, badgeType) = dispatchBadge(item)
            BackHeader(
                title = d.unitId,
                subtitle = stringResource(R.string.dispatch_subtitle, d.plate, d.truckModel),
                onBack = onBackClick,
                actions = { StatusBadge(text = badge, type = badgeType, maxLines = 1, softWrap = false) }
            )

            HeaderSection(d)
            TemperatureSection(state, item, onWindowChange = viewModel::onWindowChange)
            RouteSection(d)
            TilesSection(item)
            ActionsSection(state, d, onOpen = viewModel::openSheet)
            Spacer(modifier = Modifier.height(spacing.lg))
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    if (item != null && state.sheet != null) {
        ActionSheet(state, item, viewModel, onShare = { shareCertificate(context, it) })
    }
}

/** Resolves a message whose arguments may be texts themselves (e.g. a status label). */
private fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Plain -> value
    is UiText.StringResource -> context.getString(id, *args.map { if (it is UiText) it.resolve(context) else it }.toTypedArray())
}

// 1. Header ---------------------------------------------------------------------

@Composable
private fun HeaderSection(d: ActiveDispatch) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val callDescription = stringResource(R.string.dispatch_call, d.driver.name)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(stringResource(R.string.dispatch_order, d.orderId), style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.primary)
        Text(stringResource(R.string.fleet_cargo, d.productName, d.pallets), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        Text(
            text = "${stringResource(R.string.fleet_route, d.originName, d.destinationName)} · ${stringResource(R.string.dispatch_producer, d.producerName)}",
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(id = R.drawable.ic_person, contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            }
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.dispatch_driver), style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                Text(d.driver.name, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
                Text(d.driver.phone, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
            }
            FilledIconButton(
                onClick = {
                    val number = d.driver.phone.filter { it.isDigit() || it == '+' }
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                },
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.fieldTouch)
                    .semantics { contentDescription = callDescription },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer)
            ) {
                AppIcon(id = R.drawable.ic_call, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
            }
        }
    }
}

// 2. Temperature (highlighted, dark) ----------------------------------------------

@Composable
private fun TemperatureSection(state: DispatchDetailUiState, item: DispatchItem, onWindowChange: (ChartWindow) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val threshold = item.threshold
    val prediction = item.prediction

    FruitLogixCard(variant = CardVariant.Dark, shape = FruitLogixTheme.shapes.HeroCard, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Text(stringResource(R.string.dispatch_temp_label).uppercase(), style = FruitLogixTheme.typography.labelSmall, color = colors.onInverseSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.celsius?.let { celsius(it) } ?: "—",
                    style = FruitLogixTheme.extraTypography.kpiNumber,
                    color = if (item.risk == ReeferRisk.OK) scheme.inverseOnSurface else colors.attentionContainer
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                prediction?.takeIf { it.ratePerMinute > 0 }?.let {
                    Text(
                        text = stringResource(R.string.dispatch_temp_rate, "+" + String.format(appLocale(), "%.2f °C", it.ratePerMinute)),
                        style = FruitLogixTheme.extraTypography.dataMonoSmall,
                        color = colors.onInverseSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            threshold?.let {
                Text(
                    text = stringResource(R.string.dispatch_temp_range, celsius(it.minTempCelsius), celsius(it.maxTempCelsius)),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = colors.onInverseSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            // Forecast inside the card: what will happen, in one sentence
            val (forecast, forecastColor) = when {
                item.risk == ReeferRisk.OUT_OF_RANGE -> stringResource(R.string.dispatch_forecast_out) to scheme.error
                prediction?.status == PredictiveAlertStatus.PREDICTIVE && threshold != null ->
                    stringResource(R.string.dispatch_forecast_risk, celsius(threshold.maxTempCelsius), prediction.minutesToBreach) to colors.attentionContainer
                else -> stringResource(R.string.dispatch_forecast_ok) to scheme.inversePrimary
            }
            Text(forecast, style = FruitLogixTheme.typography.titleSmall, color = forecastColor)

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            SegmentedTabs(
                options = listOf(
                    ChartWindow.ONE_HOUR to stringResource(R.string.dispatch_filter_1h),
                    ChartWindow.SIX_HOURS to stringResource(R.string.dispatch_filter_6h),
                    ChartWindow.TRIP to stringResource(R.string.dispatch_filter_trip)
                ),
                selected = state.window,
                onSelect = onWindowChange
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            val readings = state.chartReadings
            if (readings.size < 2 || threshold == null) {
                Text(stringResource(R.string.dispatch_no_readings), style = FruitLogixTheme.typography.bodyMedium, color = colors.onInverseSurfaceVariant)
            } else {
                val oldest = readings.maxOf { it.minutesAgo }
                val description = stringResource(
                    R.string.dispatch_chart_cd,
                    item.celsius?.let { celsius(it) } ?: "—",
                    celsius(threshold.minTempCelsius),
                    celsius(threshold.maxTempCelsius)
                )
                TemperatureTrendChart(
                    readings = readings,
                    min = threshold.minTempCelsius,
                    max = threshold.maxTempCelsius,
                    minLabel = celsius(threshold.minTempCelsius),
                    maxLabel = celsius(threshold.maxTempCelsius),
                    startLabel = formatTime(java.time.LocalDateTime.now().minusMinutes(oldest.toLong())),
                    endLabel = formatTime(java.time.LocalDateTime.now()),
                    lineColor = scheme.inversePrimary,
                    bandColor = scheme.inversePrimary.copy(alpha = 0.12f),
                    alertColor = scheme.error,
                    labelColor = colors.onInverseSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .semantics { contentDescription = description }
                )
            }
        }
    }
}

// 3. Route with checkpoints --------------------------------------------------------

@Composable
private fun RouteSection(d: ActiveDispatch) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(stringResource(R.string.dispatch_route_title), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        LimeProgressBar(progress = d.progress, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.dispatch_route_progress, d.travelledKm.toInt(), d.totalKm.toInt(), (d.progress * 100).toInt()),
            style = FruitLogixTheme.extraTypography.dataMonoSmall,
            color = scheme.onSurfaceVariant
        )
        Text(stringResource(R.string.dispatch_route_eta, formatTime(today.atTime(d.eta))), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
        if (d.isDelayed) {
            Text(
                text = stringResource(R.string.dispatch_route_delay, d.delayMinutes, d.delayReason?.let { stringResource(delayReasonLabel(it)) } ?: ""),
                style = FruitLogixTheme.typography.bodyMedium,
                color = colors.attentionContent
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        d.checkpoints.forEachIndexed { index, cp ->
            val passed = cp.passedAt != null
            val isCurrent = cp.kind == CheckpointKind.CURRENT
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 20.dp else 14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    cp.hasIssue -> colors.attentionContent
                                    isCurrent -> scheme.primaryContainer
                                    passed -> scheme.primary
                                    else -> scheme.outlineVariant
                                }
                            )
                    )
                    if (index < d.checkpoints.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(30.dp).background(if (passed || isCurrent) scheme.primary else scheme.outlineVariant))
                    }
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cp.name, style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val detail = when {
                        isCurrent -> stringResource(R.string.dispatch_checkpoint_now, cp.km ?: 0)
                        cp.kind == CheckpointKind.DESTINATION -> stringResource(R.string.dispatch_checkpoint_eta, formatTime(today.atTime(d.eta)))
                        cp.passedAt != null -> formatTime(today.atTime(cp.passedAt))
                        else -> ""
                    }
                    Text(detail, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
                    if (cp.hasIssue) {
                        Text(stringResource(R.string.dispatch_checkpoint_issue), style = FruitLogixTheme.typography.bodySmall, color = colors.attentionContent)
                    }
                }
            }
        }
    }
}

// 4. Humidity and battery ---------------------------------------------------------

@Composable
private fun TilesSection(item: DispatchItem) {
    val device = item.device ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
        Tile(
            iconRes = R.drawable.ic_humidity,
            label = stringResource(R.string.dispatch_humidity),
            value = stringResource(R.string.dispatch_percent, device.lastReading.humidityPercent.toInt()),
            alert = item.threshold?.let { device.lastReading.humidityPercent !in it.minHumidityPercent..it.maxHumidityPercent } == true,
            modifier = Modifier.weight(1f)
        )
        Tile(
            iconRes = R.drawable.ic_battery,
            label = stringResource(R.string.dispatch_battery),
            value = stringResource(R.string.dispatch_percent, device.batteryPercent),
            alert = device.isLowBattery,
            modifier = Modifier.weight(1f)
        )
    }
    Text(
        text = stringResource(R.string.dispatch_sensor_line, device.id, formatTimeAgo(device.lastReading.at)),
        style = FruitLogixTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Tile(iconRes: Int, label: String, value: String, alert: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Card)
            .background(if (alert) FruitLogixTheme.colors.attentionContainer else scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        AppIcon(id = iconRes, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(label, style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        Text(value, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
    }
}

// 5. Actions --------------------------------------------------------------------

@Composable
private fun ActionsSection(state: DispatchDetailUiState, d: ActiveDispatch, onOpen: (DispatchSheet) -> Unit) {
    val today = LocalDate.now()
    val certificate = state.certificate
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
        SectionHeader(title = stringResource(R.string.dispatch_actions_title))
        ActionRow(R.drawable.ic_task_check, stringResource(R.string.dispatch_action_status), stringResource(deliveryStateLabel(d.deliveryState))) {
            onOpen(DispatchSheet.STATUS)
        }
        ActionRow(
            R.drawable.ic_thermostat,
            stringResource(R.string.dispatch_action_reefer),
            d.reeferCheckedAt?.let { stringResource(R.string.dispatch_action_reefer_done, formatRelativeDateTime(it)) }
                ?: stringResource(R.string.dispatch_action_reefer_pending)
        ) { onOpen(DispatchSheet.REEFER) }
        if (d.isActive) {
            ActionRow(R.drawable.ic_schedule, stringResource(R.string.dispatch_action_delay), stringResource(R.string.dispatch_action_delay_sub, formatTime(today.atTime(d.eta)))) {
                onOpen(DispatchSheet.DELAY)
            }
            ActionRow(R.drawable.ic_receipt, stringResource(R.string.dispatch_action_delivery), stringResource(R.string.dispatch_action_delivery_sub)) {
                onOpen(DispatchSheet.DELIVERY)
            }
        }
        ActionRow(
            R.drawable.ic_shield_check,
            stringResource(R.string.dispatch_action_certificate),
            certificate?.let { stringResource(R.string.dispatch_action_certificate_sub, it.readingsCount, it.excursions) }
                ?: stringResource(R.string.certificate_no_data)
        ) { onOpen(DispatchSheet.CERTIFICATE) }
        ActionRow(R.drawable.ic_link, stringResource(R.string.dispatch_action_trace), stringResource(R.string.dispatch_action_trace_sub)) {
            onOpen(DispatchSheet.TRACEABILITY)
        }
    }
}

@Composable
private fun ActionRow(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .whiteCard(FruitLogixTheme.shapes.Button)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.iconBox)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = iconRes, contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Text(subtitle, style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        AppIcon(id = R.drawable.ic_chevron_right, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
    }
}

// Sheets ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionSheet(
    state: DispatchDetailUiState,
    item: DispatchItem,
    viewModel: DispatchDetailViewModel,
    onShare: (ColdChainCertificate) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val d = item.dispatch
    val sheet = state.sheet ?: return
    val error = state.sheetError?.let { res -> state.sheetErrorArg?.let { stringResource(res, it) } ?: stringResource(res) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = stringResource(
        when (sheet) {
            DispatchSheet.STATUS -> R.string.dispatch_action_status
            DispatchSheet.REEFER -> R.string.dispatch_action_reefer
            DispatchSheet.DELAY -> R.string.dispatch_action_delay
            DispatchSheet.DELIVERY -> R.string.dispatch_action_delivery
            DispatchSheet.CERTIFICATE -> R.string.dispatch_action_certificate
            DispatchSheet.TRACEABILITY -> R.string.dispatch_action_trace
        }
    )

    ModalBottomSheet(onDismissRequest = viewModel::closeSheet, sheetState = sheetState, containerColor = scheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg)
        ) {
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Text("${d.unitId} · ${stringResource(R.string.dispatch_order, d.orderId)}", style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
            when (sheet) {
                DispatchSheet.STATUS -> StatusSheetContent(current = d.deliveryState, error = error, onSelect = viewModel::changeState)
                DispatchSheet.REEFER -> ReeferSheetContent(
                    productName = d.productName,
                    threshold = item.threshold,
                    currentReading = item.celsius,
                    statusFor = viewModel::reeferStatus,
                    onApprove = viewModel::approveReefer
                )
                DispatchSheet.DELAY -> DelaySheetContent(currentEta = d.eta, error = error, onSend = viewModel::notifyDelay)
                DispatchSheet.DELIVERY -> DeliverySheetContent(
                    destinationName = d.destinationName,
                    error = error,
                    onConfirm = viewModel::confirmDelivery,
                    onReject = viewModel::rejectDelivery
                )
                DispatchSheet.CERTIFICATE -> CertificateSheetContent(certificate = state.certificate, onShare = onShare)
                DispatchSheet.TRACEABILITY -> TraceabilitySheetContent(dispatch = d)
            }
        }
    }
}

private fun shareCertificate(context: Context, c: ColdChainCertificate) {
    val decimal = { v: Double -> String.format(java.util.Locale.getDefault(), "%.1f °C", v) }
    val body = context.getString(
        R.string.certificate_share_text,
        c.orderId, c.unitId, c.plate, c.productName, c.route,
        decimal(c.minCelsius), decimal(c.maxCelsius), c.readingsCount, c.excursions, c.hash
    )
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.dispatch_action_certificate) + " · " + c.orderId)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.certificate_share_title)))
}
