package com.rebootech.fruitlogix.producer.logisticsMonitoring.data

import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.LoadItem
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.PickupTruck
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.TruckLoad
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.repository.DispatchRepository
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads the truck load from the order context and translates it to the logistics model,
 * so logistics does not depend on order screens or models directly.
 */
class OrderDispatchRepository(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance
) : DispatchRepository {

    override fun observeLoad(orderId: String): Flow<TruckLoad?> {
        val id = orderId.removePrefix("#")
        return orderRepository.observeOrders().map { orders ->
            orders.find { it.id.removePrefix("#") == id }?.toTruckLoad()
        }
    }

    override suspend fun confirmDispatch(orderId: String): Boolean =
        orderRepository.dispatchOrder(orderId)

    private fun Order.toTruckLoad() = TruckLoad(
        orderId = id.removePrefix("#"),
        clientName = clientName,
        distributorName = distributorName,
        items = items.map { LoadItem(it.productName, it.kg) },
        truck = assignedTruck?.let {
            PickupTruck(
                plate = it.plate,
                vehicleType = it.vehicleType,
                driverName = it.driverName,
                driverPhone = it.driverPhone,
                etaFormatted = it.etaFormatted
            )
        },
        pickupFrom = pickupFrom,
        pickupTo = pickupTo,
        coldChainMinC = coldChainMinC,
        coldChainMaxC = coldChainMaxC,
        qualityApproved = qualityStatus == QualityStatus.APPROVED,
        isDispatched = status == OrderStatus.IN_TRANSIT || status == OrderStatus.DELIVERED
    )

    companion object {
        val instance: OrderDispatchRepository by lazy { OrderDispatchRepository() }
    }
}
