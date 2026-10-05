package com.rebootech.fruitlogix.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.paymentManagement.data.FakeBillingRepository
import com.rebootech.fruitlogix.paymentManagement.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvoicesViewModel : ViewModel() {

    private val repository: BillingRepository =
        FakeBillingRepository()

    private val _uiState =
        MutableStateFlow(InvoicesUiState())

    val uiState: StateFlow<InvoicesUiState> =
        _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        _uiState.value = InvoicesUiState(
            isLoading = false,
            invoices = repository.getInvoices()
        )
    }
}