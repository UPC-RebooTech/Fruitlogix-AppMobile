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
import androidx.compose.material3.MaterialTheme
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
    val deletedMessage = order?.let { stringResource(R.string.dist_orders_deleted, it.id) }.orEmpty()
    LaunchedEffect(state.outcome) {
        when (state.outcome) {
            EditOutcome.SAVED -> Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            EditOutcome.DELETED -> Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
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
            subtitle = order?.clientName,
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
        }

        OrderFormFields(
            form = state.form,
            onClientChange = viewModel::onClientChange,
            onProductChange = viewModel::onProductChange,
            onQuantityChange = viewModel::onQuantityChange,
            onDateChange = viewModel::onDateChange,
            enabled = state.isEditable && !state.isSaving
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
