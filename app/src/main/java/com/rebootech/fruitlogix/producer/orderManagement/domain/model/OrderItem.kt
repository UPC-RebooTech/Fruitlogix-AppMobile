package com.rebootech.fruitlogix.producer.orderManagement.domain.model

/**
 * @param specification Packing/grade requested by the distributor (data written by them,
 *   e.g. "Size 8-10 · 4 kg boxes").
 */
data class OrderItem(
    val productId: String,
    val productName: String,
    val kg: Int,
    val specification: String? = null
)
