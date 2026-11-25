package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

/** An account that receives orders. [id] is the server's commercialClientId (the client's user id). */
data class CommercialClient(val id: Int, val name: String, val deliveryAddress: String = "")

/**
 * A product that can be ordered. [id] is the server's productId; prices are per kg (soles).
 * Catalog products (/products) belong to one producer and have a minimum order and stock.
 */
data class CatalogProduct(
    val id: Int,
    val name: String,
    val pricePerKg: Double,
    val producerId: Int? = null,
    val producerName: String = "",
    val minOrderKg: Double = 0.0,
    /** null when the stock is not known (offline list). */
    val availableKg: Double? = null
)

