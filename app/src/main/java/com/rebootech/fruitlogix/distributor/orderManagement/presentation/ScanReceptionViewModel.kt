package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanReceptionUiState(
    val manualCode: String = "",
    val showManualEntry: Boolean = false,
    val result: LotScanResult? = null,
    val isConfirming: Boolean = false,
    /** Order id whose reception was just confirmed. */
    val receivedOrderId: String? = null
)

/** Gate reception: read the lot label → check it against an order on route → confirm. */
class ScanReceptionViewModel : ViewModel() {

    private val repository: OrderRepository = com.rebootech.fruitlogix.distributor.DistributorDependencies.orderRepository

    private val _uiState = MutableStateFlow(ScanReceptionUiState())
    val uiState: StateFlow<ScanReceptionUiState> = _uiState.asStateFlow()

    // TODO(scanner): replace the demo buttons with a barcode analyzer on the camera preview.
    fun simulateValidScan() = check(FakeOrderRepository.DEMO_VALID_CODE)
    fun simulateDamagedScan() = check(FakeOrderRepository.DEMO_INVALID_CODE)

    fun onManualCodeChange(value: String) =
        _uiState.update { it.copy(manualCode = value.uppercase().replace(" ", "")) }

    fun showManualEntry() = _uiState.update { it.copy(showManualEntry = true) }

    fun checkManualCode() {
        val code = _uiState.value.manualCode
        if (code.isNotBlank()) check(code)
    }

    private fun check(code: String) {
        val result = repository.validateLotCode(code)
        _uiState.update {
            it.copy(
                result = result,
                receivedOrderId = null,
                // An unreadable label: offer typing the code right away
                showManualEntry = it.showManualEntry || result is LotScanResult.NotRecognized
            )
        }
    }

    fun confirmReception() {
        val matched = _uiState.value.result as? LotScanResult.Matched ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isConfirming = true) }
            val ok = repository.confirmReception(matched.order.id)
            _uiState.update {
                it.copy(isConfirming = false, receivedOrderId = if (ok) matched.order.id else null, result = if (ok) null else it.result)
            }
        }
    }

    fun reset() {
        _uiState.value = ScanReceptionUiState()
    }
}
