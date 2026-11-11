package com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.repository

import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot

interface InspectionRepository {
    fun lotForOrder(orderId: String): InspectionLot?

    /** Resolves a scanned or typed lot code (with or without "LOT #" prefix) to its lot. */
    fun findByLotCode(code: String): InspectionLot?

    fun draftFor(orderId: String): InspectionDraft?

    /** Saves the draft on the phone (works offline). */
    fun saveDraft(draft: InspectionDraft)
}
