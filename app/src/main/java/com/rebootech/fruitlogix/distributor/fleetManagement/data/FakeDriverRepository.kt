package com.rebootech.fruitlogix.distributor.fleetManagement.data

import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverAssignment
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.PendingDispatchOption
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.Vehicle
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.model.VehicleStatus
import com.rebootech.fruitlogix.distributor.fleetManagement.domain.repository.DriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class FakeDriverRepository : DriverRepository {

    companion object {
        private val driversFlow = MutableStateFlow(
            listOf(
                Driver(
                    id = "driver_1",
                    name = "Jorge Huamán",
                    licenseClass = "A-IIIc",
                    specialty = "Cold-chain certified • +51 987 111 222",
                    licenseExpiry = LocalDate.of(2027, 11, 30),
                    status = DriverStatus.ON_ROUTE,
                    currentAssignment = DriverAssignment(
                        unitCode = "FL-102",
                        plateNumber = "BQK-482",
                        routeName = "Ica → Lima Central Hub"
                    )
                ),
                Driver(
                    id = "driver_2",
                    name = "Mateo Silva",
                    licenseClass = "A-IIIc",
                    specialty = "Cold-chain certified • +51 987 333 444",
                    licenseExpiry = LocalDate.of(2028, 3, 31),
                    status = DriverStatus.ON_ROUTE,
                    currentAssignment = DriverAssignment(
                        unitCode = "FL-408",
                        plateNumber = "AZT-901",
                        routeName = "Chavín de Huántar → Callao Cold Hub"
                    )
                ),
                Driver(
                    id = "driver_3",
                    name = "Carlos Mendoza Jr.",
                    licenseClass = "A-IIIc",
                    specialty = "Freight senior • +51 987 555 666",
                    licenseExpiry = LocalDate.now().plusDays(20),
                    status = DriverStatus.AVAILABLE,
                    currentAssignment = null
                ),
                Driver(
                    id = "driver_4",
                    name = "Luis Quispe",
                    licenseClass = "A-IIb",
                    specialty = "Regional carrier • +51 987 777 888",
                    licenseExpiry = LocalDate.of(2026, 8, 31),
                    status = DriverStatus.OFF_DUTY,
                    currentAssignment = DriverAssignment(
                        restPeriodEnds = "Rest period ends tomorrow 06:00 PET"
                    )
                ),
                Driver(
                    id = "driver_5",
                    name = "Ricardo Paredes",
                    licenseClass = "A-IIIc",
                    specialty = "Heavy cargo • +51 987 999 000",
                    licenseExpiry = LocalDate.of(2023, 10, 12),
                    status = DriverStatus.EXPIRED,
                    currentAssignment = DriverAssignment(
                        cannotBeAssignedReason = "Cannot be assigned • License expired (12/10/2023)"
                    )
                )
            )
        )

        private val vehiclesFlow = MutableStateFlow(
            listOf(
                Vehicle(
                    id = "veh_1",
                    plate = "BQK-482",
                    unitCode = "Unit FL-102",
                    type = "Furgón Reefer Isotérmico",
                    capacityTons = 8.5,
                    tempRange = "2.0°C a 6.0°C",
                    status = VehicleStatus.ON_ROUTE,
                    currentRoute = "Ica → Lima Central Hub (Jorge Huamán)"
                ),
                Vehicle(
                    id = "veh_2",
                    plate = "AZT-901",
                    unitCode = "Unit FL-408",
                    type = "Camión Frigorífico Pesado",
                    capacityTons = 12.0,
                    tempRange = "0.0°C a 4.0°C",
                    status = VehicleStatus.ON_ROUTE,
                    currentRoute = "Chavín de Huántar → Callao Cold Hub (Mateo Silva)"
                ),
                Vehicle(
                    id = "veh_3",
                    plate = "CXP-112",
                    unitCode = "Unit FL-219",
                    type = "Furgón Reefer Mediano",
                    capacityTons = 5.0,
                    tempRange = "2.0°C a 8.0°C",
                    status = VehicleStatus.AVAILABLE,
                    currentRoute = "Disponible en Lima Central Hub"
                ),
                Vehicle(
                    id = "veh_4",
                    plate = "FGH-887",
                    unitCode = "Unit FL-305",
                    type = "Camión Ligero Refrigerado",
                    capacityTons = 3.5,
                    tempRange = "4.0°C a 10.0°C",
                    status = VehicleStatus.MAINTENANCE,
                    currentRoute = "Calibración de compresor térmico"
                )
            )
        )

        private val pendingDispatchesFlow = MutableStateFlow(
            listOf(
                PendingDispatchOption(
                    dispatchId = "DSP-901",
                    orderCode = "ORD-2026-104",
                    productDescription = "Palta Hass (1,800 kg)",
                    destination = "Huaral → Mercado Mayorista Santa Anita"
                ),
                PendingDispatchOption(
                    dispatchId = "DSP-902",
                    orderCode = "ORD-2026-108",
                    productDescription = "Arándano Biloxi (950 kg)",
                    destination = "Cañete → Centro de Distribución Surco"
                )
            )
        )
    }

    override fun getDrivers(): Flow<List<Driver>> = driversFlow.asStateFlow()

    override fun getVehicles(): Flow<List<Vehicle>> = vehiclesFlow.asStateFlow()

    override fun getPendingDispatches(): Flow<List<PendingDispatchOption>> = pendingDispatchesFlow.asStateFlow()

    override fun assignResourcesToDispatch(
        dispatchId: String,
        driverId: String,
        vehicleId: String
    ): Result<String> {
        val dispatch = pendingDispatchesFlow.value.find { it.dispatchId == dispatchId }
            ?: return Result.failure(IllegalArgumentException("El despacho seleccionado ya no está pendiente."))

        val driver = driversFlow.value.find { it.id == driverId }
            ?: return Result.failure(IllegalArgumentException("Conductor no encontrado."))

        val vehicle = vehiclesFlow.value.find { it.id == vehicleId }
            ?: return Result.failure(IllegalArgumentException("Vehículo no encontrado."))

        if (driver.status != DriverStatus.AVAILABLE) {
            return Result.failure(
                IllegalArgumentException("Recurso no disponible: El conductor ${driver.name} se encuentra en estado${driver.status}.")
            )
        }

        if (vehicle.status != VehicleStatus.AVAILABLE) {
            return Result.failure(
                IllegalArgumentException("Recurso no disponible: El vehículo ${vehicle.plate} se encuentra en estado${vehicle.status.label}.")
            )
        }

        driversFlow.value = driversFlow.value.map {
            if (it.id == driverId) {
                it.copy(
                    status = DriverStatus.ON_ROUTE,
                    currentAssignment = DriverAssignment(
                        unitCode = vehicle.unitCode.removePrefix("Unit "),
                        plateNumber = vehicle.plate,
                        routeName = "${dispatch.orderCode} •${dispatch.destination}"
                    )
                )
            } else it
        }

        vehiclesFlow.value = vehiclesFlow.value.map {
            if (it.id == vehicleId) {
                it.copy(
                    status = VehicleStatus.ON_ROUTE,
                    currentRoute = "${dispatch.destination} (${driver.name})"
                )
            } else it
        }

        pendingDispatchesFlow.value = pendingDispatchesFlow.value.filterNot { it.dispatchId == dispatchId }

        return Result.success(
            "Despacho ${dispatch.orderCode} asignado a ${driver.name} con unidad${vehicle.plate}. Cliente notificado."
        )
    }
}