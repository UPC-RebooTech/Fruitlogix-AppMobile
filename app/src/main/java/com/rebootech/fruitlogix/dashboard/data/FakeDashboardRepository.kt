package com.rebootech.fruitlogix.dashboard.data

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.dashboard.domain.ActionItem
import com.rebootech.fruitlogix.dashboard.domain.ActionItemStyle
import com.rebootech.fruitlogix.dashboard.domain.ComplianceSummary
import com.rebootech.fruitlogix.dashboard.domain.DashboardData
import com.rebootech.fruitlogix.dashboard.domain.FleetStatusType
import com.rebootech.fruitlogix.dashboard.domain.FleetUnitSummary
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
            TemperatureReading(minutesAgo = 45, celsius = 0.20),
            TemperatureReading(minutesAgo = 36, celsius = 0.80),
            TemperatureReading(minutesAgo = 27, celsius = 1.40),
            TemperatureReading(minutesAgo = 18, celsius = 2.00),
            TemperatureReading(minutesAgo = 9, celsius = 2.60),
            TemperatureReading(minutesAgo = 0, celsius = 3.20)
        )

        val predictiveAlert = predictUseCase.execute(
            readings = fakeReadings,
            thresholdCelsius = 3.5,
            unitId = "FL-408",
            cargoDescription = "Hass avocado from Chav\u00edn de Hu\u00e1ntar"
        )

        val actionItems = listOf(
            ActionItem(
                id = "action-1",
                titleRes = R.string.action_cfdi_seal_title,
                subtitleRes = R.string.action_cfdi_seal_sub,
                captionRes = R.string.action_cfdi_seal_caption,
                buttonTextRes = R.string.action_cfdi_seal_btn,
                iconRes = R.drawable.ic_verified,
                hasRedDot = true,
                style = ActionItemStyle.LIME_BUTTON
            ),
            ActionItem(
                id = "action-2",
                titleRes = R.string.action_precooling_title,
                subtitleRes = R.string.action_precooling_sub,
                captionRes = R.string.action_precooling_caption,
                buttonTextRes = R.string.action_precooling_btn,
                iconRes = R.drawable.ic_inventory,
                hasRedDot = false,
                style = ActionItemStyle.DARK_BUTTON
            )
        )

        val fleetUnits = listOf(
            FleetUnitSummary(
                unitId = "FL-102",
                truckModel = "Kenworth T680",
                routeDescription = "Ica \u21c4 Lima Central Hub",
                statusTextRes = R.string.fleet_status_on_time,
                statusType = FleetStatusType.ON_TIME,
                reeferTemp = "3.4\u00b0C",
                humidity = "88% RH",
                destEtaOrDelay = "14:30 PET",
                isDelay = false,
                mileageText = "284 / 395 km",
                progressPercent = 0.72f,
                progressLabel = "72% Completed"
            ),
            FleetUnitSummary(
                unitId = "FL-408",
                truckModel = "International LT",
                routeDescription = "KM 184 \u2022 Panamericana Norte",
                statusTextRes = R.string.fleet_status_on_time,
                statusType = FleetStatusType.ON_TIME,
                reeferTemp = "4.3\u00b0C",
                humidity = "84% RH",
                destEtaOrDelay = "14:50 PET",
                isDelay = false,
                mileageText = "182 / 223 km",
                progressPercent = 0.82f,
                progressLabel = "82% \u2022 Gateway Delay"
            ),
            FleetUnitSummary(
                unitId = "FL-219",
                truckModel = "Freightliner M2",
                routeDescription = "Piura \u21c4 Lima Central Hub",
                statusTextRes = R.string.fleet_status_delay,
                statusType = FleetStatusType.DELAYED,
                reeferTemp = "2.1\u00b0C",
                humidity = "91% RH",
                destEtaOrDelay = "+45m",
                isDelay = true,
                mileageText = "112 / 280 km",
                progressPercent = 0.40f,
                progressLabel = "40% Weigh station delay"
            )
        )

        val complianceSummary = ComplianceSummary(
            compliancePercent = 87,
            transitIntegrityPercent = 94,
            coldBreachesToday = 0,
            predictiveRiskThermalCount = 1,
            predictiveRiskUnitId = "FL-408"
        )

        return DashboardData(
            greeting = GreetingInfo(
                dateLine = "Thursday, Oct 1, 2026",
                shiftLabel = "Active Morning Shift",
                userName = "Carlos",
                hubDescription = "Lima Central Hub \u2022 Panamericana Operations"
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
                    deltaText = "+12.5% vs yday",
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
            ),
            actionItems = actionItems,
            fleetUnits = fleetUnits,
            fleetInTransitCount = 18,
            complianceSummary = complianceSummary
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
            thresholdCelsius = 3.5,
            unitId = "FL-408",
            cargoDescription = "Hass avocado from Chav\u00edn de Hu\u00e1ntar"
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
            thresholdCelsius = 3.5,
            unitId = "FL-408",
            cargoDescription = "Hass avocado from Chav\u00edn de Hu\u00e1ntar"
        )
    }
}
