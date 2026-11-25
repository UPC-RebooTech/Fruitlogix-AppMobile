package com.rebootech.fruitlogix.distributor.orderManagement.domain.validation

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CatalogProduct
import java.time.LocalDate

/** Business rules for the order form (create and edit). Quantities are in kg, like the server. */
object OrderValidator {
    /** 100 t, a full convoy; anything above is a typo. */
    const val MAX_KG = 100_000.0

    /** Fresh fruit per pallet, used to estimate pallets. */
    const val KG_PER_PALLET = 650.0

    enum class QuantityProblem { INVALID, BELOW_MINIMUM, ABOVE_STOCK }

    /** Accepts "11200", "11 200" and decimals with dot or comma ("850,5"). */
    fun parseKg(text: String): Double? =
        text.trim().replace(" ", "").replace(' '.toString(), "").replace(',', '.').toDoubleOrNull()

    fun isValidQuantity(kg: Double?): Boolean = kg != null && kg > 0.0 && kg <= MAX_KG

    /**
     * Same checks the server makes for catalog products: at least the producer's minimum order (400)
     * and no more than the available stock (409).
     */
    fun quantityProblem(kg: Double?, product: CatalogProduct?): QuantityProblem? = when {
        !isValidQuantity(kg) -> QuantityProblem.INVALID
        product == null -> null
        kg!! < product.minOrderKg -> QuantityProblem.BELOW_MINIMUM
        product.availableKg != null && kg > product.availableKg -> QuantityProblem.ABOVE_STOCK
        else -> null
    }

    fun isValidAddress(address: String): Boolean = address.trim().length in 5..200

    /** Delivery cannot be in the past. */
    fun isValidDeliveryDate(date: LocalDate?, today: LocalDate = LocalDate.now()): Boolean =
        date != null && !date.isBefore(today)

    fun estimatePallets(kg: Double): Int = kotlin.math.max(1, kotlin.math.ceil(kg / KG_PER_PALLET).toInt())

    /** kg × price per kg, rounded to cents (the server recalculates it anyway). */
    fun totalAmount(product: CatalogProduct, kg: Double): Double = kotlin.math.round(product.pricePerKg * kg * 100) / 100
}
