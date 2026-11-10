package com.rebootech.fruitlogix.producer.dashboard.domain.usecase

import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.dashboard.domain.model.TaskPriority
import com.rebootech.fruitlogix.producer.logisticsMonitoring.ProducerLogisticsMonitoringRoutes
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType

class BuildTodayTasksUseCase {
    operator fun invoke(
        orders: List<Order>,
        stock: List<StockItem>
    ): List<ProducerTask> {
        val tasks = mutableListOf<ProducerTask>()

        // 1. Pedidos nuevos pendientes (PENDING)
        orders.filter { it.status == OrderStatus.PENDING }.forEach { order ->
            tasks.add(
                ProducerTask(
                    id = "task_pending_${order.id}",
                    title = "New order to accept",
                    detail = "${order.id} ${order.clientName} • ${order.items.joinToString { "${it.productName} ${it.kg} kg" }}",
                    priority = TaskPriority.MEDIUM,
                    destinationRoute = ProducerOrderManagementRoutes.orderDetail(order.id)
                )
            )
        }

        // 2. Reportes rechazados (qualityStatus == REJECTED)
        orders.filter { it.qualityStatus == QualityStatus.REJECTED }.forEach { order ->
            tasks.add(
                ProducerTask(
                    id = "task_rejected_${order.id}",
                    title = "Report rejected, re-inspect",
                    detail = "${order.id} • ${order.rejectionReason ?: "Check rejection details"}",
                    priority = TaskPriority.HIGH,
                    destinationRoute = ProducerQualityControlRoutes.reviewResult(order.id)
                )
            )
        }

        // 3. Inspecciones pendientes (IN_PREPARATION y qualityStatus == null)
        // Note: PENDING means "report submitted, in review" - not needing a new inspection
        // REJECTED has its own task ("Report rejected, re-inspect")
        orders.filter { it.status == OrderStatus.IN_PREPARATION && it.qualityStatus == null }.forEach { order ->
            tasks.add(
                ProducerTask(
                    id = "task_inspection_${order.id}",
                    title = "Inspection pending",
                    detail = "${order.id} ${order.clientName} • ${order.items.joinToString { "${it.productName} ${it.kg} kg" }}",
                    priority = TaskPriority.MEDIUM,
                    destinationRoute = ProducerQualityControlRoutes.inspection(order.id)
                )
            )
        }

        // 4. Camiones asignados ("Ready to load")
        orders.filter { it.isReadyToLoad }.forEach { order ->
            tasks.add(
                ProducerTask(
                    id = "task_truck_${order.id}",
                    title = "Truck ${order.assignedTruck?.plate ?: ""} assigned",
                    detail = "${order.id} Ready to load • ETA ${order.assignedTruck?.etaFormatted ?: "14:30"}",
                    priority = TaskPriority.LOW,
                    destinationRoute = ProducerLogisticsMonitoringRoutes.loadTruck(order.id),
                    badgeText = "Ready to load",
                    badgeType = StatusBadgeType.HIGHLIGHT
                )
            )
        }

        // 5. Stock crítico (CRITICAL)
        stock.filter { it.status == StockStatus.CRITICAL }.forEach { item ->
            tasks.add(
                ProducerTask(
                    id = "task_stock_${item.productId}",
                    title = "Critical stock",
                    detail = "${item.productName} ${item.availableKg} kg (below ${item.minThresholdKg} kg threshold)",
                    priority = TaskPriority.HIGH,
                    destinationRoute = ProducerQualityControlRoutes.stockDetail(item.productId)
                )
            )
        }

        return tasks
    }
}

