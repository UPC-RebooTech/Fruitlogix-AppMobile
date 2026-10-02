package com.rebootech.fruitlogix.dashboard.presentation

import com.rebootech.fruitlogix.dashboard.domain.CriticalAlert
import com.rebootech.fruitlogix.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.dashboard.domain.KpiData
import com.rebootech.fruitlogix.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.ui.components.AppLanguage

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: GreetingInfo? = null,
    val priorityActions: List<PriorityAction> = emptyList(),
    val priorityReadyCount: Int = 3,
    val criticalAlert: CriticalAlert? = null,
    val kpis: List<KpiData> = emptyList(),
    val selectedLanguage: AppLanguage = AppLanguage.ES
)
