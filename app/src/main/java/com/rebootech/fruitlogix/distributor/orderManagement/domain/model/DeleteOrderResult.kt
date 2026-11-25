package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

enum class DeleteOrderResult {
    SUCCESS,
    WINDOW_EXPIRED,
    NOT_FOUND,
    /** It already has a delivery, invoice or payment (409): it can only be cancelled. */
    HAS_DEPENDENCIES
}