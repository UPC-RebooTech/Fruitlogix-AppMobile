package com.rebootech.fruitlogix.producer.paymentManagement.presentation

enum class PayoutFilter { ALL, PENDING, COMPLETED }

data class PayoutItem(
    val id: String,
    val concept: String,
    val date: String,
    val amount: Double,
    val isPending: Boolean
)

data class ProducerPayoutsUiState(
    val isLoading: Boolean = false,
    val totalPendingAmount: Double = 0.0,
    val totalPaidThisMonth: Double = 0.0,
    val selectedFilter: PayoutFilter = PayoutFilter.ALL,
    val payouts: List<PayoutItem> = emptyList()
)