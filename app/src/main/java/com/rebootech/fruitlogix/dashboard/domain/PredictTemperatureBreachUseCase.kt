package com.rebootech.fruitlogix.dashboard.domain

import kotlin.math.roundToInt

/**
 * Use case to predict temperature breaches in reefer units.
 * Takes a list of temperature readings and a critical threshold,
 * calculates slope using simple least-squares linear regression,
 * and determines the predictive alert state.
 */
class PredictTemperatureBreachUseCase {

    fun execute(
        readings: List<TemperatureReading>,
        thresholdCelsius: Double,
        unitId: String = "FL-408",
        cargoDescription: String = "Michoacán Hass avocado"
    ): PredictiveAlert? {
        if (readings.isEmpty()) return null

        val currentReading = readings.minByOrNull { it.minutesAgo } ?: return null
        val currentCelsius = currentReading.celsius

        // Check if current temperature already breaches threshold
        if (currentCelsius >= thresholdCelsius) {
            return PredictiveAlert(
                unitId = unitId,
                cargoDescription = cargoDescription,
                minutesToBreach = 0,
                ratePerMinute = 0.0,
                currentCelsius = currentCelsius,
                thresholdCelsius = thresholdCelsius,
                readings = readings,
                status = PredictiveAlertStatus.BREACHED
            )
        }

        // Simple least-squares linear regression
        // x_i = -reading.minutesAgo (time moving forward towards 0)
        // y_i = reading.celsius
        val n = readings.size.toDouble()
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0

        for (reading in readings) {
            val x = -reading.minutesAgo.toDouble()
            val y = reading.celsius
            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
        }

        val denominator = (n * sumX2) - (sumX * sumX)
        val slope = if (denominator != 0.0) {
            ((n * sumXY) - (sumX * sumY)) / denominator
        } else {
            0.0
        }

        val status: PredictiveAlertStatus
        val minutesToBreach: Int

        if (slope <= 0.0) {
            status = PredictiveAlertStatus.NOMINAL
            minutesToBreach = Int.MAX_VALUE
        } else {
            val tempDiff = thresholdCelsius - currentCelsius
            minutesToBreach = (tempDiff / slope).roundToInt()
            status = if (minutesToBreach <= 30) {
                PredictiveAlertStatus.PREDICTIVE
            } else {
                PredictiveAlertStatus.NOMINAL
            }
        }

        return PredictiveAlert(
            unitId = unitId,
            cargoDescription = cargoDescription,
            minutesToBreach = minutesToBreach,
            ratePerMinute = slope,
            currentCelsius = currentCelsius,
            thresholdCelsius = thresholdCelsius,
            readings = readings,
            status = status
        )
    }
}
