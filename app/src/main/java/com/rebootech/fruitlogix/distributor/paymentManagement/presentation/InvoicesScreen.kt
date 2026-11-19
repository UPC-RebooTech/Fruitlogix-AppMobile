package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.paymentManagement.data.report.FinancialReportExporter
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.InvoiceRules
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.AutoInvoiceSheet
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.CreditNotesSheet
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.PricesSheet
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.PurchasesSheet
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components.overdueText
import com.rebootech.fruitlogix.shared.ui.components.ActionRow
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SectionHeader
import com.rebootech.fruitlogix.shared.ui.components.SegmentedTabs
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.formatPen
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Invoices tab: totals → to collect / to pay → status filter → list → PDF export →
 * billing tools (auto invoice, credit notes, prices, purchases), each in its own sheet.
 */
@Composable
fun InvoicesScreen(
    onInvoiceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoicesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.asString(context))
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            item {
                Column {
                    Text(stringResource(R.string.inv_title), style = FruitLogixTheme.typography.headlineMedium, color = scheme.onBackground)
                    Text(stringResource(R.string.inv_subtitle), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }

            item { Totals(state) }

            item {
                SegmentedTabs(
                    options = listOf(
                        InvoiceTab.COLLECT to stringResource(R.string.inv_tab_collect, state.count(InvoiceType.RECEIVABLE)),
                        InvoiceTab.PAY to stringResource(R.string.inv_tab_pay, state.count(InvoiceType.PAYABLE))
                    ),
                    selected = state.tab,
                    onSelect = viewModel::onTabChange
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    SelectableChip(
                        label = stringResource(R.string.dist_orders_filter_count, stringResource(R.string.inv_filter_all), state.tabCount),
                        selected = state.status == null,
                        onClick = { viewModel.onStatusChange(null) }
                    )
                    listOf(InvoiceStatus.OVERDUE, InvoiceStatus.PENDING, InvoiceStatus.PAID).forEach { status ->
                        SelectableChip(
                            label = stringResource(R.string.dist_orders_filter_count, stringResource(invoiceStatusLabel(status)), state.count(status)),
                            selected = state.status == status,
                            onClick = { viewModel.onStatusChange(status) }
                        )
                    }
                }
            }

            if (state.visible.isEmpty()) {
                item { EmptyState(title = stringResource(R.string.inv_empty), message = "") }
            } else {
                items(state.visible, key = { it.id }) { invoice ->
                    InvoiceCard(invoice = invoice, onClick = { onInvoiceClick(invoice.id) })
                }
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.inv_export),
                    onClick = { FinancialReportExporter.exportAndShare(context, state.visible, state.status) },
                    enabled = state.visible.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { AppIcon(id = R.drawable.ic_download, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    SectionHeader(title = stringResource(R.string.inv_tools_title))
                    ActionRow(R.drawable.ic_receipt, stringResource(R.string.inv_tool_auto), stringResource(R.string.inv_tool_auto_sub), { viewModel.openTool(BillingTool.AUTO_INVOICE) })
                    ActionRow(
                        R.drawable.ic_task_check,
                        stringResource(R.string.inv_tool_credit),
                        if (state.notesToReview > 0) stringResource(R.string.inv_tool_credit_sub, state.notesToReview) else stringResource(R.string.inv_tool_credit_done),
                        { viewModel.openTool(BillingTool.CREDIT_NOTES) }
                    )
                    ActionRow(R.drawable.ic_chart_trend, stringResource(R.string.inv_tool_prices), stringResource(R.string.inv_tool_prices_sub), { viewModel.openTool(BillingTool.PRICES) })
                    ActionRow(R.drawable.ic_groups, stringResource(R.string.inv_tool_purchases), stringResource(R.string.inv_tool_purchases_sub), { viewModel.openTool(BillingTool.PURCHASES) })
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(spacing.sm)
        )
    }

    state.tool?.let { ToolSheet(it, state, viewModel) }
}

@Composable
private fun Totals(state: InvoicesUiState) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val totals = state.totals

    @Composable
    fun footer(overdue: Int): @Composable () -> Unit = {
        Text(
            text = if (overdue > 0) pluralStringResource(R.plurals.inv_overdue_count, overdue, overdue) else stringResource(R.string.inv_up_to_date),
            style = FruitLogixTheme.typography.bodySmall,
            color = if (overdue > 0) colors.attentionContainer else colors.onInverseSurfaceVariant
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
        KpiCard(
            caption = stringResource(R.string.inv_to_collect),
            value = formatPen(totals.toCollect, decimals = 0),
            icon = { AppIcon(id = R.drawable.ic_invoices, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) },
            footer = footer(totals.overdueToCollect),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            caption = stringResource(R.string.inv_to_pay),
            value = formatPen(totals.toPay, decimals = 0),
            icon = { AppIcon(id = R.drawable.ic_receipt, contentDescription = null, tint = scheme.inversePrimary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) },
            footer = footer(totals.overdueToPay),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InvoiceCard(invoice: Invoice, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val status = InvoiceRules.effectiveStatus(invoice)
    val overdue = InvoiceRules.daysOverdue(invoice)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(invoice.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(text = stringResource(invoiceStatusLabel(status)), type = invoiceStatusBadge(status), maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = invoice.counterpartyName,
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(formatPen(invoice.amount), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = when {
                overdue > 0 -> overdueText(overdue)
                status == InvoiceStatus.PAID -> stringResource(R.string.inv_issued, formatInvoiceDate(invoice.issuedOn))
                else -> stringResource(R.string.inv_due, formatInvoiceDate(invoice.dueOn))
            },
            style = FruitLogixTheme.typography.bodyMedium,
            color = if (overdue > 0) scheme.error else scheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolSheet(tool: BillingTool, state: InvoicesUiState, viewModel: InvoicesViewModel) {
    val scheme = MaterialTheme.colorScheme
    val error = state.toolError?.let { res -> state.toolErrorArg?.let { stringResource(res, it) } ?: stringResource(res) }
    val title = stringResource(
        when (tool) {
            BillingTool.AUTO_INVOICE -> R.string.inv_tool_auto
            BillingTool.CREDIT_NOTES -> R.string.inv_tool_credit
            BillingTool.PRICES -> R.string.inv_tool_prices
            BillingTool.PURCHASES -> R.string.inv_tool_purchases
        }
    )
    ModalBottomSheet(
        onDismissRequest = viewModel::closeTool,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FruitLogixTheme.spacing.md)
                .padding(bottom = FruitLogixTheme.spacing.lg)
        ) {
            Text(title, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
            when (tool) {
                BillingTool.AUTO_INVOICE -> AutoInvoiceSheet(
                    clients = viewModel.purchaseClients,
                    error = error,
                    onIssue = viewModel::issueAutoInvoice
                )
                BillingTool.CREDIT_NOTES -> CreditNotesSheet(
                    notes = state.creditNotes,
                    adjustingNoteId = state.adjustingNoteId,
                    error = error,
                    onApprove = { viewModel.approveCreditNote(it) },
                    onStartAdjust = viewModel::startAdjusting,
                    onApproveAmount = { id, amount -> viewModel.approveCreditNote(id, amount) }
                )
                BillingTool.PRICES -> PricesSheet(
                    products = viewModel.priceProducts,
                    clients = viewModel.priceClients,
                    historyFor = viewModel::priceHistory
                )
                BillingTool.PURCHASES -> PurchasesSheet(
                    clients = viewModel.purchaseClients,
                    summaryFor = viewModel::purchaseSummary
                )
            }
        }
    }
}
