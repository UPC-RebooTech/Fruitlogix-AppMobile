package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestPlanDraft
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class PlanField { FRUIT, DATE, YIELD, ZONE, CREW, PICKERS }

data class ScheduleHarvestUiState(
    val fruitType: String = "",
    val date: LocalDate = LocalDate.now().plusDays(1),
    val time: LocalTime = LocalTime.of(6, 0),
    val yieldKg: String = "",
    val zone: String = "",
    val crewName: String = "",
    val pickers: String = "",
    val notes: String = "",
    val errors: Set<PlanField> = emptySet(),
    val isSaving: Boolean = false,
    /** The server refused it: its own text (blank = generic message). */
    val serverError: String? = null,
    val saved: Boolean = false
)

/** Plans a harvest on the server (POST /harvest-plans); the distributor sees it too. */
class ScheduleHarvestViewModel(
    private val repository: HarvestRepository = FakeHarvestRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleHarvestUiState())
    val uiState: StateFlow<ScheduleHarvestUiState> = _uiState.asStateFlow()

    fun onChange(field: PlanField?, transform: (ScheduleHarvestUiState) -> ScheduleHarvestUiState) = _uiState.update {
        transform(it).copy(errors = if (field != null) it.errors - field else it.errors, serverError = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val yieldKg = state.yieldKg.trim().replace(',', '.').toDoubleOrNull()
        val pickers = state.pickers.trim().toIntOrNull()
        val at = LocalDateTime.of(state.date, state.time)
        val errors = buildSet {
            if (state.fruitType.isBlank()) add(PlanField.FRUIT)
            if (at.isBefore(LocalDateTime.now())) add(PlanField.DATE)
            if (yieldKg == null || yieldKg <= 0.0) add(PlanField.YIELD)
            if (state.zone.isBlank()) add(PlanField.ZONE)
            if (state.crewName.isBlank()) add(PlanField.CREW)
            if (pickers == null || pickers < 1) add(PlanField.PICKERS)
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, serverError = null) }
            val result = repository.schedule(
                HarvestPlanDraft(
                    fruitType = state.fruitType,
                    scheduledFor = at,
                    estimatedYieldKg = yieldKg!!,
                    zone = state.zone,
                    crewName = state.crewName,
                    pickers = pickers!!,
                    notes = state.notes
                )
            )
            _uiState.update {
                it.copy(isSaving = false, saved = result.ok, serverError = if (result.ok) null else result.serverMessage ?: "")
            }
        }
    }
}
