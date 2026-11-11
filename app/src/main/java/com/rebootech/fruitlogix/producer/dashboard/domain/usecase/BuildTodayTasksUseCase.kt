package com.rebootech.fruitlogix.producer.dashboard.domain.usecase

import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.logisticsMonitoring.ProducerLogisticsMonitoringRoutes
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus

/**
 * Builds today's task list from orders and stock. Returns data only; the UI turns each
 * task into localized text.
 */
class BuildTodayTasksUseCase {
    operator fun invoke(
        orders: List<Order>,
        stock: List<StockItem>
    ): List<ProducerTask> {
        val tasks = mutableListOf<ProducerTask>()

        // 1. New orders waiting to be accepted (PENDING)
        orders.filter { it.status == OrderStatus.PENDING }.forEach { order ->
            tasks += ProducerTask.NewOrder(
                id = "task_pending_${order.id}",
                destinationRoute = ProducerOrderManagementRoutes.orderDetail(order.id),
                orderId = order.id,
                clientName = order.clientName,
                itemsSummary = order.itemsSummary()
            )
        }

        // 2. Rejected quality reports
        orders.filter { it.qualityStatus == QualityStatus.REJECTED }.forEach { order ->
            tasks += ProducerTask.QualityRejected(
                id = "task_rejected_${order.id}",
                destinationRoute = ProducerQualityControlRoutes.reviewResult(order.id),
                orderId = order.id,
                reason = order.rejectionReason
            )
        }

        // 3. Pending inspections (IN_PREPARATION without a report).
        // PENDING quality means "report submitted, in review"; REJECTED has its own task.
        orders.filter { it.status == OrderStatus.IN_PREPARATION && it.qualityStatus == null }.forEach { order ->
            tasks += ProducerTask.InspectionPending(
                id = "task_inspection_${order.id}",
                destinationRoute = ProducerQualityControlRoutes.inspection(order.id),
                orderId = order.id,
                clientName = order.clientName,
                itemsSummary = order.itemsSummary()
            )
        }

        // 4. Trucks assigned and ready to load
        orders.filter { it.isReadyToLoad }.forEach { order ->
            tasks += ProducerTask.TruckAssigned(
                id = "task_truck_${order.id}",
                destinationRoute = ProducerLogisticsMonitoringRoutes.loadTruck(order.id),
                orderId = order.id,
                truckPlate = order.assignedTruck?.plate.orEmpty(),
                eta = order.assignedTruck?.etaFormatted ?: "14:30"
            )
        }

        // 5. Critical stock
        stock.filter { it.status == StockStatus.CRITICAL }.forEach { item ->
            tasks += ProducerTask.CriticalStock(
                id = "task_stock_${item.productId}",
                destinationRoute = ProducerQualityControlRoutes.stockDetail(item.productId),
                productName = item.productName,
                availableKg = item.availableKg,
                minThresholdKg = item.minThresholdKg
            )
        }

        return tasks
    }

    private fun Order.itemsSummary(): String =
        items.joinToString(", ") { "${it.productName} ${it.kg} kg" }
}
