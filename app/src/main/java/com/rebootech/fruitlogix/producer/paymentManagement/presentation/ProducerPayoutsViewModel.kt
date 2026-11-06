package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.paymentManagement.data.FakePaymentRepository
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProducerPayoutsViewModel(
    private val repository: PaymentRepository = FakePaymentRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProducerPayoutsUiState(isLoading = true))
    val uiState: StateFlow<ProducerPayoutsUiState> = _uiState.asStateFlow()

    private var rawItems: List<PayoutItem> = emptyList()

    init {
        loadPayouts()
    }

    private fun loadPayouts() {
        viewModelScope.launch {
            // Se usa el metodo reactivo oficial de la interfaz
            repository.observePaymentSummary().collect { summary ->
                val mockPayouts = listOf(
                    PayoutItem("INV-101", "Liquidación Lote Arándano Ventura", "12 Oct 2026", 4250.00, false),
                    PayoutItem("INV-102", "Liquidación Lote Palta Hass #44", "24 Oct 2026", 8900.50, true),
                    PayoutItem("INV-103", "Cosecha Mango Kent Lote 12", "02 Nov 2026", 3120.00, false),
                    PayoutItem("INV-104", "Adelanto Cosecha Mandarina W. Murcott", "05 Nov 2026", 1850.00, true)
                )

                rawItems = mockPayouts

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        totalPendingAmount = summary.pendingAmount,
                        totalPaidThisMonth = summary.paidAmount,
                        payouts = applyFilter(rawItems, current.selectedFilter)
                    )
                }
            }
        }
    }

    fun onSelectFilter(filter: PayoutFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                payouts = applyFilter(rawItems, filter)
            )
        }
    }

    private fun applyFilter(list: List<PayoutItem>, filter: PayoutFilter): List<PayoutItem> {
        return when (filter) {
            PayoutFilter.ALL -> list
            PayoutFilter.PENDING -> list.filter { it.isPending }
            PayoutFilter.COMPLETED -> list.filter { !it.isPending }
        }
    }
}