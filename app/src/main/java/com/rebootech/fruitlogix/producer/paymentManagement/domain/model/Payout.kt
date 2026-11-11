package com.rebootech.fruitlogix.producer.paymentManagement.domain.model

import java.time.LocalDate

enum class PayoutType {
    /** Final payment for a delivered lot. */
    LOT_SETTLEMENT,

    /** Advance paid before or during harvest. */
    HARVEST_ADVANCE
}

enum class PayoutStatus { PENDING, PAID, OVERDUE }

/**
 * Payout from a distributor to the producer. Data only; the UI builds the concept
 * ("Lot settlement · Hass Avocado #44") and dates in the selected language.
 */
data class Payout(
    val id: String,
    val type: PayoutType,
    val productName: String,
    val lotCode: String?,
    val distributorName: String,
    val issuedOn: LocalDate,
    val dueOn: LocalDate?,
    val amount: Double,
    val status: PayoutStatus
)
