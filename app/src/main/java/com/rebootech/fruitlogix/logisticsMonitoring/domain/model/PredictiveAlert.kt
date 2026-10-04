package com.rebootech.fruitlogix.logisticsMonitoring.domain.model

import com.rebootech.fruitlogix.infrastructureIot.domain.model.TemperatureReading

enum class PredictiveAlertStatus {
    PREDICTIVE, // Breach predicted within 30 min and positive slope
    NOMINAL,    // Breach > 30 min away or non-positive slope
    BREACHED    // Current temperature already >= threshold
}

data class PredictiveAlert(
    val unitId: String,
    val cargoDescription: String,
    val minutesToBreach: Int,
    val ratePerMinute: Double,
    val currentCelsius: Double,
    val thresholdCelsius: Double,
    val readings: List<TemperatureReading>,
    val status: PredictiveAlertStatus
)
