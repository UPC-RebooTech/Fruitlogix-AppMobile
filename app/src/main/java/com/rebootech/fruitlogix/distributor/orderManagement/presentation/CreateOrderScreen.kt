package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.OrderFormFields
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun CreateOrderScreen(
    onBackClick: () -> Unit,
    onOrderRegistered: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        val created = state.created
        if (created != null) {
            CreatedContent(order = created, onBackToOrders = onOrderRegistered, onCreateAnother = viewModel::createAnother)
            return@Column
        }

        BackHeader(
            title = stringResource(R.string.dist_order_create_title),
            subtitle = stringResource(R.string.dist_order_create_subtitle),
            onBack = onBackClick
        )
        OrderFormFields(
            form = state.form,
            onClientChange = viewModel::onClientChange,
            onProductChange = viewModel::onProductChange,
            onQuantityChange = viewModel::onQuantityChange,
            onDateChange = viewModel::onDateChange,
            enabled = !state.isSaving
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        state.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
        PrimaryButton(
            text = stringResource(R.string.dist_order_create_submit),
            onClick = {
                focusManager.clearFocus()
                viewModel.submit()
            },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CreatedContent(order: DistributorOrder, onBackToOrders: () -> Unit, onCreateAnother: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FruitLogixTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = colors.onSuccessContainer, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Text(
            text = stringResource(R.string.dist_order_created_title, order.id),
            style = FruitLogixTheme.typography.headlineSmall,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = "${order.clientName} · ${order.productName} · ${formatQuantity(order.quantityTons, order.pallets)}",
            style = FruitLogixTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.dist_orders_delivery, formatDeliveryDate(order.deliveryDate)),
            style = FruitLogixTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(
            text = stringResource(R.string.dist_order_created_message),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.md))
        PrimaryButton(
            text = stringResource(R.string.dist_order_back_to_orders),
            onClick = onBackToOrders,
            modifier = Modifier.fillMaxWidth()
        )
        TextActionButton(
            text = stringResource(R.string.dist_order_create_another),
            onClick = onCreateAnother
        )
    }
}
