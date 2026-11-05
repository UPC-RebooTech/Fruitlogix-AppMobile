package com.rebootech.fruitlogix.producer.dashboard.presentation

import com.rebootech.fruitlogix.producer.dashboard.domain.model.DashboardSummary
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem

data class ProducerHomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val greeting: String = "Buenos días, Alexandra",
    val farmName: String = "Fundo Santa Rosa",
    val currentDateFormatted: String = "SÁB, 31 OCT",
    val heroOrder: Order? = null,
    val heroStockShortage: String? = null,
    val tasks: List<ProducerTask> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(0, 0, 0, 0.0, 0.0),
    val pipeline: PipelineCount = PipelineCount(0, 0, 0, 0),
    val stockItems: List<StockItem> = emptyList(),
    val upcomingHarvest: PlannedHarvest? = null,
    val inspectingOrdersList: List<Order> = emptyList(),
    val showOrderPickerSheet: Boolean = false,
    val showAcceptConfirmDialog: Boolean = false,
    val pendingAcceptOrder: Order? = null,
    val snackbarMessage: String? = null
)

