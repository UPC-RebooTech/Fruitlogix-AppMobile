package com.rebootech.fruitlogix.dashboard.domain

/**
 * Represents a temperature reading at a specific time in the past.
 * @param minutesAgo Minutes elapsed since the reading was taken (0 = current reading).
 * @param celsius Temperature value in degrees Celsius.
 */
data class TemperatureReading(
    val minutesAgo: Int,
    val celsius: Double
)
