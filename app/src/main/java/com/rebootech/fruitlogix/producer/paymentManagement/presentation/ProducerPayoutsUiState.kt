package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.Payout

enum class PayoutFilter { ALL, PENDING, PAID }

/** Payouts state: data only, formatted by the screen in the selected language. */
data class ProducerPayoutsUiState(
    val isLoading: Boolean = true,
    val totalPendingAmount: Double = 0.0,
    val totalPaidThisMonth: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val selectedFilter: PayoutFilter = PayoutFilter.ALL,
    val allPayouts: List<Payout> = emptyList()
) {
    val payouts: List<Payout>
        get() = allPayouts.filter { matches(it, selectedFilter) }

    fun count(filter: PayoutFilter): Int = allPayouts.count { matches(it, filter) }

    private fun matches(payout: Payout, filter: PayoutFilter): Boolean = when (filter) {
        PayoutFilter.ALL -> true
        // Pending includes overdue: both are still to be collected
        PayoutFilter.PENDING -> payout.status != com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutStatus.PAID
        PayoutFilter.PAID -> payout.status == com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutStatus.PAID
    }
}
