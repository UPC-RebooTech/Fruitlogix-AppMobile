package com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot

/** Outcome of sending an inspection; [serverMessage] is the server's own text when it refused. */
data class InspectionSendResult(val ok: Boolean, val serverMessage: String? = null)

interface InspectionRepository {
    fun lotForOrder(orderId: String): InspectionLot?

    /** Resolves a scanned or typed lot code (with or without "LOT #" prefix) to its lot. */
    fun findByLotCode(code: String): InspectionLot?

    fun draftFor(orderId: String): InspectionDraft?

    /** Saves the draft on the phone (works offline). */
    fun saveDraft(draft: InspectionDraft)

    /**
     * Sends the inspection of a server order: creates and links its harvest lot when it has none
     * (POST /harvest-batches + PATCH /orders/{id}/batch), registers the full inspection
     * (POST /quality-inspections) and marks the lot as inspected. Lots without a server order are not sent.
     */
    suspend fun sendReport(orderId: String, draft: InspectionDraft, inspectorName: String): InspectionSendResult =
        InspectionSendResult(ok = true)
}
