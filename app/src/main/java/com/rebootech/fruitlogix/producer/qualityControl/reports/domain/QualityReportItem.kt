package com.rebootech.fruitlogix.producer.qualityControl.reports.domain

enum class QualityStatus {
    PENDING, APPROVED, REJECTED
}

data class QualityReportItem(
    val id: String,
    val orderNumber: String,
    val clientName: String,
    val timestamp: String,
    val locationInfo: String,
    val status: QualityStatus,
    val productName: String,
    val weightKg: Int,
    val cratesCount: Int,
    val lotCode: String,
    val visualScore: Double,
    val defectsPercentage: Double,
    val coreTemp: Double,
    val humidityPercentage: Int,
    val inspectorName: String,
    val alertMessage: String? = null
)