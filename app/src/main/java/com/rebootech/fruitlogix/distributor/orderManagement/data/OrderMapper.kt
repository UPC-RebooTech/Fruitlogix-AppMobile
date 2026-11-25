package com.rebootech.fruitlogix.distributor.orderManagement.data

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CatalogProduct
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.validation.OrderValidator
import com.rebootech.fruitlogix.shared.core.network.CreateOrderRequestDto
import com.rebootech.fruitlogix.shared.core.network.OrderItemRequestDto
import com.rebootech.fruitlogix.shared.core.network.OrderResponseDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Server order (OrderResource) ↔ app order. Nothing is invented: a client whose name is unknown keeps
 * its id and a blank name, an order without items shows no product and 0 kg.
 */
internal object OrderMapper {

    /** Where a server order goes in the app. */
    sealed interface Mapped {
        data class Active(val order: DistributorOrder) : Mapped
        data class Finished(val item: OrderHistoryItem) : Mapped
    }

    fun codeFor(remoteId: Int) = "FX-$remoteId"

    /**
     * Server statuses: Pending, InPreparation, InTransit, Delivered, DeliveredWithIncidence, Rejected,
     * Cancelled. Unknown values are kept as pending so the order is still visible.
     * @param clientName name of a client account by id (from the catalog of clients).
     */
    fun map(dto: OrderResponseDto, clientName: (Int?) -> String? = { null }, zone: ZoneId = ZoneId.systemDefault()): Mapped? {
        val remoteId = dto.id ?: return null
        val dueDate = dto.deliveryDueDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val createdAt = parseDateTime(dto.createdAt, zone) ?: UNKNOWN_CREATION
        val items = dto.items.orEmpty()
        val kg = items.sumOf { it.quantityKg ?: 0.0 }
        val product = items.firstOrNull()?.productName.orEmpty()
        val client = dto.commercialClientName?.takeIf { it.isNotBlank() } ?: clientName(dto.commercialClientId).orEmpty()

        val finished = when (dto.status?.lowercase(Locale.ROOT)) {
            "delivered", "deliveredwithincidence" -> OrderHistoryStatus.DELIVERED
            "cancelled", "rejected" -> OrderHistoryStatus.CANCELLED
            else -> null
        }
        if (finished != null) {
            return Mapped.Finished(
                OrderHistoryItem(
                    id = codeFor(remoteId),
                    clientName = client,
                    clientId = dto.commercialClientId,
                    producerName = dto.producerName.orEmpty(),
                    productName = product,
                    // deliveredAt for deliveries (CAMBIOS-API §8.2); the last change for cancellations
                    completedAt = parseDateTime(dto.deliveredAt ?: dto.receivedAt ?: dto.updatedAt ?: dto.createdAt, zone)
                        ?: dueDate.atStartOfDay(),
                    status = finished
                )
            )
        }
        return Mapped.Active(
            DistributorOrder(
                id = codeFor(remoteId),
                clientName = client,
                productName = product,
                quantityKg = kg,
                pallets = if (kg > 0) OrderValidator.estimatePallets(kg) else 0,
                deliveryDate = dueDate,
                status = when (dto.status?.lowercase(Locale.ROOT)) {
                    "inpreparation" -> OrderStatus.IN_PREPARATION
                    "intransit" -> OrderStatus.ON_ROUTE
                    else -> OrderStatus.PENDING
                },
                createdAt = createdAt,
                clientId = dto.commercialClientId,
                remoteId = remoteId,
                producerId = dto.producerId,
                producerName = dto.producerName.orEmpty(),
                batchId = dto.batchId
            )
        )
    }

    /**
     * CreateOrderResource. The server recalculates subtotal and total, and for catalog products
     * takes the name and price from /products and assigns their producer.
     */
    fun toCreateRequest(draft: OrderDraft, product: CatalogProduct): CreateOrderRequestDto {
        val total = OrderValidator.totalAmount(product, draft.quantityKg)
        return CreateOrderRequestDto(
            commercialClientId = draft.clientId,
            deliveryDueDate = draft.deliveryDate.toString(),
            deliveryAddress = draft.deliveryAddress.trim(),
            totalAmount = total,
            notes = null,
            items = listOf(
                OrderItemRequestDto(
                    productId = product.id,
                    productName = product.name,
                    quantityKg = draft.quantityKg,
                    unitPrice = product.pricePerKg,
                    subtotal = total
                )
            )
        )
    }

    /** Accepts "2026-11-13T07:07:35.4166119+00:00" (converted to local time) or a plain local date-time. */
    fun parseDateTime(value: String?, zone: ZoneId = ZoneId.systemDefault()): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(zone).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
    }

    /** Used when the server sends no creation time: old enough that the 24 h delete window is closed. */
    private val UNKNOWN_CREATION: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0)
}
