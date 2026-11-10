package com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model

enum class StockStatus {
    HEALTHY,
    LOW,
    CRITICAL
}

data class StockItem(
    val productId: String,
    val productName: String,
    val availableKg: Int,
    val reservedKg: Int,
    val minThresholdKg: Int
) {
    val freeKg: Int get() = availableKg - reservedKg

    val status: StockStatus get() = when {
        availableKg < minThresholdKg -> StockStatus.CRITICAL
        availableKg < (minThresholdKg * 1.5).toInt() -> StockStatus.LOW
        else -> StockStatus.HEALTHY
    }

    /**
     * Fraction from 0f to 1f normalized up to 4x threshold.
     * Threshold is marked at 0.25 (1/4).
     */
    val fillFraction: Float get() = (availableKg.toFloat() / (minThresholdKg * 4f)).coerceIn(0f, 1f)
}