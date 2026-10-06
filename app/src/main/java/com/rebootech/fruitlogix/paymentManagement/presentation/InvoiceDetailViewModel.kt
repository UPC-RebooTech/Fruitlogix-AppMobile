package com.rebootech.fruitlogix.paymentManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.paymentManagement.data.FakeBillingRepository
import com.rebootech.fruitlogix.paymentManagement.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvoiceDetailViewModel : ViewModel() {

    private val repository: BillingRepository =
        FakeBillingRepository()

    private val _uiState =
        MutableStateFlow(InvoiceDetailUiState())

    val uiState: StateFlow<InvoiceDetailUiState> =
        _uiState.asStateFlow()

    fun loadInvoice(invoiceId: String) {

        _uiState.value = InvoiceDetailUiState(
            isLoading = true
        )

        val invoice =
            repository.getInvoiceById(invoiceId)

        _uiState.value =
            if (invoice != null) {
                InvoiceDetailUiState(
                    isLoading = false,
                    invoice = invoice,
                    isNotFound = false
                )
            } else {
                InvoiceDetailUiState(
                    isLoading = false,
                    invoice = null,
                    isNotFound = true
                )
            }
    }
}