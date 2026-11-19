package com.rebootech.fruitlogix.distributor.orderManagement.domain.validation

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderCatalog
import java.time.LocalDate

/** Business rules for the order form (create and edit). Quantities are in kg, like the server. */
object OrderValidator {
    /** 100 t, a full convoy; anything above is a typo. */
    const val MAX_KG = 100_000.0

    /** Fresh fruit per pallet, used to estimate pallets. */
    const val KG_PER_PALLET = 650.0

    fun isValidClient(clientId: Int?): Boolean = OrderCatalog.client(clientId) != null

    fun isValidProduct(name: String): Boolean = OrderCatalog.product(name) != null

    /** Accepts "11200", "11 200" and decimals with dot or comma ("850,5"). */
    fun parseKg(text: String): Double? =
        text.trim().replace(" ", "").replace(' '.toString(), "").replace(',', '.').toDoubleOrNull()

    fun isValidQuantity(kg: Double?): Boolean = kg != null && kg > 0.0 && kg <= MAX_KG

    /** Delivery cannot be in the past. */
    fun isValidDeliveryDate(date: LocalDate?, today: LocalDate = LocalDate.now()): Boolean =
        date != null && !date.isBefore(today)

    fun estimatePallets(kg: Double): Int = kotlin.math.max(1, kotlin.math.ceil(kg / KG_PER_PALLET).toInt())

    /** Total sent to the server: kg × price per kg of the catalog product. */
    fun totalAmount(productName: String, kg: Double): Double? =
        OrderCatalog.product(productName)?.let { kotlin.math.round(it.pricePerKg * kg * 100) / 100 }
}
