package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary

enum class PayoutFilter { ALL, PENDING, COMPLETED }

data class ProducerPayoutsUiState(
    val isLoading: Boolean = false,
    val totalPendingAmount: Double = 0.0,
    val totalPaidThisMonth: Double = 0.0,
    val selectedFilter: PayoutFilter = PayoutFilter.ALL,
    val payouts: List<PaymentSummary> = emptyList()
)