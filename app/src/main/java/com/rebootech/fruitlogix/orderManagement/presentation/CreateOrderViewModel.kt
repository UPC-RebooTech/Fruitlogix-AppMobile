package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderRegistration
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateOrderViewModel(
    private val repository: OrderRepository = FakeOrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateOrderUiState())

    val uiState: StateFlow<CreateOrderUiState> =
        _uiState.asStateFlow()

    fun onProductChange(value: String) {
        _uiState.value = _uiState.value.copy(
            productName = value,
            productError = false
        )
    }

    fun onQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(
            quantity = value,
            quantityError = false
        )
    }

    fun onRequiredDateChange(value: String) {
        _uiState.value = _uiState.value.copy(
            requiredDate = value,
            requiredDateError = false
        )
    }

    fun registerOrder() {
        val state = _uiState.value

        val quantityValue = state.quantity
            .replace(",", ".")
            .toDoubleOrNull()

        val productError =
            state.productName.isBlank()

        val quantityError =
            quantityValue == null || quantityValue <= 0

        val requiredDateError =
            state.requiredDate.isBlank()

        if (
            productError ||
            quantityError ||
            requiredDateError
        ) {
            _uiState.value = state.copy(
                productError = productError,
                quantityError = quantityError,
                requiredDateError = requiredDateError
            )
            return
        }

        val registeredOrder = repository.createOrder(
            OrderRegistration(
                productName = state.productName.trim(),
                quantity = quantityValue!!,
                requiredDate = state.requiredDate.trim()
            )
        )

        _uiState.value = state.copy(
            registeredOrder = registeredOrder,
            productError = false,
            quantityError = false,
            requiredDateError = false
        )
    }

    fun resetForm() {
        _uiState.value = CreateOrderUiState()
    }
}