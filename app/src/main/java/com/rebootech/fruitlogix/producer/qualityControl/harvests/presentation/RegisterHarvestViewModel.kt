package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestError
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestField
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestInput
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.validation.HarvestValidator
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** Form state; errors are enums mapped to localized messages by the screen. */
data class RegisterHarvestUiState(
    val input: HarvestInput,
    val notes: String = "",
    val productSuggestions: List<String> = emptyList(),
    val sectors: List<String> = emptyList(),
    val harvestedAt: LocalDateTime = LocalDateTime.now(),
    val errors: Map<HarvestField, HarvestError> = emptyMap(),
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class RegisterHarvestViewModel(
    private val repository: HarvestRepository = FakeHarvestRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegisterHarvestUiState(
            input = HarvestInput(
                productName = "",
                lotCode = "",
                weightKg = "",
                brix = "",
                sector = repository.storageSectors.firstOrNull().orEmpty()
            ),
            // Products the producer already has in stock, as one-tap choices
            productSuggestions = StockRepository.stockItems.map { it.productName }.distinct(),
            sectors = repository.storageSectors
        )
    )
    val uiState: StateFlow<RegisterHarvestUiState> = _uiState.asStateFlow()

    fun onInputChange(field: HarvestField, transform: (HarvestInput) -> HarvestInput) {
        _uiState.update { it.copy(input = transform(it.input), errors = it.errors - field) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    /** Suggests a lot code from the product and today's date, e.g. "L-KEN-1111". */
    fun suggestLotCode() {
        val product = _uiState.value.input.productName
        val letters = product.filter { it.isLetter() }.take(3).uppercase().ifEmpty { "LOT" }
        val code = "L-$letters-${LocalDate.now().format(DateTimeFormatter.ofPattern("ddMM"))}"
        onInputChange(HarvestField.LOT_CODE) { it.copy(lotCode = code) }
    }

    fun onSave() {
        val state = _uiState.value
        val errors = HarvestValidator.validate(state.input)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.addHarvest(
                fruitName = state.input.productName.trim(),
                lotCode = state.input.lotCode.trim().uppercase(),
                weightKg = HarvestValidator.parseDecimal(state.input.weightKg)?.roundToInt() ?: 0,
                harvestedAt = LocalDateTime.now(),
                sector = state.input.sector,
                brix = HarvestValidator.parseDecimal(state.input.brix),
                notes = state.notes.trim().ifEmpty { null }
            )
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
