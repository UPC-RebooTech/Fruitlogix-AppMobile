package com.rebootech.fruitlogix.producer.qualityControl.inspection.data

import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.Caliber
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.repository.InspectionRepository

/** In-memory lots and drafts until the quality backend exists. */
class FakeInspectionRepository : InspectionRepository {

    private val avocadoCalibers = listOf(
        Caliber(12, 300), Caliber(14, 250), Caliber(16, 220), Caliber(18, 200), Caliber(20, 175)
    )
    private val mangoCalibers = listOf(
        Caliber(8, 600), Caliber(9, 530), Caliber(10, 480), Caliber(12, 400)
    )

    private val lots = listOf(
        InspectionLot(
            orderId = "1044",
            lotCode = "KM-04",
            clientName = "Mercado Central S.A.",
            productName = "Kent Mango",
            netWeightKg = 400,
            crates = 20,
            crateKg = 20,
            previousRejectionReason = "Magulladuras sobre el límite de 4 % · se pidió reclasificar",
            initialCoreTempC = 9.4,
            initialMoisturePct = 74.0,
            initialDefectsPct = 5.2,
            initialVisualScore = 3,
            calibers = mangoCalibers,
            initialCalibers = setOf(9, 10)
        ),
        InspectionLot(
            orderId = "1048",
            lotCode = "HA-09",
            clientName = "Supermercados Wong",
            productName = "Hass Avocado",
            netWeightKg = 300,
            crates = 15,
            crateKg = 20,
            previousRejectionReason = null,
            initialCoreTempC = 6.2,
            initialMoisturePct = 78.5,
            initialDefectsPct = 1.8,
            initialVisualScore = 4,
            calibers = avocadoCalibers,
            initialCalibers = setOf(16, 18)
        )
    )

    private val drafts = mutableMapOf<String, InspectionDraft>()

    override fun lotForOrder(orderId: String): InspectionLot? {
        val id = orderId.removePrefix("#").removePrefix("ORD-").trim()
        return lots.find { it.orderId == id }
    }

    override fun findByLotCode(code: String): InspectionLot? {
        val clean = code.uppercase().replace("LOT", "").replace("#", "").trim()
        return lots.find { it.lotCode.equals(clean, ignoreCase = true) }
    }

    override fun draftFor(orderId: String): InspectionDraft? = drafts[orderId.removePrefix("#")]

    override fun saveDraft(draft: InspectionDraft) {
        drafts[draft.orderId] = draft
    }

    companion object {
        val instance: InspectionRepository by lazy { FakeInspectionRepository() }
    }
}
