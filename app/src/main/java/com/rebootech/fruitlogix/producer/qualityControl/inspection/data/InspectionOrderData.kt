package com.rebootech.fruitlogix.producer.qualityControl.inspection.data

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
    fun getOrderById(orderId: String): InspectionOrderDetails {
        val cleanId = orderId.replace("#", "").trim()

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