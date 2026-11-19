package com.rebootech.fruitlogix.paymentManagement.presentation

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.InvoiceDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InvoiceDetailViewModelTest {

    private val existingInvoice = Invoice(
        id = "INV-TEST-001",
        counterpartyName = "Test Distributor",
        amount = 1500.00,
        issuedOn = LocalDate.of(2026, 10, 5),
        dueOn = LocalDate.of(2026, 11, 4),
        type = InvoiceType.RECEIVABLE,
        status = InvoiceStatus.PENDING
    )

    private val repository = object : BillingRepository {
        override val invoices: StateFlow<List<Invoice>> = MutableStateFlow(listOf(existingInvoice))
        override val creditNotes: StateFlow<List<ProposedCreditNote>> = MutableStateFlow(emptyList())
        override fun getInvoiceById(invoiceId: String): Invoice? =
            if (invoiceId == existingInvoice.id) existingInvoice else null
        override suspend fun addInvoice(invoice: Invoice) = Unit
        override suspend fun approveCreditNote(noteId: String, amount: Double) = Unit
        override fun priceClients(): List<String> = emptyList()
        override fun priceProducts(): List<String> = emptyList()
        override fun priceHistory(productName: String, clientName: String): List<PricePoint> = emptyList()
        override fun purchaseClients(): List<String> = emptyList()
        override fun purchaseSummary(clientName: String, period: SummaryPeriod): PurchaseSummary? = null
        override fun nextInvoiceId(): String = "INV-TEST-002"
    }

    @Test
    fun loadInvoice_existingInvoice_updatesStateWithInvoice() {
        val viewModel = InvoiceDetailViewModel(repository)

        viewModel.loadInvoice("INV-TEST-001")

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isNotFound)
        assertNotNull(state.invoice)
        assertEquals("INV-TEST-001", state.invoice?.id)
        assertEquals("Test Distributor", state.invoice?.counterpartyName)
        assertEquals(1500.00, state.invoice?.amount ?: 0.0, 0.0)
    }

    @Test
    fun loadInvoice_unknownInvoice_updatesStateAsNotFound() {
        val viewModel = InvoiceDetailViewModel(repository)

        viewModel.loadInvoice("INV-NOT-FOUND")

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isNotFound)
        assertNull(state.invoice)
    }
}
