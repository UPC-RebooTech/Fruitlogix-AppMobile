package com.rebootech.fruitlogix.distributor.fleetManagement.domain.model

data class DriverAssignment(
    val unitCode: String? = null,
    val plateNumber: String? = null,
    val routeName: String? = null,
    val restPeriodEnds: String? = null,
    val cannotBeAssignedReason: String? = null
)
