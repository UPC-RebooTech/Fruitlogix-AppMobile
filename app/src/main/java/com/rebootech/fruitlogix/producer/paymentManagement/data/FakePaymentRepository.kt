package com.rebootech.fruitlogix.producer.paymentManagement.data

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePaymentRepository : PaymentRepository {
    private val _summary = MutableStateFlow(
        PaymentSummary(
            pendingAmount = 18450.0,
            paidAmount = 42300.0,
            overdueAmount = 3200.0
        )
    )

    override fun observePaymentSummary(): Flow<PaymentSummary> = _summary.asStateFlow()

    companion object {
        val instance: FakePaymentRepository by lazy { FakePaymentRepository() }
    }
}

