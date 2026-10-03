package com.rebootech.fruitlogix.dashboard.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Domain models for the Home dashboard screen.
 */

data class GreetingInfo(
    val dateLine: String,
    val shiftLabel: String,
    val userName: String,
    val hubDescription: String
)

data class PriorityAction(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val style: PriorityActionStyle
)

enum class PriorityActionStyle {
    DARK,
    LIME
}

data class KpiData(
    val caption: String,
    @DrawableRes val iconRes: Int,
    val value: String,
    val deltaText: String? = null,
    val deltaIsPositive: Boolean = true,
    val progress: Float? = null,
    val footerLine1: String? = null,
    val footerLine2: String? = null,
    val footerBadgeText: String? = null,
    val footerBadgeType: KpiBadgeType = KpiBadgeType.SUCCESS,
    val segmentedBarSegments: List<SegmentedBarSegment>? = null
)

enum class KpiBadgeType {
    SUCCESS,
    WARNING,
    DANGER,
    INFO,
    NEUTRAL
}

data class SegmentedBarSegment(
    val weight: Float,
    val type: KpiBadgeType
)

data class DashboardData(
    val greeting: GreetingInfo,
    val priorityActions: List<PriorityAction>,
    val predictiveAlert: PredictiveAlert?,
    val kpis: List<KpiData>,
    val actionItems: List<ActionItem>,
    val fleetUnits: List<FleetUnitSummary>,
    val fleetInTransitCount: Int,
    val complianceSummary: ComplianceSummary
)
