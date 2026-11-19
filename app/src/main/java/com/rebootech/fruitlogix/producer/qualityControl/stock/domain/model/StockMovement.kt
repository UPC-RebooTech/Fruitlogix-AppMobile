package com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model

import java.time.LocalDateTime

enum class StockMovementType { HARVEST_INTAKE, DISPATCH, ADJUSTMENT }

/**
 * Change in a lot's stock. Data only; the UI builds the label ("Harvest intake · Crew A").
 * @param reference Crew name or order number, when it applies.
 * @param kg Positive for intake, negative for dispatch.
 */
data class StockMovement(
    val type: StockMovementType,
    val reference: String?,
    val kg: Int,
    val at: LocalDateTime
)
