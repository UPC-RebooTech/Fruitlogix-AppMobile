package com.rebootech.fruitlogix.logisticsMonitoring.data

import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchDetail
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchSummary
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchTemperaturePoint
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.FleetAlert
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.MilestoneStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.RouteMilestone
import com.rebootech.fruitlogix.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.Sensor
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorStatus
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.SensorType
import com.rebootech.fruitlogix.shared.ui.components.TemperaturePoint

/**
 * In-memory fake implementation of [FleetRepository].
 *
 * All data uses Peruvian context:
 *  - Country: Peru, hub: Lima
 *  - Time format: 24h + "PET" suffix
 *  - Currency: Peruvian Sol (S/)
 *  - Plates: Peruvian format ABC-123
 *  - Routes: Panamericana Sur / Norte / Carretera Central
 *  - Sensor TH-09 is the ONLY offline sensor and belongs to FL-408
 */
class FakeFleetRepository : FleetRepository {

    override fun getActiveDispatches(): List<DispatchSummary> = listOf(

        // FL-102 – ON TIME – Ica grapes – Panamericana Sur
        DispatchSummary(
            id = "FL-102",
            licensePlate = "BQK-482",
            cargoDescription = "Ica grapes \u2022 16 Pallets (Grade A)",
            routeLabel = "Ica \u2192 Lima Central Hub",
            reeferTemp = "3.4\u00b0C",
            reeferTempTarget = "Target 3.0\u00b0C",
            humidity = "88% RH",
            dockEta = "14:30 PET",
            etaDelta = null,
            progressFraction = 0.72f,
            progressLabel = "284 / 395 km",
            status = DispatchStatus.ON_TIME,
            footerNote = "BLE Sensatag Sync OK",
            showImmediateDiagnostic = false
        ),

        // FL-408 – TEMP ALERT – Hass avocado (Chavín de Huántar) – Carretera Central
        DispatchSummary(
            id = "FL-408",
            licensePlate = "AZT-901",
            cargoDescription = "Hass avocado \u2022 18 Pallets",
            routeLabel = "Chav\u00edn de Hu\u00e1ntar \u2192 Callao Cold Hub",
            reeferTemp = "3.2\u00b0C",
            reeferTempTarget = "Target 4.0\u00b0C",
            humidity = "84% RH",
            dockEta = "14:50 PET",
            etaDelta = null,
            progressFraction = 0.82f,
            progressLabel = "182 / 223 km \u2022 Gateway Delay",
            status = DispatchStatus.TEMP_ALERT,
            footerNote = null,
            showImmediateDiagnostic = true
        ),

        // FL-219 – WEIGH STATION DELAY – Piura mango – Panamericana Norte
        DispatchSummary(
            id = "FL-219",
            licensePlate = "CHD-330",
            cargoDescription = "Piura mango \u2022 20 Pallets",
            routeLabel = "Piura \u2192 Lima Central Hub",
            reeferTemp = "2.1\u00b0C",
            reeferTempTarget = "Nominal",
            humidity = "91% RH",
            dockEta = "16:45 PET",
            etaDelta = "+45m",
            progressFraction = 0.40f,
            progressLabel = "112 / 280 km \u2022 Weigh station delay",
            status = DispatchStatus.WEIGH_STATION_DELAY,
            footerNote = "Weigh Station Hold",
            showImmediateDiagnostic = false
        )
    )

    override fun getFleetAlerts(): List<FleetAlert> = listOf(
        FleetAlert(
            id = "alert-1",
            title = "Reefer temp 4.3\u00b0C – above 3.5\u00b0C threshold",
            unitId = "FL-408",
            severity = DispatchStatus.TEMP_ALERT
        ),
        FleetAlert(
            id = "alert-2",
            title = "Sensor TH-09 offline \u2022 47 min last seen",
            unitId = "FL-408",
            severity = DispatchStatus.WEIGH_STATION_DELAY
        )
    )

