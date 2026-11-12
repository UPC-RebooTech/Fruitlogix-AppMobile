package com.rebootech.fruitlogix.distributor.dashboard.data

import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.dashboard.domain.DashboardData
import com.rebootech.fruitlogix.distributor.dashboard.domain.DashboardSummary
import com.rebootech.fruitlogix.distributor.dashboard.domain.DistributorTask
import com.rebootech.fruitlogix.distributor.dashboard.domain.HomeTruck
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.GeofenceZone
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlert
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlertStatus
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ArrivalRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.service.PredictTemperatureBreachUseCase
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Builds the Home from the other contexts, only through their domain interfaces: orders,
 * trucks and alerts are the same ones the Orders and Fleet tabs show.
 * Deliveries, ETA average and quality are still sample figures.
 */
class FakeDashboardRepository(
    private val orders: OrderRepository = DistributorDependencies.orderRepository,
    private val fleet: FleetRepository = DistributorDependencies.fleetRepository,
    private val iot: IotRepository = DistributorDependencies.iotRepository
) {
    private val predictor = PredictTemperatureBreachUseCase()

    fun observeDashboard(): Flow<DashboardData> =
        combine(orders.orders, fleet.dispatches, iot.devices, iot.alerts) { orderList, dispatches, _, alerts ->
            val active = dispatches.filter { it.isActive }
            val predictions = active.associate { it.unitId to predict(it) }
            DashboardData(
                hubName = GeofenceZone().name,
                // The riskiest truck; if none is at risk, the first one ("all in range")
                temperatureAlert = predictions.values.filterNotNull()
                    .sortedBy { if (it.status == PredictiveAlertStatus.NOMINAL) 1 else 0 }
                    .firstOrNull(),
                summary = DashboardSummary(
                    activeOrders = orderList.size,
                    pendingOrders = orderList.count { it.status == OrderStatus.PENDING },
                    deliveriesToday = 32,
                    averageEtaMinutes = 42,
                    qualityApprovalPct = 98.4,
                    lotsInspected = 200,
                    criticalAlerts = alerts.count { it.severity == AlertSeverity.CRITICAL },
                    warningAlerts = alerts.count { it.severity == AlertSeverity.WARNING }
                ),
                tasks = buildList {
                    add(DistributorTask.MissingDispatchGuide(id = "task-guide", orderId = "FX-1040", producerName = "Agro Valle Verde"))
                    active.filter { ArrivalRules.isAtGate(it) }.forEach {
                        add(DistributorTask.ReceiveAtDock("task-${it.unitId}", it.unitId, it.dock, it.pallets, it.productName))
                    }
                },
                fleet = active.map { d ->
                    HomeTruck(
                        dispatch = d,
                        celsius = iot.deviceForUnit(d.unitId)?.lastReading?.celsius,
                        atRisk = predictions[d.unitId]?.status?.let { it != PredictiveAlertStatus.NOMINAL } == true
                    )
                }
            )
        }

    private fun predict(dispatch: ActiveDispatch): PredictiveAlert? {
        val limit = iot.thresholdFor(dispatch.productName)?.maxTempCelsius ?: return null
        val recent = iot.temperatureHistory(dispatch.unitId).filter { it.minutesAgo <= 45 }
        return predictor.execute(recent, limit, dispatch.unitId, "${dispatch.productName} · ${dispatch.originName}")
    }
}
