package com.rebootech.fruitlogix.producer.dashboard.presentation

import com.rebootech.fruitlogix.producer.dashboard.domain.model.DashboardSummary
import com.rebootech.fruitlogix.producer.dashboard.domain.model.PipelineCount
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.StockShortage
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.shared.core.model.UiText
import java.time.LocalDate

/** Part of the day, used to pick the greeting string in the UI. */
enum class DayPart { MORNING, AFTERNOON, EVENING }

/**
 * Home state. Holds data and resource-based text only (no resolved strings), so the
 * screen follows the selected language after the activity is recreated.
 */
data class ProducerHomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: UiText? = null,
    val dayPart: DayPart = DayPart.MORNING,
    val userName: String = "Alexandra",
    val farmName: String = "Fundo Santa Rosa",
    val today: LocalDate = LocalDate.now(),
    val heroOrder: Order? = null,
    val heroStockShortage: StockShortage? = null,
    val tasks: List<ProducerTask> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(0, 0, 0, 0.0, 0.0),
    val pipeline: PipelineCount = PipelineCount(0, 0, 0, 0),
    val stockItems: List<StockItem> = emptyList(),
    val upcomingHarvest: PlannedHarvest? = null,
    val inspectingOrdersList: List<Order> = emptyList(),
    val showOrderPickerSheet: Boolean = false,
    val showAcceptConfirmDialog: Boolean = false,
    val pendingAcceptOrder: Order? = null,
    val snackbarMessage: UiText? = null
)
