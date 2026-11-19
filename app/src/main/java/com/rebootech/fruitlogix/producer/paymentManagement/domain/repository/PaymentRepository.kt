package com.rebootech.fruitlogix.producer.paymentManagement.domain.repository

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.Payout
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePaymentSummary(): Flow<PaymentSummary>
    suspend fun getPaymentSummary(): PaymentSummary
    fun observePayouts(): Flow<List<Payout>>
}
