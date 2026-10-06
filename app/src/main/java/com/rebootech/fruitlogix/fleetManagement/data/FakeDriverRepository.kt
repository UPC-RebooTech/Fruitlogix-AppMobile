package com.rebootech.fruitlogix.fleetManagement.data

import com.rebootech.fruitlogix.fleetManagement.domain.model.Driver
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverAssignment
import com.rebootech.fruitlogix.fleetManagement.domain.model.DriverStatus
import com.rebootech.fruitlogix.fleetManagement.domain.repository.DriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

class FakeDriverRepository : DriverRepository {

    private val sampleDrivers = listOf(
        Driver(
            id = "driver_1",
            name = "Jorge Huamán",
            licenseClass = "A-IIIc",
            specialty = "Cold-chain certified",
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
            specialty = "Cold-chain certified",
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
            specialty = "Freight senior",
            licenseExpiry = LocalDate.now().plusDays(20),
            status = DriverStatus.AVAILABLE,
            currentAssignment = null
        ),
        Driver(
            id = "driver_4",
            name = "Luis Quispe",
            licenseClass = "A-IIb",
            specialty = "Regional carrier",
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
            specialty = "Heavy cargo",
            licenseExpiry = LocalDate.of(2023, 10, 12),
            status = DriverStatus.EXPIRED,
            currentAssignment = DriverAssignment(
                cannotBeAssignedReason = "License expired (12/10/2023)"
            )
        )
    )

    override fun getDrivers(): Flow<List<Driver>> {
        return flowOf(sampleDrivers)
    }
}
