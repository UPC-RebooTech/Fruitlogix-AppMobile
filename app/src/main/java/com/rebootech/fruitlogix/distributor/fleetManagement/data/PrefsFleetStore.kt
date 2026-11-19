package com.rebootech.fruitlogix.distributor.fleetManagement.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Assignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleType
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Drivers, vehicles and assigned orders saved on this phone (SharedPreferences, JSON).
 * The server has no drivers or vehicles yet: a delivery only stores the driver's name and
 * the plate as text. When the server adds them, this store is replaced by API calls.
 */
class PrefsFleetStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    /** Plain copies with dates and enums as text, so Gson needs no adapters. */
    private data class DriverJson(
        val id: String, val name: String, val phone: String, val licenseClass: String, val licenseExpiry: String,
        val coldChainCertified: Boolean, val status: String,
        val unitCode: String?, val plate: String?, val orderId: String?, val route: String?, val restUntil: String?
    )

    private data class VehicleJson(
        val id: String, val unitCode: String, val plate: String, val type: String, val capacityKg: Double,
        val minCelsius: Double, val maxCelsius: Double, val status: String, val driverName: String?
    )

    /** null when nothing was saved yet (first run): the sample fleet is used. */
    fun loadDrivers(): List<Driver>? = read<List<DriverJson>>(KEY_DRIVERS)?.mapNotNull { it.toDriver() }

    fun loadVehicles(): List<Vehicle>? = read<List<VehicleJson>>(KEY_VEHICLES)?.mapNotNull { it.toVehicle() }

    fun loadAssignedOrders(): Set<String> = read<List<String>>(KEY_ASSIGNED)?.toSet().orEmpty()

    fun save(drivers: List<Driver>, vehicles: List<Vehicle>, assignedOrders: Set<String>) {
        prefs.edit {
            putString(KEY_DRIVERS, gson.toJson(drivers.map { it.toJson() }))
            putString(KEY_VEHICLES, gson.toJson(vehicles.map { it.toJson() }))
            putString(KEY_ASSIGNED, gson.toJson(assignedOrders.toList()))
        }
    }

    private inline fun <reified T> read(key: String): T? {
        val json = prefs.getString(key, null) ?: return null
        return runCatching { gson.fromJson<T>(json, object : TypeToken<T>() {}.type) }.getOrNull()
    }

    private fun Driver.toJson() = DriverJson(
        id, name, phone, licenseClass, licenseExpiry.toString(), coldChainCertified, status.name,
        assignment?.unitCode, assignment?.plate, assignment?.orderId, assignment?.route, restUntil?.toString()
    )

    private fun DriverJson.toDriver(): Driver? = runCatching {
        Driver(
            id = id, name = name, phone = phone, licenseClass = licenseClass,
            licenseExpiry = LocalDate.parse(licenseExpiry), coldChainCertified = coldChainCertified,
            status = DriverStatus.valueOf(status),
            assignment = if (unitCode != null && plate != null && orderId != null) Assignment(unitCode, plate, orderId, route.orEmpty()) else null,
            restUntil = restUntil?.let { LocalDateTime.parse(it) }
        )
    }.getOrNull()

    private fun Vehicle.toJson() = VehicleJson(id, unitCode, plate, type.name, capacityKg, minCelsius, maxCelsius, status.name, driverName)

    private fun VehicleJson.toVehicle(): Vehicle? = runCatching {
        Vehicle(id, unitCode, plate, VehicleType.valueOf(type), capacityKg, minCelsius, maxCelsius, VehicleStatus.valueOf(status), driverName)
    }.getOrNull()

    private companion object {
        const val PREFS = "distributor_fleet"
        const val KEY_DRIVERS = "drivers"
        const val KEY_VEHICLES = "vehicles"
        const val KEY_ASSIGNED = "assigned_orders"
    }
}
