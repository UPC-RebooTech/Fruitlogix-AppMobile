package com.rebootech.fruitlogix.distributor.orderManagement.domain.validation

import java.time.LocalDate

/** Business rules for the order form (create and edit). */
object OrderValidator {
    const val MAX_TONS = 100.0

    fun isValidClient(name: String): Boolean = name.trim().length in 2..80

    fun isValidProduct(name: String): Boolean = name.trim().length in 2..60

    /** Accepts "12.5" and "12,5". */
    fun parseTons(text: String): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()

    fun isValidQuantity(tons: Double?): Boolean = tons != null && tons > 0.0 && tons <= MAX_TONS

    /** Delivery cannot be in the past. */
    fun isValidDeliveryDate(date: LocalDate?, today: LocalDate = LocalDate.now()): Boolean =
        date != null && !date.isBefore(today)

    /** About 0.65 t per pallet of fresh fruit: used to estimate pallets for new orders. */
    fun estimatePallets(tons: Double): Int = kotlin.math.max(1, kotlin.math.ceil(tons / 0.65).toInt())
}
