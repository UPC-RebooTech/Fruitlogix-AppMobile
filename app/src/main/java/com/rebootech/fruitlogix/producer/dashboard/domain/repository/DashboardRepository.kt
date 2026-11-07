package com.rebootech.fruitlogix.producer.dashboard.domain.repository

import com.rebootech.fruitlogix.producer.dashboard.domain.model.DashboardSummary
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import kotlinx.coroutines.flow.Flow

data class ProducerDashboardData(
    val heroOrder: Order?,
    val heroStockShortage: String?,
    val tasks: List<ProducerTask>,
    val summary: DashboardSummary,
    val pipeline: PipelineCount,
    val stockItems: List<StockItem>,
    val upcomingHarvest: PlannedHarvest?
)

interface DashboardRepository {
    fun observeDashboard(): Flow<ProducerDashboardData>
    suspend fun acceptOrder(orderId: String): Boolean
}

