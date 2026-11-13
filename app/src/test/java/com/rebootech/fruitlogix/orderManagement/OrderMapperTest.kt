package com.rebootech.fruitlogix.orderManagement

import com.rebootech.fruitlogix.distributor.orderManagement.data.OrderMapper
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.validation.OrderValidator
import com.rebootech.fruitlogix.shared.core.network.OrderItemResponseDto
import com.rebootech.fruitlogix.shared.core.network.OrderResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class OrderMapperTest {

    private val lima = ZoneId.of("America/Lima")

    /** The order created from Swagger in the test (id 4, client 534, 10 kg of "Palta"). */
    private val swaggerOrder = OrderResponseDto(
        id = 4, commercialClientId = 534, producerId = null, status = "Pending",
        deliveryDueDate = "2026-11-29", deliveryAddress = "Av. Tambo", totalAmount = 10.0, notes = "Palta",
        items = listOf(OrderItemResponseDto(id = 4, productId = 109, productName = "Palta", quantityKg = 10.0, unitPrice = 3.0, subtotal = 100.0)),
        createdAt = "2026-11-13T07:07:35.4166119+00:00"
    )

    private fun active(dto: OrderResponseDto) = (OrderMapper.map(dto, lima) as OrderMapper.Mapped.Active).order

    @Test
    fun quantity_isTheRealKg() {
        val order = active(swaggerOrder)
        assertEquals("FX-4", order.id)
        assertEquals(10.0, order.quantityKg, 0.0)
        assertEquals("Palta", order.productName)
        assertEquals(LocalDate.of(2026, 11, 29), order.deliveryDate)
    }

    @Test
    fun unknownClient_keepsItsIdAndNoName() {
        val order = active(swaggerOrder)
        assertEquals("", order.clientName)
        assertEquals(534, order.clientId)
        assertEquals("Supermercados Lima Norte", active(swaggerOrder.copy(commercialClientId = 101)).clientName)
    }

    @Test
    fun creationTime_isConvertedFromUtc() {
        assertEquals(LocalDateTime.of(2026, 11, 13, 2, 7, 35, 416_611_900), active(swaggerOrder).createdAt)
    }

    @Test
    fun serverStatuses_areTranslated() {
        assertEquals(OrderStatus.PENDING, active(swaggerOrder).status)
        assertEquals(OrderStatus.IN_PREPARATION, active(swaggerOrder.copy(status = "InPreparation")).status)
        assertEquals(OrderStatus.ON_ROUTE, active(swaggerOrder.copy(status = "InTransit")).status)
        val delivered = OrderMapper.map(swaggerOrder.copy(status = "DeliveredWithIncidence"), lima) as OrderMapper.Mapped.Finished
        assertEquals(OrderHistoryStatus.DELIVERED, delivered.item.status)
        val rejected = OrderMapper.map(swaggerOrder.copy(status = "Rejected"), lima) as OrderMapper.Mapped.Finished
        assertEquals(OrderHistoryStatus.CANCELLED, rejected.item.status)
    }

    @Test
    fun nothingIsInvented_whenDataIsMissing() {
        val empty = active(swaggerOrder.copy(items = emptyList()))
        assertEquals("", empty.productName)
        assertEquals(0.0, empty.quantityKg, 0.0)
        assertEquals(0, empty.pallets)
        assertNull(OrderMapper.map(swaggerOrder.copy(deliveryDueDate = null), lima))
        // No creation time: old enough that it can no longer be deleted
        assertFalse(active(swaggerOrder.copy(createdAt = null)).canBeDeleted())
    }

    @Test
    fun serverOrders_onlyAllowChangingTheDate() {
        assertTrue(active(swaggerOrder).onlyDateEditable)
    }

    @Test
    fun createRequest_usesTheCatalog() {
        val draft = OrderDraft(clientId = 103, productName = "Fresa", quantityKg = 11_200.0, deliveryDate = LocalDate.of(2026, 11, 20))
        val request = OrderMapper.toCreateRequest(draft)!!
        assertEquals(103, request.commercialClientId)
        assertEquals("Panamericana Sur Km 18.5, San Juan de Miraflores, Lima", request.deliveryAddress)
        assertEquals(5, request.items.single().productId)
        assertEquals(11_200.0, request.items.single().quantityKg, 0.0)
        assertEquals(76_160.0, request.totalAmount, 0.001)
        assertNull(OrderMapper.toCreateRequest(draft.copy(clientId = 534)))
        assertNull(OrderMapper.toCreateRequest(draft.copy(productName = "Palta")))
    }

    @Test
    fun kilos_areParsedAndPalletsEstimated() {
        assertEquals(11_200.0, OrderValidator.parseKg("11 200")!!, 0.0)
        assertEquals(850.5, OrderValidator.parseKg("850,5")!!, 0.0)
        assertFalse(OrderValidator.isValidQuantity(0.0))
        assertFalse(OrderValidator.isValidQuantity(150_000.0))
        assertEquals(18, OrderValidator.estimatePallets(11_200.0))
        assertEquals(1, OrderValidator.estimatePallets(10.0))
    }
}
