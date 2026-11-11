package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.paymentManagement.data.FakePaymentRepository
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProducerPayoutsViewModel(
    private val repository: PaymentRepository = FakePaymentRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProducerPayoutsUiState())
    val uiState: StateFlow<ProducerPayoutsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observePaymentSummary(), repository.observePayouts()) { summary, payouts ->
                summary to payouts.sortedByDescending { it.issuedOn }
            }.collect { (summary, payouts) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalPendingAmount = summary.pendingAmount,
                        totalPaidThisMonth = summary.paidAmount,
                        overdueAmount = summary.overdueAmount,
                        allPayouts = payouts
                    )
                }
            }
        }
    }

    fun onSelectFilter(filter: PayoutFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
