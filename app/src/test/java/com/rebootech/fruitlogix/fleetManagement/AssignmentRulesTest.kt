package com.rebootech.fruitlogix.fleetManagement

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules.AssignmentRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AssignmentRulesTest {

    private val today = LocalDate.of(2026, 11, 12)
    private val order = PendingDispatch("FX-1040", "Mercados Lima Sur", "Fresa", 11_200.0)
    private val driver = Driver("d4", "Pedro Salazar", "+51 933 210 456", "A-IIIc", today.plusDays(20), true, DriverStatus.AVAILABLE)
    private val bigTruck = Vehicle("v4", "FL-510", "HJK-264", VehicleType.HEAVY_REEFER, 14_000.0, -1.0, 10.0, VehicleStatus.AVAILABLE)

    @Test
    fun validAssignment_isAccepted() {
        assertNull(AssignmentRules.validate(order, driver, bigTruck, today))
    }

    @Test
    fun everyChoiceIsRequired() {
        assertEquals(AssignmentRules.Error.ORDER_REQUIRED, AssignmentRules.validate(null, driver, bigTruck, today))
        assertEquals(AssignmentRules.Error.DRIVER_REQUIRED, AssignmentRules.validate(order, null, bigTruck, today))
        assertEquals(AssignmentRules.Error.VEHICLE_REQUIRED, AssignmentRules.validate(order, driver, null, today))
    }

    @Test
    fun expiredLicense_cannotBeAssigned() {
        val expired = driver.copy(licenseExpiry = today.minusDays(1))
        assertEquals(AssignmentRules.Error.LICENSE_EXPIRED, AssignmentRules.validate(order, expired, bigTruck, today))
        assertFalse(AssignmentRules.canDrive(expired, today))
        assertTrue(AssignmentRules.canDrive(driver, today))
    }

    @Test
    fun busyDriverOrVehicle_isRefused() {
        assertEquals(
            AssignmentRules.Error.DRIVER_NOT_AVAILABLE,
            AssignmentRules.validate(order, driver.copy(status = DriverStatus.OFF_DUTY), bigTruck, today)
        )
        assertEquals(
            AssignmentRules.Error.VEHICLE_NOT_AVAILABLE,
            AssignmentRules.validate(order, driver, bigTruck.copy(status = VehicleStatus.MAINTENANCE), today)
        )
    }

    @Test
    fun cargoAboveCapacity_isRefused() {
        val van = bigTruck.copy(capacityKg = 8_000.0)
        assertEquals(AssignmentRules.Error.OVER_CAPACITY, AssignmentRules.validate(order, driver, van, today))
    }

    @Test
    fun routeDistance_mustBePositiveAndRealistic() {
        assertTrue(AssignmentRules.isValidRouteKm(380.0))
        assertFalse(AssignmentRules.isValidRouteKm(null))
        assertFalse(AssignmentRules.isValidRouteKm(0.0))
        assertFalse(AssignmentRules.isValidRouteKm(5000.0))
    }

    @Test
    fun arrival_isEstimatedFromTheDistance() {
        val departure = java.time.LocalDateTime.of(2026, 11, 13, 8, 0)
        // 380 km at 50 km/h = 7 h 36 min
        assertEquals(java.time.LocalDateTime.of(2026, 11, 13, 15, 36), AssignmentRules.estimatedArrival(departure, 380.0))
    }
}
