package com.rebootech.fruitlogix.dashboard.presentation

import com.rebootech.fruitlogix.dashboard.domain.ActionItem
import com.rebootech.fruitlogix.dashboard.domain.ComplianceSummary
import com.rebootech.fruitlogix.dashboard.domain.FleetUnitSummary
import com.rebootech.fruitlogix.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.dashboard.domain.KpiData
import com.rebootech.fruitlogix.dashboard.domain.PredictiveAlert
import com.rebootech.fruitlogix.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.ui.components.AppLanguage

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
    val selectedLanguage: AppLanguage = AppLanguage.ES
)
