package com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val invoices: StateFlow<List<Invoice>>
    val creditNotes: StateFlow<List<ProposedCreditNote>>

    fun getInvoiceById(invoiceId: String): Invoice?

    /** Adds a new invoice (e.g. the automatic one of US66). */
    suspend fun addInvoice(invoice: Invoice)

    suspend fun approveCreditNote(noteId: String, amount: Double)

    /** Clients and products that have price history (US68). */
    fun priceClients(): List<String>
    fun priceProducts(): List<String>
    fun priceHistory(productName: String, clientName: String): List<PricePoint>

    /** Clients with purchases (US69). */
    fun purchaseClients(): List<String>
    fun purchaseSummary(clientName: String, period: SummaryPeriod): PurchaseSummary?

    /** Next free invoice number. */
    fun nextInvoiceId(): String

    /** Pulls invoices from the server and merges them; does nothing when offline. */
    suspend fun refresh() {}
}
