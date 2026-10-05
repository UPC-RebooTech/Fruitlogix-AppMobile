package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditOrderViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: OrderRepository =
        FakeOrderRepository()

    private val orderId: String =
        savedStateHandle["orderId"] ?: ""

    private val _uiState =
        MutableStateFlow(EditOrderUiState())

    val uiState: StateFlow<EditOrderUiState> =
        _uiState.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        val order = repository.getOrderById(orderId)

        if (order == null) {
            _uiState.value = EditOrderUiState(
                orderId = orderId,
                isLoading = false,
                editBlocked = true
            )
            return
        }

        val quantity = order.quantityLabel
            .substringAfter("•")
            .removeSuffix("t")
            .trim()

        _uiState.value = EditOrderUiState(
            orderId = order.id,
            productName = order.productName,
            quantity = quantity,
            requiredDate = order.deliveryDateLabel,
            status = order.status,
            isLoading = false,
            editBlocked = order.status != OrderStatus.PENDING
        )
    }

    fun onProductChange(value: String) {
        if (!_uiState.value.isEditable) return

        _uiState.value = _uiState.value.copy(
            productName = value,
            productError = false
        )
    }

    fun onQuantityChange(value: String) {
        if (!_uiState.value.isEditable) return

        _uiState.value = _uiState.value.copy(
            quantity = value,
            quantityError = false
        )
    }

    fun onRequiredDateChange(value: String) {
        if (!_uiState.value.isEditable) return

        _uiState.value = _uiState.value.copy(
            requiredDate = value,
            requiredDateError = false
        )
    }

    fun saveChanges() {
        val state = _uiState.value

        if (!state.isEditable) {
            _uiState.value = state.copy(
                editBlocked = true
            )
            return
        }

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

        val updated = repository.updateOrder(
            orderId = state.orderId,
            productName = state.productName.trim(),
            quantity = quantityValue!!,
            requiredDate = state.requiredDate.trim()
        )

        _uiState.value = state.copy(
            isSaved = updated,
            editBlocked = !updated
        )
    }
}