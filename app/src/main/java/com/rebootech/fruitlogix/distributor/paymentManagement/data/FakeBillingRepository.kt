package com.rebootech.fruitlogix.distributor.paymentManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderCatalogRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceSaveResult
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.PurchaseRules
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.CreateInvoiceRequestDto
import com.rebootech.fruitlogix.shared.core.network.InvoiceResponseDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * The distributor's invoices, only from the server (/invoices): there is no sample data.
 * Price history (US68) and purchases by client (US69) are calculated from the server orders
 * (GET /orders: product, client, kg and price per kg of each item).
 * Credit notes (US67) have no endpoint yet, so the list starts empty.
 */
class FakeBillingRepository(
    private val catalog: OrderCatalogRepository,
    private val orders: OrderRepository
) : BillingRepository {

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    override val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _creditNotes = MutableStateFlow<List<ProposedCreditNote>>(emptyList())
    override val creditNotes: StateFlow<List<ProposedCreditNote>> = _creditNotes.asStateFlow()

    /** Order lines of the server, used for prices and purchases. */
    @Volatile private var lines: List<PurchaseRules.Line> = emptyList()

    override fun getInvoiceById(invoiceId: String): Invoice? =
        _invoices.value.firstOrNull { it.id.equals(invoiceId, ignoreCase = true) }

    override suspend fun addInvoice(invoice: Invoice): InvoiceSaveResult = withContext(Dispatchers.IO) {
        // The server needs the client's account id: only clients from /users can be invoiced
        val client = catalog.clients.value.firstOrNull { it.name.equals(invoice.counterpartyName.trim(), ignoreCase = true) }
            ?: return@withContext InvoiceSaveResult(ok = false, unknownClient = true)
        val request = CreateInvoiceRequestDto(
            clientId = client.id,
            // orderId is optional: null instead of linking the invoice to an unrelated order
            orderId = invoice.orderId?.let { orders.backendIdFor(it) },
            amount = invoice.amount,
            currency = "PEN",
            dueDate = invoice.dueOn.toString(),
            invoiceType = invoice.type.name
        )
        when (val result = apiCall { RetrofitClient.api.createInvoice(request) }) {
            is ApiResult.Ok -> {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val saved = result.value.toInvoice() ?: invoice
                _invoices.update { listOf(saved) + it.filterNot { existing -> existing.id == saved.id } }
                InvoiceSaveResult(ok = true, invoice = saved)
            }
            is ApiResult.Failed -> {
                Log.w(TAG, "POST /invoices failed: ${result.failure}")
                InvoiceSaveResult(ok = false, serverMessage = result.failure.serverMessage, offline = result.failure.isNetwork)
            }
        }
    }

    /** Approved on this phone only: the server has no credit notes yet. */
    override suspend fun approveCreditNote(noteId: String, amount: Double) {
        _creditNotes.update { list -> list.map { if (it.id == noteId) it.copy(approvedAmount = amount) else it } }
    }

    override fun priceClients(): List<String> = lines.map { it.clientName }.distinct().sorted()
    override fun priceProducts(): List<String> = lines.map { it.productName }.distinct().sorted()

    override fun priceHistory(productName: String, clientName: String): List<PricePoint> =
        PurchaseRules.priceHistory(lines, productName, clientName)

    override fun purchaseClients(): List<String> = lines.map { it.clientName }.distinct().sorted()

    override fun purchaseSummary(clientName: String, period: SummaryPeriod): PurchaseSummary? =
        PurchaseRules.summary(lines, clientName, period, LocalDate.now())

    override fun clientNames(): List<String> = catalog.clients.value.map { it.name }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            catalog.refresh()
            when (val result = apiCall { RetrofitClient.api.getInvoices() }) {
                is ApiResult.Ok -> {
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                    _invoices.value = result.value.mapNotNull { it.toInvoice() }
                }
                is ApiResult.Failed -> RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /invoices")
            }
            (apiCall { RetrofitClient.api.getOrders() } as? ApiResult.Ok)?.value?.let { list ->
                lines = list.flatMap { order ->
                    val client = order.commercialClientName?.takeIf { it.isNotBlank() }
                        ?: catalog.clientName(order.commercialClientId)
                        ?: order.commercialClientId?.let { "#$it" }
                        ?: return@flatMap emptyList()
                    val date = order.createdAt?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        ?: return@flatMap emptyList()
                    if (order.status.equals("Cancelled", true) || order.status.equals("Rejected", true)) return@flatMap emptyList()
                    order.items.orEmpty().mapNotNull { item ->
                        val kg = item.quantityKg ?: return@mapNotNull null
                        val price = item.unitPrice ?: return@mapNotNull null
                        PurchaseRules.Line(
                            orderId = order.id ?: 0,
                            clientName = client,
                            productName = item.productName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                            kg = kg,
                            pricePerKg = price,
                            amount = item.subtotal ?: (kg * price),
                            date = date
                        )
                    }
                }
            }
        }
    }

    /** InvoiceResource → invoice. The code is "INV-<id>"; the client is shown by name when it is known. */
    private fun InvoiceResponseDto.toInvoice(): Invoice? {
        val serverId = id ?: return null
        val resolvedAmount = totalAmount ?: amount ?: return null
        val due = dueDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val issued = issuedAt?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: due ?: return null
        return Invoice(
            id = invoiceNumber?.takeIf { it.isNotBlank() } ?: "INV-$serverId",
            counterpartyName = clientName?.takeIf { it.isNotBlank() }
                ?: catalog.clientName(clientId)
                ?: clientId?.let { "#$it" }
                ?: return null,
            amount = resolvedAmount,
            issuedOn = issued,
            dueOn = due ?: issued,
            type = if (invoiceType.equals("PAYABLE", ignoreCase = true)) InvoiceType.PAYABLE else InvoiceType.RECEIVABLE,
            status = when (status?.uppercase()) {
                "PAID" -> InvoiceStatus.PAID
                "OVERDUE" -> InvoiceStatus.OVERDUE
                else -> InvoiceStatus.PENDING
            },
            orderId = orderId?.let { orders.orderCodeFor(it) ?: "FX-$it" }
        )
    }

    private companion object {
        const val TAG = "FruitLogix-Billing"
    }
}
