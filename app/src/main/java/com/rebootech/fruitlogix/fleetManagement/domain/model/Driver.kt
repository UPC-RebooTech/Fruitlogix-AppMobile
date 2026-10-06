package com.rebootech.fruitlogix.fleetManagement.domain.model

import java.time.LocalDate

data class Driver(
    val id: String,
    val name: String,
    val licenseClass: String,
    val specialty: String,
    val licenseExpiry: LocalDate,
    val status: DriverStatus,
    val currentAssignment: DriverAssignment? = null
) {
    val initials: String
        get() {
            val parts = name.trim().split(" ")
            return when {
                parts.isEmpty() -> ""
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts.last().first()}".uppercase()
            }
        }
}
