package com.rebootech.fruitlogix.producer.dashboard.data

import com.rebootech.fruitlogix.producer.dashboard.domain.model.DashboardSummary
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.producer.dashboard.domain.repository.DashboardRepository
import com.rebootech.fruitlogix.producer.dashboard.domain.repository.ProducerDashboardData
import com.rebootech.fruitlogix.producer.dashboard.domain.usecase.BuildTodayTasksUseCase
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.producer.paymentManagement.data.FakePaymentRepository
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PaymentSummary
import com.rebootech.fruitlogix.producer.paymentManagement.domain.repository.PaymentRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.stock.data.FakeStockRepository
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FakeDashboardRepository(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance,
    private val stockRepository: StockRepository = FakeStockRepository.instance,
    private val harvestRepository: HarvestRepository = FakeHarvestRepository.instance,
    private val paymentRepository: PaymentRepository = FakePaymentRepository.instance,
    private val buildTodayTasksUseCase: BuildTodayTasksUseCase = BuildTodayTasksUseCase()
) : DashboardRepository {

    override fun observeDashboard(): Flow<ProducerDashboardData> {
        return combine(
            orderRepository.observeOrders(),
            stockRepository.observeStock(),
            harvestRepository.observeUpcomingHarvest(),
            paymentRepository.observePaymentSummary()
        ) { orders: List<Order>, stock: List<StockItem>, harvest: PlannedHarvest?, payment: PaymentSummary ->
            val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }
            val inPrepOrders = orders.filter { it.status == OrderStatus.IN_PREPARATION }
            val inTransitOrders = orders.filter { it.status == OrderStatus.IN_TRANSIT }
            val deliveredOrders = orders.filter { it.status == OrderStatus.DELIVERED }

            val heroOrder = pendingOrders.firstOrNull()

            var shortageText: String? = null
            if (heroOrder != null) {
                for (item in heroOrder.items) {
                    val currentStock = stock.find { it.productId == item.productId || it.productName.equals(item.productName, ignoreCase = true) }
                    val available = currentStock?.availableKg ?: 0
                    if (available < item.kg) {
                        val diff = item.kg - available // <-- Corregido el operador de resta (-)

                        shortageText = "Short by $diff kg of ${item.productName}"
                        break
                    }
                }
            }

            val tasks = buildTodayTasksUseCase(orders, stock)

            val summary = DashboardSummary(
                pendingOrdersCount = pendingOrders.size + inPrepOrders.size,
                lotsDispatchedThisMonth = 12,
                qualityApprovalPct = 98,
                pendingPaymentsAmount = payment.pendingAmount,
                paidPaymentsAmount = payment.paidAmount
            )

            val pipeline = PipelineCount(
                pending = pendingOrders.size,
                inPrep = inPrepOrders.size,
                inTransit = inTransitOrders.size,
                delivered = deliveredOrders.size
            )

            val sortedStock = stock.sortedBy { it.status }

            ProducerDashboardData(
                heroOrder = heroOrder,
                heroStockShortage = shortageText,
                tasks = tasks,
                summary = summary,
                pipeline = pipeline,
                stockItems = sortedStock,
                upcomingHarvest = harvest
            )
        }
    }

    override suspend fun acceptOrder(orderId: String): Boolean {
        return orderRepository.acceptOrder(orderId)
    }

    companion object {
        val instance: FakeDashboardRepository by lazy { FakeDashboardRepository() }
    }
}