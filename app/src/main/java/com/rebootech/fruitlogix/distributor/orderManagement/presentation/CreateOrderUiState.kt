package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.RegisteredOrder

data class CreateOrderUiState(
    val productName: String = "",
    val quantity: String = "",
    val requiredDate: String = "",

    val productError: Boolean = false,
    val quantityError: Boolean = false,
    val requiredDateError: Boolean = false,

    val registeredOrder: RegisteredOrder? = null
) {
    val isSuccess: Boolean
        get() = registeredOrder != null
}