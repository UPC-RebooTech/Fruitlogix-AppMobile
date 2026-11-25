package com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceSaveResult
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val invoices: StateFlow<List<Invoice>>
    val creditNotes: StateFlow<List<ProposedCreditNote>>

    fun getInvoiceById(invoiceId: String): Invoice?

    /** Sends a new invoice to the server (e.g. the automatic one of US66). Its code comes from the server. */
    suspend fun addInvoice(invoice: Invoice): InvoiceSaveResult

    suspend fun approveCreditNote(noteId: String, amount: Double)

    /** Clients and products that have price history (US68). */
    fun priceClients(): List<String>
    fun priceProducts(): List<String>
    fun priceHistory(productName: String, clientName: String): List<PricePoint>

    /** Clients with purchases (US69). */
    fun purchaseClients(): List<String>
    fun purchaseSummary(clientName: String, period: SummaryPeriod): PurchaseSummary?

    /** Client accounts that can be invoiced (GET /users). */
    fun clientNames(): List<String> = emptyList()

    /** Reads invoices and orders from the server again. */
    suspend fun refresh() {}
}
