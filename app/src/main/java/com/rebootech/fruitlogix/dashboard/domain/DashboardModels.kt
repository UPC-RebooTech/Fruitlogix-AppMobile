package com.rebootech.fruitlogix.dashboard.domain

import androidx.annotation.DrawableRes

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
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val style: PriorityActionStyle
)

enum class PriorityActionStyle {
    DARK,
    LIME,
    DANGER
}

data class CriticalAlert(
    val title: String,
    val badgeText: String,
    val unitLabel: String,
    val temperatureValue: String,
    val routeDescription: String,
    val locationLine: String,
    val actionButtonText: String
)

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
    val criticalAlert: CriticalAlert?,
    val kpis: List<KpiData>
)
