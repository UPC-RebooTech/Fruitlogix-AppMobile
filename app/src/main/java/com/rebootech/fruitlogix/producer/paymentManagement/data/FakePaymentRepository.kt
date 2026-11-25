package com.rebootech.fruitlogix.producer.paymentManagement.data

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.Payout
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutStatus
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutType
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Payments of the producer. The server does not show invoices or payments to a Producer
 * (GET /invoices returns [] for them, CAMBIOS-API 8.3), so there is nothing to list: no sample data.
 */
class FakePaymentRepository : PaymentRepository {

    private val _summary = MutableStateFlow(
        PaymentSummary(
            pendingAmount = 0.0,
            paidAmount = 0.0,
            overdueAmount = 0.0
        )
    )

    private val _payouts = MutableStateFlow<List<Payout>>(emptyList())

    override fun observePaymentSummary(): Flow<PaymentSummary> = _summary.asStateFlow()

    override suspend fun getPaymentSummary(): PaymentSummary = _summary.value

    override fun observePayouts(): Flow<List<Payout>> = _payouts.asStateFlow()

    companion object {
        val instance: FakePaymentRepository by lazy { FakePaymentRepository() }
    }
}