    override fun getAllSensors(): List<Sensor> = listOf(

        // ----------- ON ROUTE SENSORS -----------

        // TH-09 – OFFLINE – belongs to FL-408 (the ONLY offline sensor)
        Sensor(
            id = "#TH-09",
            type = SensorType.TEMPERATURE,
            linkedTruckId = "FL-408",
            linkedOrderId = "FX-1042",
            cargoOrLocation = "Hass Avocado Premium Export",
            lastSignal = "Lost 47 min ago \u2022 KM 184 Carretera Central",
            batteryPercent = 14,
            status = SensorStatus.OFFLINE,
            isOfflineWithLastSeen = true
        ),

        // GPS-204 – LOW BATTERY – belongs to FL-219
        Sensor(
            id = "#GPS-204",
            type = SensorType.GPS,
            linkedTruckId = "FL-219",
            linkedOrderId = "FX-1039",
            cargoOrLocation = "Piura mango \u2013 Locked",
            lastSignal = "1 min ago \u2022 SatCom Mesh Sync",
            batteryPercent = 18,
            status = SensorStatus.LOW_BATTERY,
            detailLabel1 = "Ambient",
            detailValue1 = "26.4\u00b0C",
            detailLabel2 = "Seal Status",
            detailValue2 = "Tamper Safe"
        ),

        // TH-04 – ONLINE – belongs to FL-102
        Sensor(
            id = "#TH-04",
            type = SensorType.TEMPERATURE,
            linkedTruckId = "FL-102",
            linkedOrderId = "FX-1040",
            cargoOrLocation = "Ica Organic Grapes",
            lastSignal = "24 s ago \u2022 BLE Gateway Sync",
            batteryPercent = 86,
            status = SensorStatus.ONLINE,
            detailLabel1 = "CHILL CORE",
            detailValue1 = "3.4\u00b0C",
            detailLabel2 = "HUMIDITY",
            detailValue2 = "88%"
        ),

        // ----------- WAREHOUSE / STANDBY SENSORS -----------

        // TH-15 – STANDBY – Callao Cold Hub, Staging Bay 3 (unlinked)
        Sensor(
            id = "#TH-15",
            type = SensorType.TEMPERATURE,
            linkedTruckId = null,
            linkedOrderId = null,
            cargoOrLocation = "Multi-Zone Reefer Probe",
            lastSignal = "5 min ago \u2022 Mesh Relay Active (94% BATT)",
            batteryPercent = 94,
            status = SensorStatus.STANDBY,
            warehouseLocation = "Callao Cold Hub \u2022 Staging Bay 3"
        ),

        // ENV-08 – STANDBY – Cold Room 02 (unlinked)
        Sensor(
            id = "#ENV-08",
            type = SensorType.HUMIDITY,
            linkedTruckId = null,
            linkedOrderId = null,
            cargoOrLocation = "Atmospheric O\u2082 & Ethylene Sensor",
            lastSignal = "12 min ago \u2022 Ethylene: 0.02 ppm (76% BATT)",
            batteryPercent = 76,
            status = SensorStatus.STANDBY,
            warehouseLocation = "Callao Cold Hub \u2022 Dock B"
        )
    )

