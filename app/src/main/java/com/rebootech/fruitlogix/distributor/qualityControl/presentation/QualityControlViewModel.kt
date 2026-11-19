package com.rebootech.fruitlogix.distributor.qualityControl.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.QualityLot
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionKind
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionReason
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageSummary
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.FieldInspectionRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.RejectionRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.ShrinkageRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.VoiceIncidentRules
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class QualityTab { LOTS, SHRINKAGE }

/** Open sheet: reject a lot (US10), field inspection (US53) or voice incident (US57). */
sealed interface QualitySheet {
    data class Reject(val lot: QualityLot) : QualitySheet
    data object FieldInspection : QualitySheet
    data object VoiceIncident : QualitySheet
}

data class QualityUiState(
    val lots: List<QualityLot> = emptyList(),
    val thresholds: List<FruitThreshold> = emptyList(),
    val tab: QualityTab = QualityTab.LOTS,
    val status: LotStatus? = null,
    val months: List<YearMonth> = emptyList(),
    val month: YearMonth = YearMonth.now(),
    val shrinkage: ShrinkageSummary = ShrinkageSummary(0, 0.0, emptyList()),
    val sheet: QualitySheet? = null,
    @field:StringRes val sheetError: Int? = null,
    /** Field-inspection errors, one per field. */
    val fieldErrors: Set<FieldInspectionRules.Error> = emptySet(),
    val message: UiText? = null
) {
    /** Pending first (they need a decision), newest first. */
    val visibleLots: List<QualityLot>
        get() = lots.filter { status == null || it.status == status }
            .sortedWith(compareBy<QualityLot> { it.status != LotStatus.PENDING }.thenByDescending { it.recordedAt })

    fun count(value: LotStatus) = lots.count { it.status == value }

    fun thresholdFor(product: String): FruitThreshold? =
        thresholds.firstOrNull { it.fruitName.equals(product, ignoreCase = true) }
}

/**
 * Quality control: lot decisions, offline field inspections, voice incidents and shrinkage.
 * Fruit limits come from IoT through its domain interface (same ranges as the Fleet).
 */
class QualityControlViewModel(
    private val repository: QualityRepository,
    private val iot: IotRepository
) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.qualityRepository, DistributorDependencies.iotRepository)

    private val local = MutableStateFlow(
        QualityUiState(
            months = repository.shrinkageMonths(),
            month = repository.shrinkageMonths().firstOrNull() ?: YearMonth.now(),
            shrinkage = ShrinkageRules.summary(repository.shrinkage, repository.shrinkageMonths().firstOrNull() ?: YearMonth.now())
        )
    )

    val uiState: StateFlow<QualityUiState> =
        combine(repository.lots, iot.thresholds, local) { lots, thresholds, local ->
            local.copy(lots = lots, thresholds = thresholds)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), local.value)
    init { viewModelScope.launch { repository.refresh() } }
    fun onTabChange(value: QualityTab) = local.update { it.copy(tab = value) }
    fun onStatusChange(value: LotStatus?) = local.update { it.copy(status = value) }
    fun onMonthChange(value: YearMonth) =
        local.update { it.copy(month = value, shrinkage = ShrinkageRules.summary(repository.shrinkage, value)) }

    fun openSheet(sheet: QualitySheet) = local.update { it.copy(sheet = sheet, sheetError = null, fieldErrors = emptySet()) }
    fun closeSheet() = local.update { it.copy(sheet = null, sheetError = null, fieldErrors = emptySet()) }
    fun onMessageShown() = local.update { it.copy(message = null) }

    private fun done(message: UiText) =
        local.update { it.copy(sheet = null, sheetError = null, fieldErrors = emptySet(), message = message) }

    private fun parse(text: String) = text.trim().replace(',', '.').toDoubleOrNull()

    fun approve(lot: QualityLot) {
        viewModelScope.launch {
            repository.approve(lot.id)
            local.update { it.copy(message = UiText.StringResource(R.string.qc_approved_msg, arrayOf(lot.lotCode))) }
        }
    }

    // US10
    fun reject(lot: QualityLot, kind: RejectionKind?, note: String) {
        when (RejectionRules.validate(kind, note)) {
            RejectionRules.Error.REASON_REQUIRED -> local.update { it.copy(sheetError = R.string.qc_error_reason) }
            RejectionRules.Error.NOTE_REQUIRED -> local.update { it.copy(sheetError = R.string.qc_error_note) }
            null -> viewModelScope.launch {
                repository.reject(lot.id, RejectionReason(kind!!, note))
                done(UiText.StringResource(R.string.qc_rejected_msg, arrayOf(lot.lotCode)))
            }
        }
    }

    // US53
    fun saveFieldInspection(lotCode: String, product: String, tempText: String, humidityText: String) {
        val temp = parse(tempText)
        val humidity = parse(humidityText)
        val errors = FieldInspectionRules.validate(lotCode, product, temp, humidity)
        if (errors.isNotEmpty()) {
            local.update { it.copy(fieldErrors = errors) }
            return
        }
        viewModelScope.launch {
            repository.addFieldInspection(lotCode, product, temp!!, humidity!!)
            local.update { it.copy(tab = QualityTab.LOTS, status = null) }
            done(UiText.StringResource(R.string.qc_field_saved))
        }
    }

    // US57
    fun saveVoiceIncident(lotCode: String, description: String, recordedSeconds: Int?) {
        when (VoiceIncidentRules.validate(lotCode, description, recordedSeconds)) {
            VoiceIncidentRules.Error.LOT_REQUIRED -> local.update { it.copy(sheetError = R.string.qc_error_lot) }
            VoiceIncidentRules.Error.DESCRIPTION_REQUIRED -> local.update { it.copy(sheetError = R.string.qc_error_description) }
            VoiceIncidentRules.Error.RECORDING_TOO_SHORT -> local.update { it.copy(sheetError = R.string.qc_error_recording) }
            null -> viewModelScope.launch {
                repository.addVoiceIncident(lotCode, description, recordedSeconds!!)
                local.update { it.copy(tab = QualityTab.LOTS, status = null) }
                done(UiText.StringResource(R.string.qc_voice_saved))
            }
        }
    }
}
