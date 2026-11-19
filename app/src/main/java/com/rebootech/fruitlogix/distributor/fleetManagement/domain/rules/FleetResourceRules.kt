package com.rebootech.fruitlogix.distributor.fleetManagement.domain.rules

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverDraft
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleDraft
import java.time.LocalDate

/** Rules to add drivers and vehicles to the distributor's fleet. */
object FleetResourceRules {

    /** Peruvian license categories that allow driving trucks (MTC). */
    val TRUCK_LICENSES = listOf("A-IIb", "A-IIIa", "A-IIIb", "A-IIIc")

    /** Reefer trucks go from a small van (~1 t) to a full trailer (~30 t). */
    const val MIN_CAPACITY_KG = 500.0
    const val MAX_CAPACITY_KG = 40_000.0

    /** Range a reefer unit can hold. */
    const val MIN_REEFER_CELSIUS = -30.0
    const val MAX_REEFER_CELSIUS = 30.0

    enum class DriverError { NAME, PHONE, PHONE_TAKEN, LICENSE_CLASS, LICENSE_EXPIRY }
    enum class VehicleError { UNIT_CODE, UNIT_CODE_TAKEN, PLATE, PLATE_TAKEN, CAPACITY, TEMPERATURE }

    private val PHONE_DIGITS = Regex("\\d{9,12}")

    /** Peruvian plates: three letters/digits, a dash and three digits (e.g. "C7X-915"). */
    private val PLATE = Regex("^[A-Z0-9]{3}-\\d{3}$")

    fun digits(phone: String) = phone.filter { it.isDigit() }

    fun validateDriver(draft: DriverDraft, existing: List<Driver>, today: LocalDate = LocalDate.now()): Set<DriverError> = buildSet {
        if (draft.name.trim().length < 3) add(DriverError.NAME)
        val phone = digits(draft.phone)
        if (!PHONE_DIGITS.matches(phone)) add(DriverError.PHONE)
        else if (existing.any { digits(it.phone).takeLast(9) == phone.takeLast(9) }) add(DriverError.PHONE_TAKEN)
        if (draft.licenseClass !in TRUCK_LICENSES) add(DriverError.LICENSE_CLASS)
        // An expired license cannot be registered: the driver could never be assigned
        if (draft.licenseExpiry == null || draft.licenseExpiry.isBefore(today)) add(DriverError.LICENSE_EXPIRY)
    }

    fun normalizePlate(plate: String): String = plate.trim().uppercase().replace(" ", "")

    fun validateVehicle(draft: VehicleDraft, existing: List<Vehicle>): Set<VehicleError> = buildSet {
        val code = draft.unitCode.trim().uppercase()
        if (code.length < 3) add(VehicleError.UNIT_CODE)
        else if (existing.any { it.unitCode.equals(code, ignoreCase = true) }) add(VehicleError.UNIT_CODE_TAKEN)
        val plate = normalizePlate(draft.plate)
        if (!PLATE.matches(plate)) add(VehicleError.PLATE)
        else if (existing.any { normalizePlate(it.plate) == plate }) add(VehicleError.PLATE_TAKEN)
        val capacity = draft.capacityKg
        if (capacity == null || capacity < MIN_CAPACITY_KG || capacity > MAX_CAPACITY_KG) add(VehicleError.CAPACITY)
        val min = draft.minCelsius
        val max = draft.maxCelsius
        if (min == null || max == null || min >= max || min < MIN_REEFER_CELSIUS || max > MAX_REEFER_CELSIUS) add(VehicleError.TEMPERATURE)
    }
}
