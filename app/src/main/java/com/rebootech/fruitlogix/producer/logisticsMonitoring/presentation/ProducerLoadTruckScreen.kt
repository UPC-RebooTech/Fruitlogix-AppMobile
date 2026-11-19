package com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.LoadBlocker
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.LoadCheck
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.PickupTruck
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.TruckLoad
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The distributor's truck arrives at the farm: the producer checks the cargo
 * and confirms the dispatch. The order then goes "in transit".
 */
@Composable
fun ProducerLoadTruckScreen(
    onNavigateBack: () -> Unit,
    onDone: () -> Unit,
    onOpenChat: (orderId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoadTruckViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val load = state.load
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = stringResource(if (state.isDispatched) R.string.load_dispatched_title else R.string.load_title),
                subtitle = load?.let { stringResource(R.string.load_subtitle, it.orderId, it.clientName) },
                onBack = onNavigateBack
            )

            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = scheme.primary)
                }

                load == null -> Text(
                    text = stringResource(R.string.load_not_found),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )

                state.isDispatched -> DispatchedContent(load, state.dispatchedAt)

                else -> {
                    if (load.blockers.isNotEmpty()) BlockedNotice(load.blockers)
                    load.truck?.let { TruckCard(load, it) }
                    load.truck?.let { DriverCard(it) }
                    CargoSection(load)
                    if (load.canLoad) {
                        ChecklistSection(
                            load = load,
                            checked = state.checked,
                            onToggle = viewModel::toggle
                        )
                    }
                }
            }
        }

        if (load != null) {
            Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
                if (state.isDispatched) {
                    PrimaryButton(
                        text = stringResource(R.string.load_done),
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextActionButton(
                        text = stringResource(R.string.order_detail_message),
                        onClick = { onOpenChat(load.orderId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (load.canLoad) {
                    PrimaryButton(
                        text = stringResource(R.string.load_confirm),
                        onClick = { showConfirm = true },
                        enabled = state.canConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            AppIcon(id = R.drawable.ic_dispatch_truck, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                        }
                    )
                } else {
                    TextActionButton(
                        text = stringResource(R.string.order_detail_message),
                        onClick = { onOpenChat(load.orderId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showConfirm && load != null) {
        ConfirmDialog(
            title = stringResource(R.string.load_confirm_dialog_title, load.orderId),
            message = stringResource(R.string.load_confirm_dialog_message),
            confirmText = stringResource(R.string.load_confirm_dialog_yes),
            onConfirm = {
                showConfirm = false
                viewModel.confirmDispatch()
            },
            onDismiss = { showConfirm = false }
        )
    }
}

@Composable
private fun BlockedNotice(blockers: List<LoadBlocker>) {
    val colors = FruitLogixTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(colors.attentionContainer)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = colors.attentionContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(stringResource(R.string.load_blocked_title), style = FruitLogixTheme.typography.titleSmall, color = colors.onAttentionContainer)
        }
        blockers.forEach { blocker ->
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            Text(
                text = "• " + stringResource(
                    when (blocker) {
                        LoadBlocker.QUALITY_NOT_APPROVED -> R.string.load_blocked_quality
                        LoadBlocker.NO_TRUCK_ASSIGNED -> R.string.load_blocked_truck
                    }
                ),
                style = FruitLogixTheme.typography.bodyMedium,
                color = colors.onAttentionContainer
            )
        }
    }
}

@Composable
private fun TruckCard(load: TruckLoad, truck: PickupTruck) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val locale = appLocale()
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.load_truck_label).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = colors.onInverseSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (load.qualityApproved) {
                    StatusBadge(
                        text = stringResource(R.string.load_quality_approved),
                        type = StatusBadgeType.SUCCESS,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(truck.plate, style = FruitLogixTheme.extraTypography.kpiNumber, color = scheme.inverseOnSurface)
            Text(truck.vehicleType, style = FruitLogixTheme.typography.bodyMedium, color = colors.onInverseSurfaceVariant)
            truck.etaFormatted?.let { eta ->
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(id = R.drawable.ic_eta, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    Text(stringResource(R.string.load_eta, eta), style = FruitLogixTheme.typography.titleMedium, color = scheme.inverseOnSurface)
                }
            }
            if (load.pickupFrom != null && load.pickupTo != null) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                Text(
                    text = stringResource(R.string.load_pickup_window, load.pickupFrom.format(timeFormat), load.pickupTo.format(timeFormat)),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = colors.onInverseSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DriverCard(truck: PickupTruck) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val callDescription = stringResource(R.string.load_call_driver, truck.driverName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(scheme.surfaceContainerLow)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.minTouch)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(initialsOf(truck.driverName), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSecondaryContainer)
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.load_driver_label), style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Text(truck.driverName, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Text(truck.driverPhone, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
        }
        FilledIconButton(
            onClick = {
                val number = truck.driverPhone.filter { it.isDigit() || it == '+' }
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
            },
            modifier = Modifier.size(FruitLogixTheme.sizing.fieldTouch),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            )
        ) {
            AppIcon(id = R.drawable.ic_call, contentDescription = callDescription, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
        }
    }
}

@Composable
private fun CargoSection(load: TruckLoad) {
    val scheme = MaterialTheme.colorScheme
    val number = NumberFormat.getNumberInstance(appLocale())
    DetailSection(title = stringResource(R.string.load_section_cargo)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        load.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FruitLogixTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.productName, style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurface, modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.producer_orders_tile_volume_format, number.format(item.kg)),
                    style = FruitLogixTheme.extraTypography.dataMono,
                    color = scheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FruitLogixTheme.shapes.Button)
                .background(scheme.secondaryContainer)
                .padding(FruitLogixTheme.spacing.inner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.load_total), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSecondaryContainer, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.producer_orders_tile_volume_format, number.format(load.totalKg)),
                style = FruitLogixTheme.extraTypography.dataMono,
                color = scheme.onSecondaryContainer
            )
        }
        if (load.coldChainMinC != null && load.coldChainMaxC != null) {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(id = R.drawable.ic_logistics_snowflake, contentDescription = null, tint = FruitLogixTheme.colors.coldContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.order_detail_cold_chain_value, formatDecimal(load.coldChainMinC), formatDecimal(load.coldChainMaxC)),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChecklistSection(
    load: TruckLoad,
    checked: Set<LoadCheck>,
    onToggle: (LoadCheck) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val total = LoadCheck.entries.size
    DetailSection(title = stringResource(R.string.load_section_checklist)) {
        Text(
            text = if (checked.size == total) {
                stringResource(R.string.load_checklist_progress, checked.size, total)
            } else {
                stringResource(R.string.load_checklist_hint) + " " + stringResource(R.string.load_checklist_progress, checked.size, total)
            },
            style = FruitLogixTheme.typography.bodySmall,
            color = if (checked.size == total) FruitLogixTheme.colors.successContent else scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        LoadCheck.entries.forEach { check ->
            val label = when (check) {
                LoadCheck.CRATES_COUNTED -> stringResource(R.string.load_check_crates)
                LoadCheck.LOT_LABELS -> stringResource(R.string.load_check_labels)
                LoadCheck.TRUCK_COOLING -> if (load.coldChainMinC != null && load.coldChainMaxC != null) {
                    stringResource(R.string.load_check_cooling_range, formatDecimal(load.coldChainMinC), formatDecimal(load.coldChainMaxC))
                } else {
                    stringResource(R.string.load_check_cooling)
                }
                LoadCheck.GUIDE_SIGNED -> stringResource(R.string.load_check_guide)
            }
            CheckRow(label = label, checked = check in checked, onToggle = { onToggle(check) })
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(if (checked) FruitLogixTheme.colors.successContainer else scheme.surfaceContainerLow)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = scheme.primary,
                checkmarkColor = scheme.onPrimary,
                uncheckedColor = scheme.outline
            ),
            modifier = Modifier.padding(FruitLogixTheme.spacing.xs)
        )
        Text(
            text = label,
            style = FruitLogixTheme.typography.bodyLarge,
            color = if (checked) FruitLogixTheme.colors.onSuccessContainer else scheme.onSurface,
            modifier = Modifier.padding(vertical = FruitLogixTheme.spacing.xs)
        )
    }
    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
}

@Composable
private fun DispatchedContent(load: TruckLoad, dispatchedAt: LocalDateTime?) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = R.drawable.ic_dispatch_truck, contentDescription = null, tint = colors.onSuccessContainer, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Text(
            text = stringResource(R.string.load_dispatched_message, load.orderId, load.distributorName),
            style = FruitLogixTheme.typography.titleMedium,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )
        dispatchedAt?.let {
            Text(
                text = stringResource(R.string.load_dispatched_at, formatRelativeDateTime(it)),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        StatusBadge(
            text = stringResource(R.string.load_in_transit),
            type = StatusBadgeType.INFO,
            maxLines = 1,
            softWrap = false
        )
    }
    load.truck?.let { DriverCard(it) }
    CargoSection(load)
}
