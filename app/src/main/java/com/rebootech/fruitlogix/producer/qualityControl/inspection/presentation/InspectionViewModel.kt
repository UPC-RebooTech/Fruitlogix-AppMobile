package com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.FakeInspectionRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class InspectionUiState(
    val lot: InspectionLot? = null,
    val draft: InspectionDraft? = null,
    val savedOffline: Boolean = false
) {
    val coreTempOk get() = draft?.let { InspectionRules.coreTempOk(it.coreTemp) } ?: false
    val moistureOk get() = draft?.let { InspectionRules.moistureOk(it.moisture) } ?: false
    val defectsOk get() = draft?.let { InspectionRules.defectsOk(it.defects) } ?: false
    val calibersOk get() = draft?.calibers?.isNotEmpty() ?: false
    val issues get() = listOf(coreTempOk, moistureOk, defectsOk, calibersOk).count { !it }
    val canSubmit get() = draft?.let { InspectionRules.canSubmit(it) } ?: false
}

/** Inspection form for one order. Reads "orderId" from the navigation arguments. */
class InspectionViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = FakeInspectionRepository.instance
    private val orderId = savedStateHandle.get<String>("orderId").orEmpty().removePrefix("#")

    private val _uiState = MutableStateFlow(InspectionUiState())
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    init {
        val lot = repository.lotForOrder(orderId)
        val draft = repository.draftFor(orderId) ?: lot?.let {
            InspectionDraft(
                orderId = it.orderId,
                visualScore = it.initialVisualScore,
                calibers = it.initialCalibers,
                coreTemp = String.format(Locale.US, "%.1f", it.initialCoreTempC),
                moisture = String.format(Locale.US, "%.1f", it.initialMoisturePct),
                defects = String.format(Locale.US, "%.1f", it.initialDefectsPct),
                notes = ""
            )
        }
        _uiState.value = InspectionUiState(lot = lot, draft = draft)
    }

    fun update(transform: (InspectionDraft) -> InspectionDraft) {
        _uiState.update { state ->
            state.copy(draft = state.draft?.let(transform), savedOffline = false)
        }
    }

    fun toggleCaliber(code: Int) = update { draft ->
        draft.copy(calibers = if (code in draft.calibers) draft.calibers - code else draft.calibers + code)
    }

    fun saveOffline() {
        _uiState.value.draft?.let { repository.saveDraft(it) }
        _uiState.update { it.copy(savedOffline = true) }
    }

    /** Saves the draft so the report step can read it. */
    fun submit(): Boolean {
        val state = _uiState.value
        val draft = state.draft ?: return false
        if (!state.canSubmit) return false
        repository.saveDraft(draft)
        return true
    }
}
