package com.rebootech.fruitlogix.producer.paymentManagement.domain.repository

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePaymentSummary(): Flow<PaymentSummary>
    suspend fun getPaymentSummary(): PaymentSummary
}

