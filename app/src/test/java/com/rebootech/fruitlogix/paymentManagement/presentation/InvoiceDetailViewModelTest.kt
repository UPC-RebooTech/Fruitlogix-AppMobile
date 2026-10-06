package com.rebootech.fruitlogix.paymentManagement.presentation

import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.paymentManagement.domain.repository.BillingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceDetailViewModelTest {

    private val existingInvoice = Invoice(
        id = "INV-TEST-001",
        counterpartyName = "Test Distributor",
        amount = 1500.00,
        dateLabel = "05 Oct 2026",
        type = InvoiceType.RECEIVABLE,
        status = InvoiceStatus.PENDING
    )

    private val repository = object : BillingRepository {

        override fun getInvoices(): List<Invoice> {
            return listOf(existingInvoice)
        }

        override fun getInvoiceById(
            invoiceId: String
        ): Invoice? {
            return if (invoiceId == existingInvoice.id) {
                existingInvoice
            } else {
                null
            }
        }
    }

    @Test
    fun loadInvoice_existingInvoice_updatesStateWithInvoice() {
        val viewModel =
            InvoiceDetailViewModel(repository)

        viewModel.loadInvoice("INV-TEST-001")

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isNotFound)
        assertNotNull(state.invoice)

        assertEquals(
            "INV-TEST-001",
            state.invoice?.id
        )

        assertEquals(
            "Test Distributor",
            state.invoice?.counterpartyName
        )

        assertEquals(
            1500.00,
            state.invoice?.amount ?: 0.0,
            0.0
        )
    }

    @Test
    fun loadInvoice_unknownInvoice_updatesStateAsNotFound() {
        val viewModel =
            InvoiceDetailViewModel(repository)

        viewModel.loadInvoice("INV-NOT-FOUND")

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isNotFound)
        assertNull(state.invoice)
    }
}