package com.rebootech.fruitlogix.distributor.qualityControl.domain.repository

import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionReason
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import kotlinx.coroutines.flow.StateFlow
import java.time.YearMonth

interface QualityRepository {
    val lots: StateFlow<List<QualityLot>>
    val shrinkage: List<ShrinkageRecord>

    /** Months offered in the shrinkage report, newest first (some may have no records). */
    fun shrinkageMonths(): List<YearMonth>

    suspend fun approve(lotId: String)
    suspend fun reject(lotId: String, reason: RejectionReason)

    /** Saved on the phone and marked "pending sync" until there is signal (US53). */
    suspend fun addFieldInspection(lotCode: String, productName: String, celsius: Double, humidityPercent: Double)

    suspend fun addVoiceIncident(lotCode: String, description: String, voiceNoteSeconds: Int)

    suspend fun refresh() {}
}
