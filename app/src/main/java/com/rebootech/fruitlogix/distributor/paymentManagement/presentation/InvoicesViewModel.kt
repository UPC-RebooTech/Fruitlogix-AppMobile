package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.BillingTotals
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.AutoInvoiceRules
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.CreditNoteRules
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.InvoiceRules
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate

enum class InvoiceTab { COLLECT, PAY }

/** Billing tools, each opened in its own sheet. */
enum class BillingTool { AUTO_INVOICE, CREDIT_NOTES, PRICES, PURCHASES }

data class InvoicesUiState(
    val all: List<Invoice> = emptyList(),
    val creditNotes: List<ProposedCreditNote> = emptyList(),
    val tab: InvoiceTab = InvoiceTab.COLLECT,
    /** null = all statuses. */
    val status: InvoiceStatus? = null,
    val tool: BillingTool? = null,
    @field:StringRes val toolError: Int? = null,
    val toolErrorArg: String? = null,
    /** Note being adjusted inside the credit notes sheet. */
    val adjustingNoteId: String? = null,
    val message: UiText? = null
) {
    val totals: BillingTotals get() = InvoiceRules.totals(all)

    private val tabInvoices: List<Invoice>
        get() = all.filter { it.type == if (tab == InvoiceTab.COLLECT) InvoiceType.RECEIVABLE else InvoiceType.PAYABLE }

    /** Overdue first, then pending, then paid; newest first inside each group. */
    val visible: List<Invoice>
        get() = tabInvoices
            .filter { status == null || InvoiceRules.effectiveStatus(it) == status }
            .sortedWith(compareBy<Invoice> { InvoiceRules.effectiveStatus(it).sortOrder() }.thenByDescending { it.issuedOn })

    fun count(type: InvoiceType): Int = all.count { it.type == type }
    fun count(status: InvoiceStatus): Int = tabInvoices.count { InvoiceRules.effectiveStatus(it) == status }
    val tabCount: Int get() = tabInvoices.size
    val notesToReview: Int get() = creditNotes.count { !it.isApproved }
}

private fun InvoiceStatus.sortOrder() = when (this) {
    InvoiceStatus.OVERDUE -> 0
    InvoiceStatus.PENDING -> 1
    InvoiceStatus.PAID -> 2
}

/** Invoices tab. Rules (totals, overdue, auto invoice, credit notes) are in domain. */
class InvoicesViewModel(private val repository: BillingRepository) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.billingRepository)

    private val local = MutableStateFlow(InvoicesUiState())

    val uiState: StateFlow<InvoicesUiState> =
        combine(repository.invoices, repository.creditNotes, local) { invoices, notes, local ->
            local.copy(all = invoices, creditNotes = notes)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoicesUiState())

    init {
        // Merge invoices from the Render backend when it answers
        viewModelScope.launch { repository.refresh() }
    }

    fun onTabChange(value: InvoiceTab) = local.update { it.copy(tab = value, status = null) }
    fun onStatusChange(value: InvoiceStatus?) = local.update { it.copy(status = value) }

    fun openTool(tool: BillingTool) = local.update { it.copy(tool = tool, toolError = null, toolErrorArg = null, adjustingNoteId = null) }
    fun closeTool() = local.update { it.copy(tool = null, toolError = null, toolErrorArg = null, adjustingNoteId = null) }
    fun onMessageShown() = local.update { it.copy(message = null) }

    private fun toolError(@StringRes res: Int, arg: String? = null) = local.update { it.copy(toolError = res, toolErrorArg = arg) }

    private fun pen(amount: Double): UiText {
        val format = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return UiText.StringResource(R.string.common_amount_pen, arrayOf(format.format(amount)))
    }

    private fun parse(text: String) = text.trim().replace(',', '.').toDoubleOrNull()

    // US66 -------------------------------------------------------------------

    fun issueAutoInvoice(client: String, kilosText: String, priceText: String) {
        when (val result = AutoInvoiceRules.calculate(client, parse(kilosText), parse(priceText))) {
            AutoInvoiceRules.Result.MissingClient -> toolError(R.string.inv_auto_error_client)
            AutoInvoiceRules.Result.InvalidKilos -> toolError(R.string.inv_auto_error_kilos)
            AutoInvoiceRules.Result.PendingPrice -> toolError(R.string.inv_auto_pending, client.trim())
            is AutoInvoiceRules.Result.Issued -> viewModelScope.launch {
                val today = LocalDate.now()
                val invoice = Invoice(
                    id = repository.nextInvoiceId(),
                    counterpartyName = client.trim(),
                    amount = result.amount,
                    issuedOn = today,
                    dueOn = today.plusDays(AutoInvoiceRules.PAYMENT_TERM_DAYS),
                    type = InvoiceType.RECEIVABLE,
                    status = InvoiceStatus.PENDING
                )
                repository.addInvoice(invoice)
                local.update {
                    it.copy(
                        tool = null, toolError = null, tab = InvoiceTab.COLLECT, status = null,
                        message = UiText.StringResource(R.string.inv_auto_issued, arrayOf(invoice.id, pen(invoice.amount)))
                    )
                }
            }
        }
    }

    // US67 -------------------------------------------------------------------

    fun startAdjusting(noteId: String) = local.update { it.copy(adjustingNoteId = noteId, toolError = null) }

    /** @param amountText null approves the suggested amount. */
    fun approveCreditNote(noteId: String, amountText: String? = null) {
        val note = uiState.value.creditNotes.firstOrNull { it.id == noteId } ?: return
        val amount = if (amountText == null) note.suggestedAmount else parse(amountText)
        val invoiceAmount = repository.getInvoiceById(note.invoiceId)?.amount
        when (CreditNoteRules.validate(amount, invoiceAmount)) {
            CreditNoteRules.Error.NOT_A_NUMBER -> toolError(R.string.inv_credit_error_number)
            CreditNoteRules.Error.NOT_POSITIVE -> toolError(R.string.inv_credit_error_positive)
            CreditNoteRules.Error.ABOVE_INVOICE -> {
                val format = NumberFormat.getNumberInstance().apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
                toolError(R.string.inv_credit_error_above, "S/ ${format.format(invoiceAmount)}")
            }
            null -> viewModelScope.launch {
                repository.approveCreditNote(noteId, amount!!)
                local.update {
                    it.copy(
                        adjustingNoteId = null, toolError = null,
                        message = UiText.StringResource(R.string.inv_credit_done, arrayOf(noteId, pen(amount)))
                    )
                }
            }
        }
    }

    // US68 / US69: read-only lookups ------------------------------------------

    val priceProducts: List<String> get() = repository.priceProducts()
    val priceClients: List<String> get() = repository.priceClients()
    fun priceHistory(product: String, client: String) = repository.priceHistory(product, client)

    val purchaseClients: List<String> get() = repository.purchaseClients()
    fun purchaseSummary(client: String, period: com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod) =
        repository.purchaseSummary(client, period)
}
