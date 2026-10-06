package com.rebootech.fruitlogix.fleetManagement.domain.service

import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface LicenseValidityState {
    object Expired : LicenseValidityState
    data class ExpiringSoon(val daysRemaining: Int) : LicenseValidityState
    object Valid : LicenseValidityState
}

object LicenseValidity {
    fun checkValidity(
        expiryDate: LocalDate,
        currentDate: LocalDate = LocalDate.now()
    ): LicenseValidityState {
        if (expiryDate.isBefore(currentDate)) {
            return LicenseValidityState.Expired
        }
        val daysRemaining = ChronoUnit.DAYS.between(currentDate, expiryDate).toInt()
        return when {
            daysRemaining in 0..30 -> LicenseValidityState.ExpiringSoon(daysRemaining)
            else -> LicenseValidityState.Valid
        }
    }
}
