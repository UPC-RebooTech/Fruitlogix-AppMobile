package com.rebootech.fruitlogix.producer.qualityControl.inspection.data

import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.Caliber
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionRules
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.repository.InspectionRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.repository.InspectionSendResult
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.AssignOrderBatchRequestDto
import com.rebootech.fruitlogix.shared.core.network.CreateHarvestBatchRequestDto
import com.rebootech.fruitlogix.shared.core.network.QualityInspectionRequestDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.UpdateHarvestBatchStatusRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale
import kotlin.math.ceil

/**
 * Lots to inspect: the sample lots, plus one built from each server order of the producer.
 * Drafts are kept on the phone (work offline); a server order's report is sent with [sendReport].
 */
class FakeInspectionRepository : InspectionRepository {
    private val avocadoCalibers = listOf(
        Caliber(12, 300), Caliber(14, 250), Caliber(16, 220), Caliber(18, 200), Caliber(20, 175)
    )
    private val mangoCalibers = listOf(
        Caliber(8, 600), Caliber(9, 530), Caliber(10, 480), Caliber(12, 400)
    )
    /** Generic sizes for other fruits (count per 10 kg box). */
    private val genericCalibers = listOf(Caliber(1, 400), Caliber(2, 250), Caliber(3, 150))

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
        return lots.find { it.orderId == id } ?: serverLot(id)
    }

    /** A server order ("FX-12") as a lot: weight and product from the order, measurements empty. */
    private fun serverLot(orderId: String): InspectionLot? {
        val order = FakeOrderRepository.instance.orderById(orderId)?.takeIf { it.remoteId != null } ?: return null
        val product = order.items.firstOrNull()?.productName.orEmpty()
        val kg = order.totalKg
        val name = product.lowercase(Locale.ROOT)
        return InspectionLot(
            orderId = orderId,
            lotCode = order.batchId?.let { "LOT-$it" } ?: "—",
            clientName = order.clientName,
            productName = product,
            netWeightKg = kg,
            crates = ceil(kg / CRATE_KG.toDouble()).toInt(),
            crateKg = CRATE_KG,
            previousRejectionReason = order.qualityRejectionReason
                ?: order.rejectionReason.takeIf { order.qualityStatus == QualityStatus.REJECTED },
            initialCoreTempC = 0.0,
            initialMoisturePct = 0.0,
            initialDefectsPct = 0.0,
            initialVisualScore = 3,
            calibers = when {
                "mango" in name -> mangoCalibers
                "palta" in name || "avocado" in name -> avocadoCalibers
                else -> genericCalibers
            },
            initialCalibers = emptySet(),
            fromServer = true,
            remoteOrderId = order.remoteId,
            batchId = order.batchId
        )
    }

    override fun findByLotCode(code: String): InspectionLot? {
        val clean = code.uppercase().replace("LOT", "").replace("#", "").trim().removePrefix("-")
        return lots.find { it.lotCode.equals(clean, ignoreCase = true) }
            ?: FakeOrderRepository.instance.orderByBatch(clean.toIntOrNull())?.let { serverLot(it.id) }
    }

    override fun draftFor(orderId: String): InspectionDraft? = drafts[orderId.removePrefix("#")]

    override fun saveDraft(draft: InspectionDraft) {
        drafts[draft.orderId] = draft
    }

    override suspend fun sendReport(orderId: String, draft: InspectionDraft, inspectorName: String): InspectionSendResult {
        val lot = lotForOrder(orderId)
        val remoteOrderId = lot?.remoteOrderId ?: return InspectionSendResult(ok = true)
        return withContext(Dispatchers.IO) {
            // 1. The lot: the one already linked, or a new one for this order
            val batchId = lot.batchId ?: run {
                val producerId = FakeProfileRepository.instance.producerId.value ?: 0
                val created = apiCall {
                    RetrofitClient.api.createHarvestBatch(
                        CreateHarvestBatchRequestDto(
                            // The server takes the producer from the account; the body value is ignored for a Producer
                            producerId = producerId,
                            fruitType = lot.productName,
                            quantityKg = lot.netWeightKg.toDouble(),
                            harvestDate = LocalDate.now().toString()
                        )
                    )
                }
                val batch = (created as? ApiResult.Ok)?.value
                    ?: return@withContext InspectionSendResult(false, (created as ApiResult.Failed).failure.serverMessage)
                val linked = apiCall { RetrofitClient.api.assignOrderBatch(remoteOrderId, AssignOrderBatchRequestDto(batch.id)) }
                if (linked is ApiResult.Failed) return@withContext InspectionSendResult(false, linked.failure.serverMessage)
                batch.id
            }
            // 2. The full inspection
            val inspection = apiCall { RetrofitClient.api.createQualityInspection(toRequest(batchId, draft, inspectorName)) }
            if (inspection is ApiResult.Failed) return@withContext InspectionSendResult(false, inspection.failure.serverMessage)
            // 3. The lot waits for the distributor's decision
            apiCall { RetrofitClient.api.updateHarvestBatchStatus(batchId, UpdateHarvestBatchStatusRequestDto("Inspected")) }
            InspectionSendResult(ok = true)
        }
    }

    private fun toRequest(batchId: Int, draft: InspectionDraft, inspectorName: String): QualityInspectionRequestDto {
        val calibers = draft.calibers.sorted().joinToString(", ")
        val notes = listOfNotNull(
            draft.notes.trim().takeIf { it.isNotBlank() },
            calibers.takeIf { it.isNotBlank() }?.let { "Calibers: $it" }
        ).joinToString(" · ")
        return QualityInspectionRequestDto(
            batchId = batchId,
            notes = notes.ifBlank { null },
            appearanceRating = draft.visualScore.coerceIn(1, 5),
            hasStains = draft.hasStains,
            hasBruises = draft.hasBruises,
            hasDeformations = draft.hasDeformations,
            hasRot = draft.hasRot,
            wastePercentage = InspectionRules.parse(draft.defects) ?: 0.0,
            temperatureCelsius = InspectionRules.parse(draft.coreTemp) ?: 0.0,
            humidityPercent = InspectionRules.parse(draft.moisture) ?: 0.0,
            ph = InspectionRules.parse(draft.ph) ?: 0.0,
            brixDegrees = InspectionRules.parse(draft.brix) ?: 0.0,
            dryCleaningDone = draft.dryCleaningDone,
            caliberSortingConfirmed = draft.calibers.isNotEmpty(),
            packagingMaterialInspected = draft.packagingInspected,
            finalBoxSealing = draft.boxesSealed,
            inspectorName = inspectorName.ifBlank { null }
        )
    }

    companion object {
        private const val CRATE_KG = 20

        val instance: InspectionRepository by lazy { FakeInspectionRepository() }
    }
}
