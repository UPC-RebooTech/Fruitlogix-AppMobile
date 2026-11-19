package com.rebootech.fruitlogix.fleetManagement

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules.DriverError
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.FleetResourceRules.VehicleError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FleetResourceRulesTest {

    private val today = LocalDate.of(2026, 11, 13)
    private val existingDriver = Driver("d1", "Jorge Huamán", "+51 987 112 340", "A-IIIc", today.plusYears(1), true, DriverStatus.AVAILABLE)
    private val existingVehicle = Vehicle("v1", "FL-408", "C7X-915", VehicleType.HEAVY_REEFER, 12_000.0, 0.0, 8.0, VehicleStatus.AVAILABLE)

    private val driver = DriverDraft("Pedro Salazar", "933 210 456", "A-IIIc", today.plusMonths(6), true)
    private val vehicle = VehicleDraft("FL-610", "abc-123", VehicleType.REEFER_TRUCK, 12_000.0, -1.0, 8.0)

    @Test
    fun validDriver_isAccepted() {
        assertTrue(FleetResourceRules.validateDriver(driver, listOf(existingDriver), today).isEmpty())
    }

    @Test
    fun driver_needsValidDataAndAFutureLicense() {
        val errors = FleetResourceRules.validateDriver(DriverDraft("Al", "123", "B-I", today.minusDays(1), false), emptyList(), today)
        assertEquals(setOf(DriverError.NAME, DriverError.PHONE, DriverError.LICENSE_CLASS, DriverError.LICENSE_EXPIRY), errors)
        assertEquals(setOf(DriverError.LICENSE_EXPIRY), FleetResourceRules.validateDriver(driver.copy(licenseExpiry = null), emptyList(), today))
    }

    @Test
    fun driver_samePhoneIsRefused() {
        // Compared by the last 9 digits: "+51 987 112 340" and "987112340" are the same phone
        assertEquals(
            setOf(DriverError.PHONE_TAKEN),
            FleetResourceRules.validateDriver(driver.copy(phone = "987112340"), listOf(existingDriver), today)
        )
    }

    @Test
    fun validVehicle_isAccepted_andPlateIsNormalized() {
        assertTrue(FleetResourceRules.validateVehicle(vehicle, listOf(existingVehicle)).isEmpty())
        assertEquals("ABC-123", FleetResourceRules.normalizePlate(" abc-123 "))
    }

    @Test
    fun vehicle_rulesAndDuplicates() {
        val errors = FleetResourceRules.validateVehicle(VehicleDraft("F", "ABC123", VehicleType.LIGHT_REEFER, 100.0, 8.0, -1.0), emptyList())
        assertEquals(setOf(VehicleError.UNIT_CODE, VehicleError.PLATE, VehicleError.CAPACITY, VehicleError.TEMPERATURE), errors)
        val duplicate = FleetResourceRules.validateVehicle(vehicle.copy(unitCode = "fl-408", plate = "C7X-915"), listOf(existingVehicle))
        assertEquals(setOf(VehicleError.UNIT_CODE_TAKEN, VehicleError.PLATE_TAKEN), duplicate)
    }
}
