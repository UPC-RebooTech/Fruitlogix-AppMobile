package com.rebootech.fruitlogix.producer.qualityControl.reports.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QualityReportRepository {
    val reports = mutableStateListOf(
        QualityReportItem(
            id = "1048",
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
            id = "1041",
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
            id = "1044",
            orderNumber = "#1044",
            clientName = "Mercado Central S.A.",
            timestamp = "Yesterday, 16:30 PM",
            locationInfo = "Inspection Bay 1",
            status = QualityStatus.REJECTED,
            productName = "Kent Mango",
            weightKg = 400,
            cratesCount = 20,
            lotCode = "Lot #KM-04",
            visualScore = 3.0,
            defectsPercentage = 5.4,
            coreTemp = 7.2,
            humidityPercentage = 85,
            inspectorName = "Carlos Melgar",
            alertMessage = "Quality Alert: Bruising 5.4%. Exceeded standard 3.5% threshold. Lot quarantined for re-grading."
        ),
        QualityReportItem(
            id = "1030",
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

    // NUEVO: Sincronización en segundo plano con el backend al cargar los reportes
    init {
        fetchReportsFromBackend()
    }

    private fun fetchReportsFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intenta sincronizar con el servicio de calidad de la API en Render
                val remoteInspections = RetrofitClient.api.getQualityInspections()
                if (remoteInspections.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Reportes de calidad sincronizados desde la nube.")
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                // Si el servidor está durmiendo, se protege usando los reportes locales intactos
                Log.e("FruitLogix-API", "Backend inactivo para reportes. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
    }
}