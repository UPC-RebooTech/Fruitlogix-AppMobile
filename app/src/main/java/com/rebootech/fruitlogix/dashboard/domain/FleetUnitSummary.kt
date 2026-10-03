package com.rebootech.fruitlogix.dashboard.domain

import androidx.annotation.StringRes

enum class FleetStatusType {
    ON_TIME,
    DELAYED,
    WARNING
}

data class FleetUnitSummary(
    val unitId: String,
    val truckModel: String,
    val routeDescription: String,
    @StringRes val statusTextRes: Int,
    val statusType: FleetStatusType,
    val reeferTemp: String,
    val humidity: String,
    val destEtaOrDelay: String,
    val isDelay: Boolean = false,
    val mileageText: String,
    val progressPercent: Float,
    val progressLabel: String
)
