package com.rebootech.fruitlogix.producer.qualityControl.reports.data

import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualitySummary
import java.time.LocalDate
import java.time.LocalTime

object QualityReportRepository {
    private val today: LocalDate = LocalDate.now()

    val summary = QualitySummary(
        passRatePct = 92.8,
        weeklyDeltaPct = 1.4,
        sampledTodayKg = 1470,
        cratesInspectedToday = 42
    )

    val reports = mutableStateListOf(
        QualityReportItem(
            id = "1048", // Coincide con el orderNumber
            orderNumber = "#1048",
            clientName = "Supermercados Wong",
            inspectedAt = today.atTime(LocalTime.of(10, 48)),
            locationInfo = "Bay 3 Loading Dock",
            status = QualityStatus.PENDING,
            productName = "Hass Avocado",
            weightKg = 300,
            cratesCount = 15,
            lotCode = "HA-882",
            visualScore = 4.0,
            defectsPercentage = 2.1,
            coreTemp = 6.5,
            humidityPercentage = 88,
            inspectorName = "Carlos Melgar",
            harvestedOn = today.minusDays(1)
        ),
        QualityReportItem(
            id = "1041",
            orderNumber = "#1041",
            clientName = "Supermercados Wong",
            inspectedAt = today.atTime(LocalTime.of(9, 15)),
            locationInfo = "Pre-Cool Tunnel B",
            status = QualityStatus.APPROVED,
            productName = "Blueberries",
            weightKg = 120,
            cratesCount = 12,
            lotCode = "BB-109",
            visualScore = 5.0,
            defectsPercentage = 0.8,
            coreTemp = 3.5,
            humidityPercentage = 92,
            inspectorName = "Elena Quispe",
            harvestedOn = today.minusDays(2),
            inspectorNotes = "Fruta firme con pruina natural intacta. Calibre uniforme (14-16 mm). Sin bayas blandas en 3 bandejas de muestreo."
        ),
        QualityReportItem(
            id = "1044",
            orderNumber = "#1044",
            clientName = "Mercado Central S.A.",
            inspectedAt = today.minusDays(1).atTime(LocalTime.of(16, 30)),
            locationInfo = "Inspection Bay 1",
            status = QualityStatus.REJECTED,
            productName = "Kent Mango",
            weightKg = 400,
            cratesCount = 20,
            lotCode = "KM-04",
            visualScore = 3.0,
            defectsPercentage = 5.4,
            coreTemp = 7.2,
            humidityPercentage = 85,
            inspectorName = "Carlos Melgar",
            harvestedOn = today.minusDays(1)
        ),
        QualityReportItem(
            id = "1030",
            orderNumber = "#1030",
            clientName = "Cencosud Retail",
            inspectedAt = today.minusDays(13).atTime(LocalTime.of(14, 10)),
            locationInfo = "Main Packhouse Line 2",
            status = QualityStatus.APPROVED,
            productName = "Red Globe Grapes",
            weightKg = 600,
            cratesCount = 30,
            lotCode = "RG-402",
            visualScore = 4.8,
            defectsPercentage = 1.2,
            coreTemp = 4.0,
            humidityPercentage = 90,
            inspectorName = "Elena Quispe",
            harvestedOn = today.minusDays(14)
        )
    )

    /** Adds or replaces the report of an order after the producer sends it (status PENDING). */
    fun submit(report: QualityReportItem) {
        val index = reports.indexOfFirst { it.id == report.id }
        if (index >= 0) reports[index] = report else reports.add(0, report)
    }
}
