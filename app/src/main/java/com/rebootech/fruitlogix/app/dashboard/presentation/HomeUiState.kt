package com.rebootech.fruitlogix.app.dashboard.presentation

import com.rebootech.fruitlogix.app.dashboard.domain.ActionItem
import com.rebootech.fruitlogix.qualityControl.domain.model.ComplianceSummary
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.FleetUnitSummary
import com.rebootech.fruitlogix.app.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.app.dashboard.domain.KpiData
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.PredictiveAlert
import com.rebootech.fruitlogix.app.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: GreetingInfo? = null,
    val priorityActions: List<PriorityAction> = emptyList(),
    val priorityReadyCount: Int = 3,
    val predictiveAlert: PredictiveAlert? = null,
    val kpis: List<KpiData> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val fleetUnits: List<FleetUnitSummary> = emptyList(),
    val fleetInTransitCount: Int = 18,
    val complianceSummary: ComplianceSummary? = null,
    val selectedLanguage: com.rebootech.fruitlogix.shared.ui.components.AppLanguage = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppLanguage.ES
)
