package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.model.LotScanStatus
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanReceptionViewModel : ViewModel() {

    private val repository: OrderRepository =
        FakeOrderRepository()

    private val _uiState =
        MutableStateFlow(ScanReceptionUiState())

    val uiState: StateFlow<ScanReceptionUiState> =
        _uiState.asStateFlow()

    fun simulateSuccessfulScan() {
        validateCode("LOT-FX-1040-FRESA")
    }

    fun simulateInvalidScan() {
        validateCode("DAMAGED-CODE")
    }

    fun onManualCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            manualCode = value
        )
    }

    fun validateManualCode() {
        val code = _uiState.value.manualCode

        if (code.isBlank()) return

        validateCode(code)
    }

    fun showManualEntry() {
        _uiState.value = _uiState.value.copy(
            showManualEntry = true
        )
    }

    fun resetScan() {
        _uiState.value = ScanReceptionUiState()
    }

    private fun validateCode(code: String) {
        val result = repository.validateLotCode(code)

        _uiState.value = _uiState.value.copy(
            scanResult = result,
            showManualEntry =
                result.status == LotScanStatus.NOT_RECOGNIZED
        )
    }
}