package com.rebootech.fruitlogix.distributor.paymentManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderCatalog
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.CreditReason
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.CreateInvoiceRequestDto
import com.rebootech.fruitlogix.shared.core.network.InvoiceResponseDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

/**
 * Sample invoices with the same clients and producers as Orders and Fleet. When the Render
 * backend answers (/api/v1/invoices), its invoices are merged in, and new invoices are sent
 * there (CreateInvoiceResource). Without connection the sample data keeps the screen usable.
 */
class FakeBillingRepository private constructor() : BillingRepository {

    private var nextNumber = 1050

    private val _invoices = MutableStateFlow(initialInvoices())
    override val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _creditNotes = MutableStateFlow(initialCreditNotes())
    override val creditNotes: StateFlow<List<ProposedCreditNote>> = _creditNotes.asStateFlow()

    override fun getInvoiceById(invoiceId: String): Invoice? =
        _invoices.value.firstOrNull { it.id.equals(invoiceId, ignoreCase = true) }

    override suspend fun addInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        // The server needs the client id: only clients of the catalog can be sent (no invented ids)
        val resolvedClientId = OrderCatalog.clientByName(invoice.counterpartyName)?.id
        if (resolvedClientId == null) {
            Log.i("FruitLogix-API", "Invoice for a client outside the catalog: kept on this phone")
            _invoices.update { listOf(invoice) + it.filterNot { existing -> existing.id == invoice.id } }
            return@withContext
        }
        // orderId is optional on the server: send null instead of linking the invoice to an unrelated order
        val resolvedOrderId = invoice.orderId?.let { FakeOrderRepository.instance.backendIdFor(it) }

        val requestDto = CreateInvoiceRequestDto(
            clientId = resolvedClientId,
            orderId = resolvedOrderId,
            amount = invoice.amount,
            currency = "PEN",
            dueDate = invoice.dueOn.toString(),
            invoiceType = invoice.type.name
        )

        val result = runCatching { RetrofitClient.api.createInvoice(requestDto) }
        var finalInvoice = invoice

