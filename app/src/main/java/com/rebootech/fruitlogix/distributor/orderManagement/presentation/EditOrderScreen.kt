package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.OrderFormFields
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.ProducerOption
import com.rebootech.fruitlogix.shared.ui.components.ActionRow
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Edit (and delete) an order. Orders that are no longer pending are shown read-only,
 * with the reason, so the user is never left wondering why nothing can be changed.
 */
@Composable
fun EditOrderScreen(
    onBackClick: () -> Unit,
    onOrderUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditOrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val order = state.order

    val savedMessage = stringResource(R.string.dist_order_saved)
    val assignedMessage = stringResource(R.string.dist_order_producer_assigned)
    val deletedMessage = order?.let { stringResource(R.string.dist_orders_deleted, it.id) }.orEmpty()
    LaunchedEffect(state.outcome) {
        when (state.outcome) {
            EditOutcome.SAVED -> Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            EditOutcome.DELETED -> Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
            EditOutcome.PRODUCER_ASSIGNED -> Toast.makeText(context, assignedMessage, Toast.LENGTH_SHORT).show()
            null -> return@LaunchedEffect
        }
        onOrderUpdated()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        BackHeader(
            title = stringResource(R.string.dist_order_edit_title, order?.id.orEmpty()),
            subtitle = order?.let { clientLabel(it) },
            onBack = onBackClick,
            actions = {
                if (order != null) {
                    StatusBadge(
                        text = stringResource(orderStatusLabel(order.status)),
                        type = orderStatusBadge(order.status),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        )

        if (order == null) {
            Text(stringResource(R.string.dist_order_not_found), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            return@Column
        }

        if (!state.isEditable) {
            LockedNotice(statusLabel = stringResource(orderStatusLabel(order.status)))
        } else if (state.onlyDateEditable) {
            InfoNotice(stringResource(R.string.dist_order_server_only_date))
        }

        ProducerSection(
            producerName = state.producerName,
            producerId = order.producerId,
            canAssign = state.isEditable,
            assignFailed = state.assignFailed,
            enabled = !state.isSaving,
            onAssign = viewModel::openProducerChoice
        )

        OrderFormFields(
            form = state.form,
            onClientChange = viewModel::onClientChange,
            onProductChange = viewModel::onProductChange,
            onQuantityChange = viewModel::onQuantityChange,
            onDateChange = viewModel::onDateChange,
            enabled = state.isEditable && !state.isSaving,
            contentEnabled = state.isEditable && !state.onlyDateEditable && !state.isSaving,
            currentClientLabel = clientLabel(order)
        )

        if (state.deleteExpired) {
            Text(stringResource(R.string.dist_order_delete_expired), style = FruitLogixTheme.typography.bodyMedium, color = scheme.error)
        }

        if (state.isEditable) {
            Spacer(modifier = Modifier.height(spacing.xs))
            PrimaryButton(
                text = stringResource(R.string.dist_order_edit_submit),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.save()
                },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = viewModel::askDelete,
                enabled = !state.isSaving,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(FruitLogixTheme.sizing.minTouch)
            ) {
                AppIcon(id = R.drawable.ic_close, contentDescription = null, tint = scheme.error, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(stringResource(R.string.dist_order_delete), style = FruitLogixTheme.typography.labelLarge, color = scheme.error)
            }
        }
    }

    if (state.choosingProducer) {
        ProducerChoiceSheet(
            producers = state.producers,
            onPick = viewModel::assignProducer,
            onDismiss = viewModel::closeProducerChoice
        )
    }

    if (state.showDeleteDialog && order != null) {
        ConfirmDialog(
            title = stringResource(R.string.dist_order_delete_title, order.id),
            message = stringResource(R.string.dist_order_delete_message),
            confirmText = stringResource(R.string.dist_order_delete_confirm),
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
            destructive = true
        )
    }
}

@Composable
private fun LockedNotice(statusLabel: String) {
    val colors = FruitLogixTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(colors.attentionContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.Top
    ) {
        AppIcon(id = R.drawable.ic_fleet_lock, contentDescription = null, tint = colors.attentionContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Column {
            Text(stringResource(R.string.dist_order_locked_title), style = FruitLogixTheme.typography.titleSmall, color = colors.onAttentionContainer)
            Text(stringResource(R.string.dist_order_locked_message, statusLabel), style = FruitLogixTheme.typography.bodyMedium, color = colors.onAttentionContainer)
        }
    }
}
/** Neutral note (e.g. what the server allows to change). */
@Composable
private fun InfoNotice(text: String) {
    val colors = FruitLogixTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(colors.coldContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.Top
    ) {
        AppIcon(id = R.drawable.ic_info, contentDescription = null, tint = colors.coldContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(text, style = FruitLogixTheme.typography.bodyMedium, color = colors.onColdContainer)
    }
}

/** Assigned producer, or the action that assigns one (moves a pending order to "in preparation"). */
@Composable
private fun ProducerSection(
    producerName: String?,
    producerId: Int?,
    canAssign: Boolean,
    assignFailed: Boolean,
    enabled: Boolean,
    onAssign: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .padding(FruitLogixTheme.spacing.inner),
        verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
    ) {
        Text(stringResource(R.string.dist_order_producer), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
        Text(
            text = when {
                producerName != null -> producerName
                producerId != null -> stringResource(R.string.dist_order_producer_number, producerId)
                else -> stringResource(R.string.dist_order_producer_not_assigned)
            },
            style = FruitLogixTheme.typography.bodyLarge,
            color = if (producerId != null) scheme.onSurface else scheme.onSurfaceVariant
        )
        if (canAssign && producerId == null) {
            Text(stringResource(R.string.dist_order_producer_assign_hint), style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            SecondaryButton(
                text = stringResource(R.string.dist_order_producer_assign),
                onClick = onAssign,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { AppIcon(id = R.drawable.ic_assign_producer, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
            )
        }
        if (assignFailed) {
            Text(stringResource(R.string.dist_order_producer_assign_failed), style = FruitLogixTheme.typography.bodySmall, color = scheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProducerChoiceSheet(
    producers: List<ProducerOption>,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
        ) {
            Text(stringResource(R.string.dist_order_producer_assign), style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Text(stringResource(R.string.dist_order_producer_assign_hint), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            if (producers.isEmpty()) {
                Text(stringResource(R.string.dist_order_producer_none_available), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            producers.forEach { producer ->
                ActionRow(
                    iconRes = R.drawable.ic_producers,
                    title = producer.name,
                    subtitle = stringResource(R.string.dist_order_producer_number, producer.id),
                    onClick = { onPick(producer.id) }
                )
            }
        }
    }
}
