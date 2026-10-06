package com.rebootech.fruitlogix.fleetManagement

import com.rebootech.fruitlogix.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.fleetManagement.domain.service.LicenseValidityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LicenseValidityTest {

    private val currentDate: LocalDate = LocalDate.of(2026, 10, 6)

    @Test
    fun checkValidity_returnsExpired_whenExpiryDateIsInThePast() {
        val expiryDate = LocalDate.of(2023, 10, 12)
        val result = LicenseValidity.checkValidity(expiryDate, currentDate)
        assertEquals(LicenseValidityState.Expired, result)
    }

    @Test
    fun checkValidity_returnsExpiringSoon_whenExpiryDateIsWithin30Days() {
        val expiryDate = LocalDate.of(2026, 10, 26) // 20 days remaining
        val result = LicenseValidity.checkValidity(expiryDate, currentDate)
        assertTrue(result is LicenseValidityState.ExpiringSoon)
        assertEquals(20, (result as LicenseValidityState.ExpiringSoon).daysRemaining)
    }

    @Test
    fun checkValidity_returnsValid_whenExpiryDateIsMoreThan30DaysInFuture() {
        val expiryDate = LocalDate.of(2027, 11, 30)
        val result = LicenseValidity.checkValidity(expiryDate, currentDate)
        assertEquals(LicenseValidityState.Valid, result)
    }
}