        result.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val body = response.body()
                val serverCode = body?.invoiceNumber?.takeIf { it.isNotBlank() }
                    ?: body?.id?.let { "INV-$it" }
                if (serverCode != null) {
                    finalInvoice = invoice.copy(id = serverCode)
                }
            } else {
                Log.w("FruitLogix-API", "POST /invoices rechazado con HTTP ${response.code()}")
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, err.message)
        }

        _invoices.update { listOf(finalInvoice) + it.filterNot { existing -> existing.id == finalInvoice.id } }
    }

    override suspend fun approveCreditNote(noteId: String, amount: Double) {
        _creditNotes.update { list -> list.map { if (it.id == noteId) it.copy(approvedAmount = amount) else it } }
    }

    override fun priceClients(): List<String> = PRICE_HISTORY.keys.map { it.second }.distinct()
    override fun priceProducts(): List<String> = PRICE_HISTORY.keys.map { it.first }.distinct()

    override fun priceHistory(productName: String, clientName: String): List<PricePoint> {
        val prices = PRICE_HISTORY[productName to clientName] ?: return emptyList()
        val last = YearMonth.now()
        return prices.mapIndexed { i, price -> PricePoint(last.minusMonths((prices.size - 1 - i).toLong()), price) }
    }

    override fun purchaseClients(): List<String> = PURCHASE_CLIENTS

    override fun purchaseSummary(clientName: String, period: SummaryPeriod): PurchaseSummary? =
        PURCHASES.firstOrNull { it.clientName == clientName && it.period == period }

    override fun nextInvoiceId(): String = "INV-${nextNumber++}"

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getInvoices() } }
        remote.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val dtos = response.body().orEmpty()
                val mapped = dtos.mapIndexedNotNull { index, dto -> dto.toInvoice(index) }
                if (mapped.isNotEmpty()) {
                    _invoices.update { (mapped + it).distinctBy { invoice -> invoice.id } }
                }
            } else {
                RetrofitClient.updateSyncStatus(
                    BackendSyncStatus.OFFLINE_FALLBACK,
                    "GET /invoices devolvió HTTP ${response.code()}"
                )
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, err.message)
        }
    }

    private fun InvoiceResponseDto.toInvoice(index: Int): Invoice? {
        val resolvedAmount = totalAmount ?: amount ?: return null
        val due = dueDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val issued = issuedAt?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: due ?: return null
        val dueOn = due ?: issued

        // Same rule as orders: a client outside the catalog shows its server id
        val resolvedCounterparty = clientName?.takeIf { it.isNotBlank() }
            ?: OrderCatalog.client(clientId)?.name
            ?: clientId?.let { "#$it" }
            ?: return null

        return Invoice(
            id = invoiceNumber?.takeIf { it.isNotBlank() } ?: "INV-${id ?: (1100 + index)}",
            counterpartyName = resolvedCounterparty,
            amount = resolvedAmount,
            issuedOn = issued,
            dueOn = dueOn,
            type = if (invoiceType.equals("PAYABLE", ignoreCase = true)) InvoiceType.PAYABLE else InvoiceType.RECEIVABLE,
            status = when (status?.uppercase()) {
                "PAID" -> InvoiceStatus.PAID
                "OVERDUE" -> InvoiceStatus.OVERDUE
                else -> InvoiceStatus.PENDING
            },
            orderId = orderId?.let { "FX-$it" }
        )
    }

    companion object {
        val instance: FakeBillingRepository by lazy { FakeBillingRepository() }

        private val PRICE_HISTORY: Map<Pair<String, String>, List<Double>> = mapOf(
            ("Palta Hass" to "Supermercados Lima Norte") to listOf(8.2, 8.5, 9.0, 8.8, 9.4),
            ("Arándano" to "Supermercados Lima Norte") to listOf(16.5, 17.2, 18.0, 17.8, 18.5),
            ("Mango Kent" to "Mercados Lima Sur") to listOf(6.0, 6.4, 6.8, 7.1, 7.5),
            ("Uva Red Globe" to "Supermercados Lima Centro") to listOf(7.9, 8.1, 8.0, 8.4, 8.6)
        )

        private val PURCHASE_CLIENTS = listOf(
            "Supermercados Lima Norte", "Mercados Lima Sur", "Supermercados Lima Centro", "Plaza Vea Callao"
        )

        private val PURCHASES = listOf(
            PurchaseSummary("Supermercados Lima Norte", SummaryPeriod.WEEK, 1850, 17390.0, 3),
            PurchaseSummary("Supermercados Lima Norte", SummaryPeriod.MONTH, 7420, 69748.0, 12),
            PurchaseSummary("Mercados Lima Sur", SummaryPeriod.WEEK, 920, 6900.0, 2),
            PurchaseSummary("Mercados Lima Sur", SummaryPeriod.MONTH, 3800, 28500.0, 8),
            PurchaseSummary("Supermercados Lima Centro", SummaryPeriod.MONTH, 1240, 10664.0, 2)
        )

        private fun initialInvoices(): List<Invoice> {
            val today = LocalDate.now()
            fun invoice(id: String, who: String, amount: Double, issuedDaysAgo: Long, type: InvoiceType, status: InvoiceStatus, order: String? = null) =
                Invoice(id, who, amount, today.minusDays(issuedDaysAgo), today.minusDays(issuedDaysAgo).plusDays(30), type, status, order)
            return listOf(
                invoice("INV-1035", "Supermercados Lima Centro", 9880.0, 3, InvoiceType.RECEIVABLE, InvoiceStatus.PENDING, "FX-1035"),
                invoice("INV-1029", "Supermercados Lima Norte", 8450.0, 12, InvoiceType.RECEIVABLE, InvoiceStatus.PENDING),
                invoice("INV-1032", "Distribuidora Pacífico", 6200.0, 6, InvoiceType.RECEIVABLE, InvoiceStatus.PAID, "FX-1032"),
                invoice("INV-1026", "Mercados Lima Sur", 3850.0, 40, InvoiceType.RECEIVABLE, InvoiceStatus.PENDING),
                invoice("INV-2031", "Fundo Santa Rosa", 5200.0, 4, InvoiceType.PAYABLE, InvoiceStatus.PENDING),
                invoice("INV-2028", "Agro Valle Verde", 3200.0, 8, InvoiceType.PAYABLE, InvoiceStatus.PAID),
                invoice("INV-2024", "Campos del Norte", 4100.0, 35, InvoiceType.PAYABLE, InvoiceStatus.PENDING)
            )
        }

        private fun initialCreditNotes() = listOf(
            ProposedCreditNote(
                id = "NC-2026-01", invoiceId = "INV-1029", clientName = "Supermercados Lima Norte",
                reason = CreditReason.THERMAL_EXCURSION, productName = "Arándano", affectedKg = 45,
                peakCelsius = 9.4, excursionMinutes = 25, suggestedAmount = 405.0
            ),
            ProposedCreditNote(
                id = "NC-2026-02", invoiceId = "INV-1026", clientName = "Mercados Lima Sur",
                reason = CreditReason.DAMAGED_IN_RECEPTION, productName = "Palta Hass", affectedKg = 62,
                suggestedAmount = 558.0
            )
        )
    }
}