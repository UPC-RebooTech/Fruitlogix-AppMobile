package com.rebootech.fruitlogix.fleetManagement

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LicenseValidityTest {

    @Test
    fun validLicense_moreThan30Days_returnsValid() {
        val today = LocalDate.of(2026, 6, 1)
        val expiry = LocalDate.of(2026, 8, 15)

        val result = LicenseValidity.checkValidity(expiry, today)
        assertEquals(LicenseValidityState.Valid, result)
    }

    @Test
    fun expiringSoonLicense_within30Days_returnsExpiringSoon() {
        val today = LocalDate.of(2026, 6, 1)
        val expiry = LocalDate.of(2026, 6, 21)

        val result = LicenseValidity.checkValidity(expiry, today)
        assertTrue(result is LicenseValidityState.ExpiringSoon)
        assertEquals(20, (result as LicenseValidityState.ExpiringSoon).daysRemaining)
    }

    @Test
    fun expiredLicense_beforeCurrentDate_returnsExpired() {
        val today = LocalDate.of(2026, 6, 1)
        val expiry = LocalDate.of(2026, 5, 31)

        val result = LicenseValidity.checkValidity(expiry, today)
        assertEquals(LicenseValidityState.Expired, result)
    }
}