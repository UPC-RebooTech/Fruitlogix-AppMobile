package com.rebootech.fruitlogix.paymentManagement

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.PurchaseRules
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.PurchaseRules.Line
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** US68 / US69 calculated from server order lines. */
class PurchaseRulesTest {

    private val today = LocalDate.of(2026, 11, 25) // Wednesday

    private val lines = listOf(
        Line(1, "cliente1", "Palta Hass", 100.0, 9.0, 900.0, LocalDate.of(2026, 10, 3)),
        Line(2, "cliente1", "Palta Hass", 300.0, 10.0, 3000.0, LocalDate.of(2026, 10, 20)),
        Line(3, "cliente1", "Palta Hass", 200.0, 11.0, 2200.0, LocalDate.of(2026, 11, 24)),
        Line(3, "cliente1", "Mango Kent", 50.0, 7.5, 375.0, LocalDate.of(2026, 11, 24)),
        Line(4, "cliente2", "Palta Hass", 80.0, 12.0, 960.0, LocalDate.of(2026, 11, 2))
    )

    @Test
    fun priceHistory_isWeightedByKgPerMonth() {
        val history = PurchaseRules.priceHistory(lines, "Palta Hass", "cliente1")
        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11)), history.map { it.month })
        // October: (100 × 9 + 300 × 10) / 400 = 9.75
        assertEquals(9.75, history[0].pricePerKg, 0.001)
        assertEquals(11.0, history[1].pricePerKg, 0.001)
    }

    @Test
    fun weekSummary_startsOnMonday_andCountsOrdersOnce() {
        val week = PurchaseRules.summary(lines, "cliente1", SummaryPeriod.WEEK, today)!!
        assertEquals(250, week.kilos)
        assertEquals(2575.0, week.amount, 0.001)
        assertEquals(1, week.orders)
    }

    @Test
    fun monthSummary_andClientWithoutOrders() {
        val month = PurchaseRules.summary(lines, "cliente2", SummaryPeriod.MONTH, today)!!
        assertEquals(80, month.kilos)
        assertNull(PurchaseRules.summary(lines, "cliente2", SummaryPeriod.WEEK, today))
        assertNull(PurchaseRules.summary(lines, "nadie", SummaryPeriod.MONTH, today))
    }
}
