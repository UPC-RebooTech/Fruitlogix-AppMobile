package com.rebootech.fruitlogix.producer.qualityControl.reports.data

import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus

object QualityReportRepository {
    val reports = mutableStateListOf(
        QualityReportItem(
            id = "1",
            orderNumber = "#1048",
            clientName = "Supermercados Wong",
            timestamp = "Today, 10:48 AM",
            locationInfo = "Bay 3 Loading Dock",
            status = QualityStatus.PENDING,
            productName = "Hass Avocado",
            weightKg = 300,
            cratesCount = 15,
            lotCode = "Lot #HA-882",
            visualScore = 4.0,
            defectsPercentage = 2.1,
            coreTemp = 6.5,
            humidityPercentage = 88,
            inspectorName = "Carlos Melgar"
        ),
        QualityReportItem(
            id = "2",
            orderNumber = "#1041",
            clientName = "Supermercados Wong",
            timestamp = "Today, 09:15 AM",
            locationInfo = "Pre-Cool Tunnel B",
            status = QualityStatus.APPROVED,
            productName = "Blueberries",
            weightKg = 120,
            cratesCount = 12,
            lotCode = "Lot #BB-109",
            visualScore = 5.0,
            defectsPercentage = 0.8,
            coreTemp = 3.5,
            humidityPercentage = 92,
            inspectorName = "Elena Quispe"
        ),
        QualityReportItem(
            id = "3",
            orderNumber = "#1044",
            clientName = "Mercado Central S.A.",
            timestamp = "Yesterday, 16:30 PM",
            locationInfo = "Inspection Bay 1",
            status = QualityStatus.REJECTED,
            productName = "Hass Avocado",
            weightKg = 450,
            cratesCount = 22,
            lotCode = "Lot #HA-791",
            visualScore = 3.0,
            defectsPercentage = 5.4,
            coreTemp = 7.2,
            humidityPercentage = 85,
            inspectorName = "Carlos Melgar",
            alertMessage = "Quality Alert: Bruising 5.4%. Exceeded standard 3.5% threshold. Lot quarantined for re-grading."
        ),
        QualityReportItem(
            id = "4",
            orderNumber = "#1030",
            clientName = "Cencosud Retail",
            timestamp = "Oct 29, 14:10 PM",
            locationInfo = "Main Packhouse Line 2",
            status = QualityStatus.APPROVED,
            productName = "Red Globe Grapes",
            weightKg = 600,
            cratesCount = 30,
            lotCode = "Lot #RG-402",
            visualScore = 4.8,
            defectsPercentage = 1.2,
            coreTemp = 4.0,
            humidityPercentage = 90,
            inspectorName = "Elena Quispe"
        )
    )
}