    override fun getDispatchDetail(unitId: String): DispatchDetail {
        val summary = getActiveDispatches().find { it.id == unitId } ?: getActiveDispatches().first()

        return when (unitId) {
            "FL-408" -> {
                DispatchDetail(
                    unitId = "FL-408",
                    licensePlate = "AZT-901",
                    truckModel = "International LT Refrigerated",
                    orderId = "FX-1042",
                    cargoDescription = "Hass avocado \u2022 18 Pallets",
                    originName = "Chav\u00edn de Hu\u00e1ntar",
                    destinationName = "Callao Cold Hub",
                    routeLabel = "Chav\u00edn de Hu\u00e1ntar \u2192 Callao Cold Hub",
                    status = DispatchStatus.TEMP_ALERT,
                    dockEta = "14:30 PET",
                    progressFraction = 0.82f,
                    distanceProgress = "182 / 220 km",
                    delayText = "+12m delay",
                    progressLabel = "182 / 220 km \u2022 +12m delay",
                    currentTemp = "3.2\u00b0C",
                    tempTarget = "Target 4.0\u00b0C",
                    rateOfRise = "+0.4\u00b0C/min",
                    timeToBreachMinutes = 12,
                    humidity = "92% RH",
                    speedKmh = "68 km/h",
                    ambientTemp = "24.5\u00b0C",
                    batteryPercent = 78,
                    sensorId = "#TH-09",
                    sensorSignal = "BLE Gateway Sync OK",
                    currentLocationLabel = "Carretera Central \u2022 Km 182",
                    hasPredictiveAlert = true,
                    predictiveAlertMessage = "Forecast: will exceed 4.0\u00b0C in 12 min",
                    driverName = "Mateo Silva",
                    driverPhone = "+51 987 654 321",
                    driverLicense = "Lic. A-IIIc \u2022 8 yrs exp",
                    minTempThreshold = 2.0f,
                    maxTempThreshold = 4.0f,
                    temperaturePoints = listOf(
                        TemperaturePoint(timeLabel = "04:00", celsius = 2.4f),
                        TemperaturePoint(timeLabel = "06:42", celsius = 4.6f),
                        TemperaturePoint(timeLabel = "08:30", celsius = 2.8f),
                        TemperaturePoint(timeLabel = "10:00", celsius = 2.9f),
                        TemperaturePoint(timeLabel = "13:00", celsius = 3.0f),
                        TemperaturePoint(timeLabel = "13:48", celsius = 3.2f)
                    ),
                    detailedTemperaturePoints = listOf(
                        DispatchTemperaturePoint("04:00", 2.4f),
                        DispatchTemperaturePoint(
                            timeLabel = "06:42",
                            celsius = 4.6f,
                            isExcursion = true,
                            excursionDurationMinutes = 8,
                            excursionTooltip = "Excursion 06:42 \u2022 4.6\u00b0C \u2022 8 min"
                        ),
                        DispatchTemperaturePoint("08:30", 2.8f),
                        DispatchTemperaturePoint("10:00", 2.9f),
                        DispatchTemperaturePoint("13:00", 3.0f),
                        DispatchTemperaturePoint("NOW (13:48)", 3.2f)
                    ),
                    milestones = listOf(
                        RouteMilestone(
                            id = "m1",
                            title = "Chav\u00edn de Hu\u00e1ntar Packhouse",
                            locationName = "Origin Facility",
                            timestamp = "04:15 PET",
                            status = MilestoneStatus.COMPLETED,
                            detailNote = "Departed \u2022 Pallets inspected & sealed"
                        ),
                        RouteMilestone(
                            id = "m2",
                            title = "San Mateo Checkpoint",
                            locationName = "KM 94 Carretera Central",
                            timestamp = "07:30 PET",
                            status = MilestoneStatus.COMPLETED,
                            detailNote = "Passed \u2022 Telemetry ping normal"
                        ),
                        RouteMilestone(
                            id = "m3",
                            title = "Current Position: Km 182",
                            locationName = "Carretera Central",
                            timestamp = "13:48 PET",
                            status = MilestoneStatus.IN_TRANSIT,
                            detailNote = "Cruising at 68 km/h \u2022 Carretera Central (+12m delay)"
                        ),
                        RouteMilestone(
                            id = "m4",
                            title = "Callao Cold-Hub Dock #14",
                            locationName = "Destination Bay",
                            timestamp = "Est. 14:30 PET",
                            status = MilestoneStatus.PENDING,
                            detailNote = "Inbound staging bay reserved"
                        )
                    )
                )
            }
            "FL-219" -> {
                DispatchDetail(
                    unitId = "FL-219",
                    licensePlate = "CHD-330",
                    truckModel = "Volvo FH Refrigerated",
                    orderId = "FX-1039",
                    cargoDescription = "Piura mango \u2022 20 Pallets",
                    originName = "Piura Packing Bay",
                    destinationName = "Lima Central Hub",
                    routeLabel = "Piura \u2192 Lima Central Hub",
                    status = DispatchStatus.WEIGH_STATION_DELAY,
                    dockEta = "16:45 PET",
                    progressFraction = 0.40f,
                    distanceProgress = "112 / 280 km",
                    delayText = "+45m delay",
                    progressLabel = "112 / 280 km \u2022 Weigh station delay",
                    currentTemp = "2.1\u00b0C",
                    tempTarget = "Nominal",
                    rateOfRise = "+0.1\u00b0C/min",
                    timeToBreachMinutes = Int.MAX_VALUE,
                    humidity = "91% RH",
                    speedKmh = "45 km/h",
                    ambientTemp = "26.4\u00b0C",
                    batteryPercent = 65,
                    sensorId = "#GPS-204",
                    sensorSignal = "SatCom Mesh Sync",
                    currentLocationLabel = "Panamericana Norte KM 112",
                    hasPredictiveAlert = false,
                    predictiveAlertMessage = null,
                    driverName = "Luis Ramos",
                    driverPhone = "+51 976 543 210",
                    driverLicense = "Lic. A-IIIc \u2022 10 yrs exp",
                    minTempThreshold = 2.0f,
                    maxTempThreshold = 4.0f,
                    temperaturePoints = listOf(
                        TemperaturePoint(timeLabel = "04:00", celsius = 2.0f),
                        TemperaturePoint(timeLabel = "07:00", celsius = 2.1f),
                        TemperaturePoint(timeLabel = "10:00", celsius = 2.0f),
                        TemperaturePoint(timeLabel = "13:00", celsius = 2.1f),
                        TemperaturePoint(timeLabel = "13:48", celsius = 2.1f)
                    ),
                    detailedTemperaturePoints = listOf(
                        DispatchTemperaturePoint("04:00", 2.0f),
                        DispatchTemperaturePoint("07:00", 2.1f),
                        DispatchTemperaturePoint("10:00", 2.0f),
                        DispatchTemperaturePoint("13:00", 2.1f),
                        DispatchTemperaturePoint("NOW (13:48)", 2.1f)
                    ),
                    milestones = listOf(
                        RouteMilestone(
                            id = "m1",
                            title = "Piura Packhouse",
                            locationName = "Origin",
                            timestamp = "03:00 PET",
                            status = MilestoneStatus.COMPLETED,
                            detailNote = "Departed \u2022 Pallets inspected & sealed"
                        ),
                        RouteMilestone(
                            id = "m2",
                            title = "Anc\u00f3n Weigh Station",
                            locationName = "KM 48 Panamericana Norte",
                            timestamp = "10:45 PET",
                            status = MilestoneStatus.WARNING,
                            detailNote = "Stalled in queue (+45m delay)"
                        ),
                        RouteMilestone(
                            id = "m3",
                            title = "Current Position: Km 112",
                            locationName = "Panamericana Norte",
                            timestamp = "13:48 PET",
                            status = MilestoneStatus.IN_TRANSIT,
                            detailNote = "Cruising at 45 km/h"
                        ),
                        RouteMilestone(
                            id = "m4",
                            title = "Lima Central Hub",
                            locationName = "Destination",
                            timestamp = "Est. 16:45 PET",
                            status = MilestoneStatus.PENDING,
                            detailNote = "Inbound staging bay reserved"
                        )
                    )
                )
            }
            else -> {
                // FL-102 or default on-time unit
                DispatchDetail(
                    unitId = summary.id,
                    licensePlate = summary.licensePlate,
                    truckModel = "Kenworth T680 Reefer",
                    orderId = "FX-1040",
                    cargoDescription = summary.cargoDescription,
                    originName = summary.routeLabel.split("\u2192").firstOrNull()?.trim() ?: "Ica",
                    destinationName = summary.routeLabel.split("\u2192").lastOrNull()?.trim() ?: "Lima Central Hub",
                    routeLabel = summary.routeLabel,
                    status = summary.status,
                    dockEta = summary.dockEta,
                    progressFraction = summary.progressFraction,
                    distanceProgress = summary.progressLabel.split("\u2022").firstOrNull()?.trim() ?: "284 / 395 km",
                    delayText = summary.etaDelta,
                    progressLabel = summary.progressLabel,
                    currentTemp = summary.reeferTemp,
                    tempTarget = summary.reeferTempTarget,
                    rateOfRise = "+0.0\u00b0C/min",
                    timeToBreachMinutes = Int.MAX_VALUE,
                    humidity = summary.humidity,
                    speedKmh = "72 km/h",
                    ambientTemp = "22.0\u00b0C",
                    batteryPercent = 86,
                    sensorId = "#TH-04",
                    sensorSignal = "24 s ago \u2022 BLE Gateway Sync OK",
                    currentLocationLabel = "Panamericana Sur KM 284",
                    hasPredictiveAlert = false,
                    predictiveAlertMessage = null,
                    driverName = "Jorge Huam\u00e1n",
                    driverPhone = "+51 912 345 678",
                    driverLicense = "Lic. A-IIIc \u2022 12 yrs exp",
                    minTempThreshold = 2.0f,
                    maxTempThreshold = 4.0f,
                    temperaturePoints = listOf(
                        TemperaturePoint(timeLabel = "04:00", celsius = 3.0f),
                        TemperaturePoint(timeLabel = "07:00", celsius = 3.1f),
                        TemperaturePoint(timeLabel = "10:00", celsius = 3.2f),
                        TemperaturePoint(timeLabel = "13:00", celsius = 3.3f),
                        TemperaturePoint(timeLabel = "13:48", celsius = 3.4f)
                    ),
                    detailedTemperaturePoints = listOf(
                        DispatchTemperaturePoint("04:00", 3.0f),
                        DispatchTemperaturePoint("07:00", 3.1f),
                        DispatchTemperaturePoint("10:00", 3.2f),
                        DispatchTemperaturePoint("13:00", 3.3f),
                        DispatchTemperaturePoint("NOW (13:48)", 3.4f)
                    ),
                    milestones = listOf(
                        RouteMilestone(
                            id = "m1",
                            title = "Ica Packhouse",
                            locationName = "Origin",
                            timestamp = "06:00 PET",
                            status = MilestoneStatus.COMPLETED,
                            detailNote = "Departed \u2022 Pallets inspected & sealed"
                        ),
                        RouteMilestone(
                            id = "m2",
                            title = "Chincha Checkpoint",
                            locationName = "KM 200 Panamericana Sur",
                            timestamp = "09:15 PET",
                            status = MilestoneStatus.COMPLETED,
                            detailNote = "Passed \u2022 Telemetry ping normal"
                        ),
                        RouteMilestone(
                            id = "m3",
                            title = "Current Position: Km 284",
                            locationName = "Panamericana Sur",
                            timestamp = "13:48 PET",
                            status = MilestoneStatus.IN_TRANSIT,
                            detailNote = "Cruising at 72 km/h \u2022 Panamericana Sur"
                        ),
                        RouteMilestone(
                            id = "m4",
                            title = "Lima Central Hub",
                            locationName = "Destination",
                            timestamp = summary.dockEta,
                            status = MilestoneStatus.PENDING,
                            detailNote = "Inbound staging bay reserved"
                        )
                    )
                )
            }
        }
    }
}
