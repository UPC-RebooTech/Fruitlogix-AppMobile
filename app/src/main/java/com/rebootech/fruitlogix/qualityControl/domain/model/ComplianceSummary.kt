package com.rebootech.fruitlogix.qualityControl.domain.model

data class ComplianceSummary(
    val compliancePercent: Int,
    val transitIntegrityPercent: Int,
    val coldBreachesToday: Int,
    val predictiveRiskThermalCount: Int,
    val predictiveRiskUnitId: String
)