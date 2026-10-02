package com.rebootech.fruitlogix.dashboard.data

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.dashboard.domain.CriticalAlert
import com.rebootech.fruitlogix.dashboard.domain.DashboardData
import com.rebootech.fruitlogix.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.dashboard.domain.KpiBadgeType
import com.rebootech.fruitlogix.dashboard.domain.KpiData
import com.rebootech.fruitlogix.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.dashboard.domain.PriorityActionStyle
import com.rebootech.fruitlogix.dashboard.domain.SegmentedBarSegment

class FakeDashboardRepository {

    fun getDashboardData(): DashboardData = DashboardData(
        greeting = GreetingInfo(
            dateLine = "Thursday, Oct 1, 2026",
            shiftLabel = "Active Morning Shift",
            userName = "Carlos",
            hubDescription = "Fruit Distribution Tactical Hub • Pacific Sector"
        ),
        priorityActions = listOf(
            PriorityAction(
                title = "Assign Producer",
                subtitle = "Link new lots",
                iconRes = R.drawable.ic_assign_producer,
                style = PriorityActionStyle.DARK
            ),
            PriorityAction(
                title = "Dispatch Fleet",
                subtitle = "3 reefers ready",
                iconRes = R.drawable.ic_dispatch_truck,
                style = PriorityActionStyle.LIME
            ),
            PriorityAction(
                title = "Alerts",
                subtitle = "2 anomalies",
                iconRes = R.drawable.ic_alert_diamond,
                style = PriorityActionStyle.DANGER
            )
        ),
        criticalAlert = CriticalAlert(
            title = "CRITICAL THERMAL DEVIATION",
            badgeText = "IMMEDIATE ACTION",
            unitLabel = "Unit FL-408",
            temperatureValue = "6.8°C",
            routeDescription = "Michoacán Hass • Max limit: 4.0°C (+2.8°C rising)",
            locationLine = "KM 184 • Toluca Expy",
            actionButtonText = "REVIEW REEFER"
        ),
        kpis = listOf(
            KpiData(
                caption = "Active Orders",
                iconRes = R.drawable.ic_orders,
                value = "48",
                deltaText = "+12.5% vs yesterday",
                deltaIsPositive = true,
                progress = 0.72f
            ),
            KpiData(
                caption = "Daily Deliveries",
                iconRes = R.drawable.ic_verified,
                value = "32",
                footerLine1 = "Avg ETA: 42 min",
                footerBadgeText = "On time",
                footerBadgeType = KpiBadgeType.SUCCESS
            ),
            KpiData(
                caption = "Fruit Quality",
                iconRes = R.drawable.ic_shield_check,
                value = "98.4%",
                footerLine1 = "Last 200 lots",
                footerLine2 = "Grade A Certified"
            ),
            KpiData(
                caption = "IoT Alerts",
                iconRes = R.drawable.ic_sensor_waves,
                value = "03",
                footerLine1 = "2 Critical • 1 Warning",
                segmentedBarSegments = listOf(
                    SegmentedBarSegment(weight = 2f, type = KpiBadgeType.DANGER),
                    SegmentedBarSegment(weight = 1f, type = KpiBadgeType.WARNING)
                )
            )
        )
    )
}
