package com.rebootech.fruitlogix.producer.qualityControl.inspection.data

import android.util.Log
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class InspectionOrderDetails(
    val orderId: String,
    val lotCode: String,
    val clientName: String,
    val productName: String,
    val netWeight: String,
    val cratesInfo: String,
    val isRejected: Boolean = false,
    val rejectionReason: String? = null,
    val initialTemp: String,
    val initialMoisture: String,
    val initialDefects: String,
    val visualScore: Int,
    val gradeText: String,
    val isTempError: Boolean = false
)

object InspectionOrderRepository {

    // Bloque opcional: Inicializa una llamada en segundo plano a Retrofit para sincronizar
    init {
        fetchInspectionsFromBackend()
    }

    private fun fetchInspectionsFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intenta traer las inspecciones reales desde tu backend en Render
                val remoteInspections = RetrofitClient.api.getQualityInspections()
                if (remoteInspections.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Inspecciones de calidad sincronizadas desde la nube: ${remoteInspections.size}")
                    // Aquí podrías guardar o mezclar en una lista local si lo deseas
                }
            } catch (e: Exception) {
                // Si el servidor de Render está dormido, usa los datos locales de abajo sin que la app falle
                Log.e("FruitLogix-API", "Backend inactivo para inspecciones. Usando modo local: ${e.message}")
            }
        }
    }

    fun getOrderById(orderId: String): InspectionOrderDetails {
        val cleanId = orderId.removePrefix("#").removePrefix("ORD-").trim()

        return when (cleanId) {
            "1044" -> InspectionOrderDetails(
                orderId = "1044",
                lotCode = "LOT #KM-04",
                clientName = "Mercado Central S.A.",
                productName = "Kent Mango",
                netWeight = "400 KG NET",
                cratesInfo = "20 × 20 kg export crates",
                isRejected = true,
                rejectionReason = "Bruising above 4% limit · Re-grading requested",
                initialTemp = "9.4",
                initialMoisture = "74.0 %",
                initialDefects = "5.2 %",
                visualScore = 3,
                gradeText = "Grade B - Re-grading required",
                isTempError = true
            )
            else -> InspectionOrderDetails( // Por defecto o ID 1048 (Hass Avocado)
                orderId = "1048",
                lotCode = "LOT #HA-09",
                clientName = "Supermercados Wong",
                productName = "Hass Avocado",
                netWeight = "300 KG NET",
                cratesInfo = "15 × 20 kg export crates",
                isRejected = false,
                initialTemp = "6.2",
                initialMoisture = "78.5 %",
                initialDefects = "1.8 %",
                visualScore = 4,
                gradeText = "Optimal Export Grade",
                isTempError = false
            )
        }
    }
}