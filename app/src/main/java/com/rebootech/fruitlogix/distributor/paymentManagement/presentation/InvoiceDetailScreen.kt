package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.InvoiceRules
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.creditReasonText
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.overdueText
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.DetailRow
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.formatPen
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoiceDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    LaunchedEffect(invoiceId) { viewModel.loadInvoice(invoiceId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        val invoice = state.invoice
        BackHeader(title = invoiceId, subtitle = invoice?.counterpartyName, onBack = onBackClick)
        when {
            state.isLoading -> CircularProgressIndicator(color = scheme.primary, modifier = Modifier.align(Alignment.CenterHorizontally))
            invoice == null -> Text(stringResource(R.string.inv_detail_not_found), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            else -> {
                AmountCard(invoice)
                DetailSection(title = stringResource(if (invoice.type == InvoiceType.RECEIVABLE) R.string.inv_detail_from else R.string.inv_detail_to)) {
                    DetailRow(R.drawable.ic_warehouse, stringResource(if (invoice.type == InvoiceType.RECEIVABLE) R.string.inv_detail_from else R.string.inv_detail_to), invoice.counterpartyName)
                    DetailRow(R.drawable.ic_schedule, stringResource(R.string.inv_detail_issued), formatInvoiceDate(invoice.issuedOn))
                    DetailRow(R.drawable.ic_schedule, stringResource(R.string.inv_detail_due), formatInvoiceDate(invoice.dueOn))
                    invoice.orderId?.let { DetailRow(R.drawable.ic_orders, stringResource(R.string.inv_detail_order), it, valueStyle = FruitLogixTheme.extraTypography.dataMono) }
                }
                if (state.creditNotes.isNotEmpty()) {
                    Text(stringResource(R.string.inv_detail_credit_notes), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
                    state.creditNotes.forEach { note ->
                        Column(modifier = Modifier.fillMaxWidth().whiteCard().padding(spacing.inner)) {
                            Text(note.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
                            Text(creditReasonText(note), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            Text(
                                text = note.approvedAmount?.let { stringResource(R.string.inv_credit_approved, formatPen(it)) }
                                    ?: stringResource(R.string.inv_credit_suggested, formatPen(note.suggestedAmount)),
                                style = FruitLogixTheme.typography.titleSmall,
                                color = if (note.isApproved) FruitLogixTheme.colors.successContent else scheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Highlighted (dark) card: the amount and its status. */
@Composable
private fun AmountCard(invoice: Invoice) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val status = InvoiceRules.effectiveStatus(invoice)
    val overdue = InvoiceRules.daysOverdue(invoice)
    FruitLogixCard(variant = CardVariant.Dark, shape = FruitLogixTheme.shapes.HeroCard, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (invoice.type == InvoiceType.RECEIVABLE) R.string.inv_to_collect else R.string.inv_to_pay).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = colors.onInverseSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(text = stringResource(invoiceStatusLabel(status)), type = invoiceStatusBadge(status), maxLines = 1, softWrap = false)
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(formatPen(invoice.amount), style = FruitLogixTheme.extraTypography.kpiNumber, color = scheme.inverseOnSurface)
            Text(
                text = if (overdue > 0) overdueText(overdue) else stringResource(R.string.inv_due, formatInvoiceDate(invoice.dueOn)),
                style = FruitLogixTheme.typography.bodyMedium,
                color = if (overdue > 0) colors.attentionContainer else colors.onInverseSurfaceVariant
            )
        }
    }
}
