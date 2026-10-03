package com.rebootech.fruitlogix.logistics.data

import com.rebootech.fruitlogix.logistics.domain.DispatchStatus
import com.rebootech.fruitlogix.logistics.domain.DispatchSummary
import com.rebootech.fruitlogix.logistics.domain.FleetAlert
import com.rebootech.fruitlogix.logistics.domain.FleetRepository
import com.rebootech.fruitlogix.logistics.domain.Sensor
import com.rebootech.fruitlogix.logistics.domain.SensorStatus
import com.rebootech.fruitlogix.logistics.domain.SensorType

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
            reeferTemp = "4.3\u00b0C",
            reeferTempTarget = "Target 3.5\u00b0C",
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
}
