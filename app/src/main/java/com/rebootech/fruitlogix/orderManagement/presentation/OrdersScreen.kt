package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import androidx.compose.runtime.LaunchedEffect

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    viewModel: OrdersViewModel = viewModel(),
    onNewOrderClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    OrdersScreenContent(
        state = state,
        onNewOrderClick = onNewOrderClick,
        modifier = modifier
    )
}

@Composable
private fun OrdersScreenContent(
    state: OrdersUiState,
    onNewOrderClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember {
        mutableStateOf(AppLanguage.ES)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            FruitLogixTopBar(
                title = stringResource(R.string.topbar_title),
                subtitle = stringResource(R.string.topbar_subtitle),
                selectedLanguage = selectedLanguage,
                onLanguageSelected = {
                    selectedLanguage = it
                },
                onNotificationClick = {},
                onProfileClick = {}
            )
        }

        item {
            OrdersHeader(
                orderCount = state.orders.size
            )
        }

        item {
            PrimaryButton(
                text = stringResource(R.string.orders_new_order),
                onClick = onNewOrderClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FruitLogixTheme.spacing.sm)
            )
        }

        when {
            state.isLoading -> {
                item {
                    OrdersLoadingState()
                }
            }

            state.isEmpty -> {
                item {
                    EmptyOrdersState()
                }
            }

            else -> {
                items(
                    items = state.orders,
                    key = { order -> order.id }
                ) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun OrdersHeader(
    orderCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = FruitLogixTheme.spacing.sm,
                vertical = FruitLogixTheme.spacing.sm
            )
    ) {
        Text(
            text = stringResource(R.string.orders_title),
            style = FruitLogixTheme.typography.displayLarge,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.textOnLight
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.orders_subtitle),
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.orders_active_count,
                orderCount
            ),
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderCard(
    order: OrderSummary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = FruitLogixTheme.spacing.sm,
                vertical = 6.dp
            ),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FruitLogixTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id}",
                    style = FruitLogixTheme.typography.titleMedium,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnDark
                )

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_warehouse,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = order.clientName,
                    style = FruitLogixTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = FruitLogixTheme.colors.textOnDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = FruitLogixTheme.colors.textMuted.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = order.productName,
                style = FruitLogixTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = FruitLogixTheme.colors.textOnDark
            )

            Text(
                text = order.quantityLabel,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_schedule,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = stringResource(
                        R.string.order_delivery_date,
                        order.deliveryDateLabel
                    ),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textOnDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(
    status: OrderStatus
) {
    val text = when (status) {
        OrderStatus.PENDING ->
            stringResource(R.string.order_status_pending)

        OrderStatus.IN_PREPARATION ->
            stringResource(R.string.order_status_in_preparation)

        OrderStatus.ON_ROUTE ->
            stringResource(R.string.order_status_on_route)
    }

    val badgeType = when (status) {
        OrderStatus.PENDING ->
            StatusBadgeType.NEUTRAL

        OrderStatus.IN_PREPARATION ->
            StatusBadgeType.WARNING

        OrderStatus.ON_ROUTE ->
            StatusBadgeType.SUCCESS
    }

    StatusBadge(
        text = text,
        type = badgeType
    )
}

@Composable
private fun OrdersLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = FruitLogixTheme.colors.primary
        )
    }
}

@Composable
private fun EmptyOrdersState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FruitLogixTheme.spacing.sm),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(
                id = R.drawable.ic_orders,
                contentDescription = null,
                tint = FruitLogixTheme.colors.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.orders_empty_title),
                style = FruitLogixTheme.typography.titleLarge,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.orders_empty_message),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersScreenPreview() {
    FruitLogixTheme {
        OrdersScreenContent(
            state = OrdersUiState(
                isLoading = false,
                orders = listOf(
                    OrderSummary(
                        id = "FX-1042",
                        clientName = "Supermercados Lima Norte",
                        productName = "Palta Hass",
                        quantityLabel = "18 pallets • 12.0 t",
                        deliveryDateLabel = "Hoy, 14:30",
                        status = OrderStatus.ON_ROUTE
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyOrdersScreenPreview() {
    FruitLogixTheme {
        OrdersScreenContent(
            state = OrdersUiState(
                isLoading = false,
                orders = emptyList()
            )
        )
    }
}