package com.rebootech.fruitlogix.producer.dashboard.domain.model

import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class ProducerTask(
    val id: String,
    val title: String,
    val detail: String,
    val priority: TaskPriority,
    val destinationRoute: String,
    val badgeText: String? = null,
    val badgeType: StatusBadgeType? = null
)

data class PipelineCount(
    val pending: Int,
    val inPrep: Int,
    val inTransit: Int,
    val delivered: Int
) {
    val totalActive: Int get() = pending + inPrep + inTransit
}

data class DashboardSummary(
    val pendingOrdersCount: Int,
    val lotsDispatchedThisMonth: Int,
    val qualityApprovalPct: Int,
    val pendingPaymentsAmount: Double,
    val paidPaymentsAmount: Double
)

