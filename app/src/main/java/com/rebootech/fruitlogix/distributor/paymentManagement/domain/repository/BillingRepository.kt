package com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice

interface BillingRepository {

    fun getInvoices(): List<Invoice>

    fun getInvoiceById(invoiceId: String): Invoice?
}