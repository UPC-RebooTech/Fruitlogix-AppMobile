package com.rebootech.fruitlogix.qualityControl

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderItem
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.rules.HarvestForecastRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** The producer's weekly forecast comes from their real orders. */
class HarvestForecastRulesTest {

    private val today = LocalDate.of(2026, 11, 25) // Wednesday; week from 23 to 29

    private fun order(id: String, product: String, kg: Int, due: LocalDate, status: OrderStatus, client: String = "#12") =
        Order(id, client, "", listOf(OrderItem(id, product, kg)), due, status)

    @Test
    fun ordersOfTheWeek_giveRequestedAndFulfilledKg() {
        val orders = listOf(
            order("FX-1", "Palta Hass", 400, LocalDate.of(2026, 11, 23), OrderStatus.DELIVERED),
            order("FX-2", "Palta Hass", 300, LocalDate.of(2026, 11, 29), OrderStatus.PENDING, client = "#15"),
            order("FX-3", "Mango Kent", 200, LocalDate.of(2026, 11, 26), OrderStatus.IN_TRANSIT),
            order("FX-4", "Mango Kent", 900, LocalDate.of(2026, 11, 27), OrderStatus.REJECTED),
            order("FX-5", "Mango Kent", 900, LocalDate.of(2026, 11, 30), OrderStatus.PENDING)
        )
        val forecast = HarvestForecastRules.forWeek(orders, today)!!
        assertEquals(LocalDate.of(2026, 11, 23), forecast.weekStart)
        assertEquals("Palta Hass", forecast.productName)
        assertEquals(900, forecast.requestedKg)
        assertEquals(600, forecast.fulfilledKg)
        assertEquals(listOf("#12", "#15"), forecast.clients)
    }

    @Test
    fun noOrdersThisWeek_meansNoForecast() {
        assertNull(HarvestForecastRules.forWeek(emptyList(), today))
    }
}
