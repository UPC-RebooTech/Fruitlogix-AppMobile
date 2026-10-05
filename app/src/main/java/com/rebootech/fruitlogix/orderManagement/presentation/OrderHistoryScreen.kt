package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderHistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg),
        contentPadding = PaddingValues(
            horizontal = FruitLogixTheme.spacing.sm,
            vertical = FruitLogixTheme.spacing.sm
        )
    ) {
        item {
            TextButton(
                onClick = onBackClick
            ) {
                Text(
                    text = stringResource(R.string.order_history_back),
                    color = FruitLogixTheme.colors.textOnLight
                )
            }

            Text(
                text = stringResource(R.string.order_history_title),
                style = FruitLogixTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnLight
            )

            Text(
                text = stringResource(R.string.order_history_subtitle),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textOnLight.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isEmpty) {
            item {
                EmptyOrderHistory()
            }
        } else {
            items(
                items = state.orders,
                key = { order -> order.id }
            ) { order ->
                OrderHistoryCard(order = order)
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: OrderHistoryItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnDark
                )

                StatusBadge(
                    text = when (order.status) {
                        OrderHistoryStatus.DELIVERED ->
                            stringResource(R.string.order_history_delivered)

                        OrderHistoryStatus.CANCELLED ->
                            stringResource(R.string.order_history_cancelled)
                    },
                    type = when (order.status) {
                        OrderHistoryStatus.DELIVERED ->
                            StatusBadgeType.SUCCESS

                        OrderHistoryStatus.CANCELLED ->
                            StatusBadgeType.NEUTRAL
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = order.clientName,
                style = FruitLogixTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_schedule,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary
                )

                Text(
                    text = stringResource(
                        R.string.order_history_date,
                        order.completedDateLabel
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyOrderHistory() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                tint = FruitLogixTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.order_history_empty_title),
                style = FruitLogixTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.order_history_empty_message),
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}