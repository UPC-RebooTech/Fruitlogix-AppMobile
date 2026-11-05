package com.rebootech.fruitlogix.producer.orderManagement.domain.model

import java.time.LocalDate

data class OrderDateRange(
    val start: LocalDate,
    val end: LocalDate
)

data class OrderFilters(
    val query: String = "",
    val statuses: Set<OrderStatus> = emptySet(),
    val dateRange: OrderDateRange? = null,
    val clientIds: Set<String> = emptySet()
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
                statuses.isNotEmpty() ||
                dateRange != null ||
                clientIds.isNotEmpty()
}