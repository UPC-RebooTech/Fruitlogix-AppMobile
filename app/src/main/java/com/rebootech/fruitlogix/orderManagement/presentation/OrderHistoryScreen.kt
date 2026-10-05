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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

            Spacer(modifier = Modifier.height(20.dp))

            HistoryFilters(
                searchQuery = state.searchQuery,
                dateFilter = state.dateFilter,
                producerFilter = state.producerFilter,
                statusFilter = state.statusFilter,
                onSearchChange = viewModel::onSearchQueryChange,
                onDateChange = viewModel::onDateFilterChange,
                onProducerChange = viewModel::onProducerFilterChange,
                onStatusChange = viewModel::onStatusFilterChange,
                onClearFilters = viewModel::clearFilters
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            state.isEmpty -> {
                item {
                    EmptyOrderHistory()
                }
            }

            state.hasNoResults -> {
                item {
                    NoFilteredResults()
                }
            }

            else -> {
                items(
                    items = state.orders,
                    key = { order -> order.id }
                ) { order ->
                    OrderHistoryCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun HistoryFilters(
    searchQuery: String,
    dateFilter: String,
    producerFilter: String,
    statusFilter: OrderHistoryStatus?,
    onSearchChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onProducerChange: (String) -> Unit,
    onStatusChange: (OrderHistoryStatus?) -> Unit,
    onClearFilters: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = FruitLogixTheme.colors.textOnDark,
        unfocusedTextColor = FruitLogixTheme.colors.textOnDark,

        focusedLabelColor = FruitLogixTheme.colors.primary,
        unfocusedLabelColor = FruitLogixTheme.colors.textMuted,

        focusedPlaceholderColor = FruitLogixTheme.colors.textMuted,
        unfocusedPlaceholderColor = FruitLogixTheme.colors.textMuted,

        cursorColor = FruitLogixTheme.colors.primary,

        focusedBorderColor = FruitLogixTheme.colors.primary,
        unfocusedBorderColor = FruitLogixTheme.colors.textMuted.copy(alpha = 0.65f),

        focusedContainerColor = FruitLogixTheme.colors.surfaceDark,
        unfocusedContainerColor = FruitLogixTheme.colors.surfaceDark
    )

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
                .padding(FruitLogixTheme.spacing.sm)
        ) {
            Text(
                text = stringResource(R.string.order_history_filters_title),
                style = FruitLogixTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.order_history_search))
                },
                placeholder = {
                    Text(stringResource(R.string.order_history_search_example))
                },
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dateFilter,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.order_history_filter_date))
                },
                placeholder = {
                    Text(stringResource(R.string.order_history_filter_date_example))
                },
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = producerFilter,
                onValueChange = onProducerChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.order_history_filter_producer))
                },
                placeholder = {
                    Text(stringResource(R.string.order_history_filter_producer_example))
                },
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.order_history_filter_status),
                style = FruitLogixTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = {
                        onStatusChange(null)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.order_history_filter_all),
                        color = if (statusFilter == null) {
                            FruitLogixTheme.colors.primary
                        } else {
                            FruitLogixTheme.colors.textMuted
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = {
                        onStatusChange(OrderHistoryStatus.DELIVERED)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.order_history_delivered),
                        color = if (statusFilter == OrderHistoryStatus.DELIVERED) {
                            FruitLogixTheme.colors.primary
                        } else {
                            FruitLogixTheme.colors.textMuted
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = {
                        onStatusChange(OrderHistoryStatus.CANCELLED)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.order_history_cancelled),
                        color = if (statusFilter == OrderHistoryStatus.CANCELLED) {
                            FruitLogixTheme.colors.primary
                        } else {
                            FruitLogixTheme.colors.textMuted
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.order_history_clear_filters),
                    color = FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    R.string.order_history_producer,
                    order.producerName
                ),
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
    HistoryMessageCard(
        title = stringResource(R.string.order_history_empty_title),
        message = stringResource(R.string.order_history_empty_message)
    )
}

@Composable
private fun NoFilteredResults() {
    HistoryMessageCard(
        title = stringResource(R.string.order_history_no_results_title),
        message = stringResource(R.string.order_history_no_results_message)
    )
}

@Composable
private fun HistoryMessageCard(
    title: String,
    message: String
) {
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
                text = title,
                style = FruitLogixTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}