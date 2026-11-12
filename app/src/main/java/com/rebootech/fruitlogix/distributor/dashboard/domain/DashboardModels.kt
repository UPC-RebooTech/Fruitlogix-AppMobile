package com.rebootech.fruitlogix.distributor.dashboard.domain

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlert

/**
 * Domain models for the distributor Home. Only data: every label is resolved in presentation,
 * so the whole screen follows the selected language. Trucks and alerts are the same ones the
 * Fleet screen shows (read through the Logistics and IoT domain interfaces).
 */

/** Something the distributor has to do today. */
sealed interface DistributorTask {
    val id: String

    /** An export order cannot leave without its SUNAT dispatch guide. */
    data class MissingDispatchGuide(
        override val id: String,
        val orderId: String,
        val producerName: String
    ) : DistributorTask

    /** A truck is at the hub gate: its lot has to be received. */
    data class ReceiveAtDock(
        override val id: String,
        val unitId: String,
        val dock: String,
        val pallets: Int,
        val productName: String
    ) : DistributorTask
}

data class DashboardSummary(
    val activeOrders: Int,
    val pendingOrders: Int,
    val deliveriesToday: Int,
    val averageEtaMinutes: Int,
    val qualityApprovalPct: Double,
    val lotsInspected: Int,
    val criticalAlerts: Int,
    val warningAlerts: Int
)

/** A truck on the road as the Home shows it. */
data class HomeTruck(
    val dispatch: ActiveDispatch,
    val celsius: Double?,
    val atRisk: Boolean
)

data class DashboardData(
    val hubName: String,
    val temperatureAlert: PredictiveAlert?,
    val summary: DashboardSummary,
    val tasks: List<DistributorTask>,
    val fleet: List<HomeTruck>
)
