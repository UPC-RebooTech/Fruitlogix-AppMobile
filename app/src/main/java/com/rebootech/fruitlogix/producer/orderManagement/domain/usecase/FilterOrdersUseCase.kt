package com.rebootech.fruitlogix.producer.orderManagement.domain.usecase

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderFilters

class FilterOrdersUseCase {
    operator fun invoke(orders: List<Order>, filters: OrderFilters): List<Order> {
        return orders.filter { order ->
            val matchesQuery = if (filters.query.isBlank()) {
                true
            } else {
                val q = filters.query.trim().lowercase()
                order.id.lowercase().contains(q) ||
                order.clientName.lowercase().contains(q) ||
                order.items.any { it.productName.lowercase().contains(q) }
            }

            val matchesStatuses = if (filters.statuses.isEmpty()) {
                true
            } else {
                order.status in filters.statuses
            }

            val matchesDateRange = if (filters.dateRange != null) {
                !order.dueDate.isBefore(filters.dateRange.start) && !order.dueDate.isAfter(filters.dateRange.end)
            } else {
                true
            }

            val matchesClients = if (filters.clientIds.isEmpty()) {
                true
            } else {
                order.clientName in filters.clientIds
            }

            matchesQuery && matchesStatuses && matchesDateRange && matchesClients
        }
    }
}