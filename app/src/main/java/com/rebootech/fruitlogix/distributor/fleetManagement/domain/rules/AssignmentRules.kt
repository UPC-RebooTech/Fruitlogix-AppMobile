package com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatch
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidity
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.service.LicenseValidityState
import java.time.LocalDate

/** Assign a driver and a vehicle to an order (US79). */
object AssignmentRules {

    enum class Error {
        ORDER_REQUIRED, DRIVER_REQUIRED, VEHICLE_REQUIRED,
        DRIVER_NOT_AVAILABLE, LICENSE_EXPIRED, VEHICLE_NOT_AVAILABLE, OVER_CAPACITY
    }

    fun isLicenseExpired(driver: Driver, today: LocalDate = LocalDate.now()) =
        LicenseValidity.checkValidity(driver.licenseExpiry, today) is LicenseValidityState.Expired

    /** A driver that can be picked: available and with a valid license. */
    fun canDrive(driver: Driver, today: LocalDate = LocalDate.now()) =
        driver.status == DriverStatus.AVAILABLE && !isLicenseExpired(driver, today)

    fun validate(
        order: PendingDispatch?,
        driver: Driver?,
        vehicle: Vehicle?,
        today: LocalDate = LocalDate.now()
    ): Error? = when {
        order == null -> Error.ORDER_REQUIRED
        driver == null -> Error.DRIVER_REQUIRED
        vehicle == null -> Error.VEHICLE_REQUIRED
        isLicenseExpired(driver, today) -> Error.LICENSE_EXPIRED
        driver.status != DriverStatus.AVAILABLE -> Error.DRIVER_NOT_AVAILABLE
        vehicle.status != VehicleStatus.AVAILABLE -> Error.VEHICLE_NOT_AVAILABLE
        order.quantityTons > vehicle.capacityTons -> Error.OVER_CAPACITY
        else -> null
    }
}
