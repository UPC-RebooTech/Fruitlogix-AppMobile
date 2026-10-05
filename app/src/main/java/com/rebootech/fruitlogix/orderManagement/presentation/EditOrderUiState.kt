package com.rebootech.fruitlogix.orderManagement.presentation

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus

data class EditOrderUiState(
    val orderId: String = "",
    val productName: String = "",
    val quantity: String = "",
    val requiredDate: String = "",
    val status: OrderStatus? = null,

    val productError: Boolean = false,
    val quantityError: Boolean = false,
    val requiredDateError: Boolean = false,

    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val editBlocked: Boolean = false
) {
    val isEditable: Boolean
        get() = status == OrderStatus.PENDING
}