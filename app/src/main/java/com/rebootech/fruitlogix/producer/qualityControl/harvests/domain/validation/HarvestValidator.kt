package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation

/** Raw form input for a new harvest. Numbers are parsed here, accepting "." or "," as decimal separator. */
data class HarvestInput(
    val productName: String,
    val lotCode: String,
    val weightKg: String,
    val brix: String,
    val sector: String
)

enum class HarvestField { PRODUCT, LOT_CODE, WEIGHT, BRIX, SECTOR }

enum class HarvestError { REQUIRED, LOT_TOO_SHORT, WEIGHT_OUT_OF_RANGE, BRIX_OUT_OF_RANGE }

object HarvestValidator {
    const val MIN_LOT_LENGTH = 3
    const val MAX_WEIGHT_KG = 50_000.0
    const val MAX_BRIX = 35.0

    fun parseDecimal(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()

    fun validate(input: HarvestInput): Map<HarvestField, HarvestError> {
        val errors = mutableMapOf<HarvestField, HarvestError>()
        if (input.productName.isBlank()) errors[HarvestField.PRODUCT] = HarvestError.REQUIRED
        when {
            input.lotCode.isBlank() -> errors[HarvestField.LOT_CODE] = HarvestError.REQUIRED
            input.lotCode.trim().length < MIN_LOT_LENGTH -> errors[HarvestField.LOT_CODE] = HarvestError.LOT_TOO_SHORT
        }
        val weight = parseDecimal(input.weightKg)
        when {
            input.weightKg.isBlank() -> errors[HarvestField.WEIGHT] = HarvestError.REQUIRED
            weight == null || weight <= 0.0 || weight > MAX_WEIGHT_KG -> errors[HarvestField.WEIGHT] = HarvestError.WEIGHT_OUT_OF_RANGE
        }
        if (input.brix.isNotBlank()) {
            val brix = parseDecimal(input.brix)
            if (brix == null || brix < 0.0 || brix > MAX_BRIX) errors[HarvestField.BRIX] = HarvestError.BRIX_OUT_OF_RANGE
        }
        if (input.sector.isBlank()) errors[HarvestField.SECTOR] = HarvestError.REQUIRED
        return errors
    }
}
