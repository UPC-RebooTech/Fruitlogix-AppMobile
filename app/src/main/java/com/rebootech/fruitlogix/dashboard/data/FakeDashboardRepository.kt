package com.rebootech.fruitlogix.dashboard.data

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.dashboard.domain.DashboardData
import com.rebootech.fruitlogix.dashboard.domain.GreetingInfo
import com.rebootech.fruitlogix.dashboard.domain.KpiBadgeType
import com.rebootech.fruitlogix.dashboard.domain.KpiData
import com.rebootech.fruitlogix.dashboard.domain.PredictTemperatureBreachUseCase
import com.rebootech.fruitlogix.dashboard.domain.PredictiveAlert
import com.rebootech.fruitlogix.dashboard.domain.PriorityAction
import com.rebootech.fruitlogix.dashboard.domain.PriorityActionStyle
import com.rebootech.fruitlogix.dashboard.domain.SegmentedBarSegment
import com.rebootech.fruitlogix.dashboard.domain.TemperatureReading

class FakeDashboardRepository {

    private val predictUseCase = PredictTemperatureBreachUseCase()

    fun getDashboardData(): DashboardData {
        val fakeReadings = listOf(
            TemperatureReading(minutesAgo = 18, celsius = 2.00),
            TemperatureReading(minutesAgo = 16, celsius = 2.13),
            TemperatureReading(minutesAgo = 14, celsius = 2.27),
            TemperatureReading(minutesAgo = 12, celsius = 2.40),
            TemperatureReading(minutesAgo = 10, celsius = 2.53),
            TemperatureReading(minutesAgo = 8, celsius = 2.67),
            TemperatureReading(minutesAgo = 6, celsius = 2.80),
            TemperatureReading(minutesAgo = 4, celsius = 2.93),
            TemperatureReading(minutesAgo = 2, celsius = 3.07),
            TemperatureReading(minutesAgo = 0, celsius = 3.20)
        )

        val predictiveAlert = predictUseCase.execute(
            readings = fakeReadings,
            thresholdCelsius = 4.0,
            unitId = "FL-408",
            cargoDescription = "Michoacán Hass avocado"
        )

        return DashboardData(
            greeting = GreetingInfo(
                dateLine = "Thursday, Oct 1, 2026",
                shiftLabel = "Active Morning Shift",
                userName = "Carlos",
                hubDescription = "Fruit Distribution Tactical Hub • Pacific Sector"
            ),
            priorityActions = listOf(
                PriorityAction(
                    titleRes = R.string.action_scan_lot,
                    iconRes = R.drawable.ic_inventory,
                    style = PriorityActionStyle.LIME
                ),
                PriorityAction(
                    titleRes = R.string.action_dispatch_fleet,
                    iconRes = R.drawable.ic_dispatch_truck,
                    style = PriorityActionStyle.DARK
                ),
                PriorityAction(
                    titleRes = R.string.action_sensors,
                    iconRes = R.drawable.ic_sensor_waves,
                    style = PriorityActionStyle.DARK
                )
            ),
            predictiveAlert = predictiveAlert,
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

    fun getNominalAlertData(): PredictiveAlert? {
        val fakeReadings = listOf(
            TemperatureReading(minutesAgo = 18, celsius = 2.0),
            TemperatureReading(minutesAgo = 12, celsius = 2.0),
            TemperatureReading(minutesAgo = 6, celsius = 2.1),
            TemperatureReading(minutesAgo = 0, celsius = 2.1)
        )
        return predictUseCase.execute(
            readings = fakeReadings,
            thresholdCelsius = 4.0,
            unitId = "FL-408",
            cargoDescription = "Michoacán Hass avocado"
        )
    }

    fun getBreachedAlertData(): PredictiveAlert? {
        val fakeReadings = listOf(
            TemperatureReading(minutesAgo = 18, celsius = 3.5),
            TemperatureReading(minutesAgo = 12, celsius = 3.8),
            TemperatureReading(minutesAgo = 6, celsius = 4.2),
            TemperatureReading(minutesAgo = 0, celsius = 4.5)
        )
        return predictUseCase.execute(
            readings = fakeReadings,
            thresholdCelsius = 4.0,
            unitId = "FL-408",
            cargoDescription = "Michoacán Hass avocado"
        )
    }
}
