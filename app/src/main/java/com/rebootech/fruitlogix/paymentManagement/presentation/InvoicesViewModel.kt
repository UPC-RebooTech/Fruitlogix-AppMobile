package com.rebootech.fruitlogix.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.paymentManagement.data.FakeBillingRepository
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
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
        val invoices = repository.getInvoices()

        _uiState.value = InvoicesUiState(
            isLoading = false,
            allInvoices = invoices,
            invoices = invoices
        )
    }

    fun onDateFilterChange(value: String) {
        _uiState.value = _uiState.value.copy(
            dateFilter = value
        )

        applyFilters()
    }

    fun onStatusFilterChange(
        status: InvoiceStatus?
    ) {
        _uiState.value = _uiState.value.copy(
            statusFilter = status
        )

        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            dateFilter = "",
            statusFilter = null,
            invoices = _uiState.value.allInvoices
        )
    }

    private fun applyFilters() {
        val state = _uiState.value

        val filtered = state.allInvoices.filter { invoice ->

            val matchesDate =
                state.dateFilter.isBlank() ||
                        invoice.dateLabel.contains(
                            state.dateFilter,
                            ignoreCase = true
                        )

            val matchesStatus =
                state.statusFilter == null ||
                        invoice.status == state.statusFilter

            matchesDate && matchesStatus
        }

        _uiState.value = state.copy(
            invoices = filtered
        )
    }
}