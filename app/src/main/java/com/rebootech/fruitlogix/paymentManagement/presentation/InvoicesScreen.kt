package com.rebootech.fruitlogix.paymentManagement.presentation

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InvoicesScreen(
    modifier: Modifier = Modifier,
    viewModel: InvoicesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
            Column(
                modifier = Modifier.padding(
                    horizontal = FruitLogixTheme.spacing.sm,
                    vertical = FruitLogixTheme.spacing.sm
                )
            ) {
                Text(
                    text = stringResource(R.string.billing_title),
                    style = FruitLogixTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnLight
                )

                Text(
                    text = stringResource(R.string.billing_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textOnLight.copy(
                        alpha = 0.7f
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BillingSummaryCard(
                        title = stringResource(
                            R.string.billing_receivables
                        ),
                        amount = state.outstandingReceivables,
                        modifier = Modifier.weight(1f)
                    )

                    BillingSummaryCard(
                        title = stringResource(
                            R.string.billing_payables
                        ),
                        amount = state.outstandingPayables,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BillingFilters(
                    dateFilter = state.dateFilter,
                    statusFilter = state.statusFilter,
                    onDateChange = viewModel::onDateFilterChange,
                    onStatusChange = viewModel::onStatusFilterChange,
                    onClear = viewModel::clearFilters
                )
            }
        }

        when {
            state.isLoading -> {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(32.dp),
                        color = FruitLogixTheme.colors.primary
                    )
                }
            }

            state.isEmpty -> {
                item {
                    BillingMessageCard(
                        title = stringResource(
                            R.string.billing_empty_title
                        ),
                        message = stringResource(
                            R.string.billing_empty_message
                        )
                    )
                }
            }

            state.hasNoResults -> {
                item {
                    BillingMessageCard(
                        title = stringResource(
                            R.string.billing_no_results_title
                        ),
                        message = stringResource(
                            R.string.billing_no_results_message
                        )
                    )
                }
            }

            else -> {
                if (state.receivables.isNotEmpty()) {
                    item {
                        BillingSectionTitle(
                            text = stringResource(
                                R.string.billing_receivables
                            )
                        )
                    }

                    items(
                        items = state.receivables,
                        key = { it.id }
                    ) { invoice ->
                        InvoiceCard(invoice)
                    }
                }

                if (state.payables.isNotEmpty()) {
                    item {
                        BillingSectionTitle(
                            text = stringResource(
                                R.string.billing_payables
                            )
                        )
                    }

                    items(
                        items = state.payables,
                        key = { it.id }
                    ) { invoice ->
                        InvoiceCard(invoice)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingSummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatAmount(amount),
                style = FruitLogixTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Text(
                text = stringResource(R.string.billing_outstanding),
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.primary
            )
        }
    }
}

@Composable
private fun BillingFilters(
    dateFilter: String,
    statusFilter: InvoiceStatus?,
    onDateChange: (String) -> Unit,
    onStatusChange: (InvoiceStatus?) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.billing_filters),
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dateFilter,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.billing_filter_date))
                },
                placeholder = {
                    Text(
                        stringResource(
                            R.string.billing_filter_date_example
                        )
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor =
                        FruitLogixTheme.colors.textOnDark,
                    unfocusedTextColor =
                        FruitLogixTheme.colors.textOnDark,
                    focusedLabelColor =
                        FruitLogixTheme.colors.primary,
                    unfocusedLabelColor =
                        FruitLogixTheme.colors.textMuted,
                    focusedPlaceholderColor =
                        FruitLogixTheme.colors.textMuted,
                    unfocusedPlaceholderColor =
                        FruitLogixTheme.colors.textMuted,
                    cursorColor =
                        FruitLogixTheme.colors.primary,
                    focusedBorderColor =
                        FruitLogixTheme.colors.primary,
                    unfocusedBorderColor =
                        FruitLogixTheme.colors.textMuted
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.billing_filter_status),
                color = FruitLogixTheme.colors.textOnDark,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusFilterButton(
                    text = stringResource(R.string.billing_status_all),
                    selected = statusFilter == null,
                    onClick = {
                        onStatusChange(null)
                    }
                )

                StatusFilterButton(
                    text = stringResource(R.string.billing_status_pending),
                    selected =
                        statusFilter == InvoiceStatus.PENDING,
                    onClick = {
                        onStatusChange(InvoiceStatus.PENDING)
                    }
                )

                StatusFilterButton(
                    text = stringResource(R.string.billing_status_paid),
                    selected =
                        statusFilter == InvoiceStatus.PAID,
                    onClick = {
                        onStatusChange(InvoiceStatus.PAID)
                    }
                )

                StatusFilterButton(
                    text = stringResource(R.string.billing_status_overdue),
                    selected =
                        statusFilter == InvoiceStatus.OVERDUE,
                    onClick = {
                        onStatusChange(InvoiceStatus.OVERDUE)
                    }
                )
            }

            TextButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(
                        R.string.billing_clear_filters
                    ),
                    color = FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatusFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = if (selected) {
                FruitLogixTheme.colors.primary
            } else {
                FruitLogixTheme.colors.textMuted
            },
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BillingSectionTitle(
    text: String
) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(
            horizontal = FruitLogixTheme.spacing.sm,
            vertical = 12.dp
        ),
        style = FruitLogixTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = FruitLogixTheme.colors.textOnLight
    )
}

@Composable
private fun InvoiceCard(
    invoice: Invoice
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.id,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnDark
                )

                InvoiceStatusBadge(invoice.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = invoice.counterpartyName,
                style = FruitLogixTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = invoice.dateLabel,
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formatAmount(invoice.amount),
                style = FruitLogixTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.primary
            )
        }
    }
}

@Composable
private fun InvoiceStatusBadge(
    status: InvoiceStatus
) {
    val text = when (status) {
        InvoiceStatus.PENDING ->
            stringResource(R.string.billing_status_pending)

        InvoiceStatus.PAID ->
            stringResource(R.string.billing_status_paid)

        InvoiceStatus.OVERDUE ->
            stringResource(R.string.billing_status_overdue)
    }

    val type = when (status) {
        InvoiceStatus.PENDING ->
            StatusBadgeType.WARNING

        InvoiceStatus.PAID ->
            StatusBadgeType.SUCCESS

        InvoiceStatus.OVERDUE ->
            StatusBadgeType.DANGER
    }

    StatusBadge(
        text = text,
        type = type
    )
}

@Composable
private fun BillingMessageCard(
    title: String,
    message: String
) {
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
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}

private fun formatAmount(
    amount: Double
): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2

    return "S/ ${formatter.format(amount)}"
}