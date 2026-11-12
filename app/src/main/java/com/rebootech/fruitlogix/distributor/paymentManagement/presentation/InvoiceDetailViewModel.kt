package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvoiceDetailViewModel(private val repository: BillingRepository) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.billingRepository)

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    fun loadInvoice(invoiceId: String) {
        _uiState.value = InvoiceDetailUiState(isLoading = true)
        val invoice = repository.getInvoiceById(invoiceId)
        _uiState.value = InvoiceDetailUiState(
            isLoading = false,
            invoice = invoice,
            isNotFound = invoice == null,
            creditNotes = repository.creditNotes.value.filter { it.invoiceId == invoiceId }
        )
    }
}
