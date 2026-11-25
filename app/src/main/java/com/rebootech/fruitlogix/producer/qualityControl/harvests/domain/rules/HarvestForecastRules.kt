package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.rules

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.HarvestForecast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Weekly demand of the producer, from their real orders (no sample data). */
object HarvestForecastRules {

    /**
     * Orders due from Monday to Sunday of [today]'s week, without rejected or cancelled ones.
     * The product is the one with the most kg; fulfilled = kg already dispatched or delivered.
     * null when there are no orders due this week.
     */
    fun forWeek(orders: List<Order>, today: LocalDate): HarvestForecast? {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val week = orders.filter {
            !it.dueDate.isBefore(monday) && !it.dueDate.isAfter(sunday) &&
                it.status != OrderStatus.REJECTED && it.status != OrderStatus.CANCELLED
        }
        if (week.isEmpty()) return null
        val mainProduct = week.flatMap { it.items }
            .groupBy { it.productName }
            .maxByOrNull { (_, items) -> items.sumOf { it.kg } }
            ?.key.orEmpty()
        return HarvestForecast(
            weekStart = monday,
            productName = mainProduct,
            requestedKg = week.sumOf { it.totalKg },
            fulfilledKg = week.filter { it.status == OrderStatus.IN_TRANSIT || it.status == OrderStatus.DELIVERED }.sumOf { it.totalKg },
            clients = week.map { it.clientName }.filter { it.isNotBlank() }.distinct()
        )
    }
}
