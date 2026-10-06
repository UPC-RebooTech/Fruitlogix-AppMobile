package com.rebootech.fruitlogix.paymentManagement.domain.repository

import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice

interface BillingRepository {

    fun getInvoices(): List<Invoice>

    fun getInvoiceById(invoiceId: String): Invoice?
}