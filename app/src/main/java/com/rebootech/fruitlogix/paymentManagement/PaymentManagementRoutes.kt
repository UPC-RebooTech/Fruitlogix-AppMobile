package com.rebootech.fruitlogix.paymentManagement

object PaymentManagementRoutes {

    const val InvoiceDetail =
        "payment_management_invoice_detail/{invoiceId}"

    fun invoiceDetail(
        invoiceId: String
    ): String {
        return "payment_management_invoice_detail/$invoiceId"
    }
}