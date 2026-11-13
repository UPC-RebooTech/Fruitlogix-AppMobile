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
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.LotStatus
import com.rebootech.fruitlogix.distributor.qualityControl.domain.repository.QualityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

class FakeDashboardRepository(
    private val orders: OrderRepository = DistributorDependencies.orderRepository,
    private val fleet: FleetRepository = DistributorDependencies.fleetRepository,
    private val iot: IotRepository = DistributorDependencies.iotRepository,
    private val quality: QualityRepository = DistributorDependencies.qualityRepository
) {
    private val predictor = PredictTemperatureBreachUseCase()

    fun observeDashboard(): Flow<DashboardData> =
        combine(
            orders.orders,
            orders.history,
            fleet.dispatches,
            iot.alerts,
            quality.lots
        ) { orderList, historyList, dispatches, alerts, qualityLots ->
            val active = dispatches.filter { it.isActive }
            val predictions = active.associate { it.unitId to predict(it) }

            val avgEta = if (active.isNotEmpty()) {
                active.map { ((it.remainingKm / 55.0) * 60.0).roundToInt() + it.delayMinutes }
                    .average()
                    .roundToInt()
                    .coerceAtLeast(5)
            } else {
                0
            }

            val decidedLots = qualityLots.filter { it.status != LotStatus.PENDING }
            val approvalPct = if (decidedLots.isNotEmpty()) {
                val approved = decidedLots.count { it.status == LotStatus.APPROVED }
                ((approved.toDouble() / decidedLots.size) * 1000.0).roundToInt() / 10.0
            } else {
                100.0
            }

            DashboardData(
                hubName = GeofenceZone().name,
                temperatureAlert = predictions.values.filterNotNull()
                    .sortedBy { if (it.status == PredictiveAlertStatus.NOMINAL) 1 else 0 }
                    .firstOrNull(),
                summary = DashboardSummary(
                    activeOrders = orderList.size,
                    pendingOrders = orderList.count { it.status == OrderStatus.PENDING },
                    deliveriesToday = dispatches.size + historyList.size,
                    averageEtaMinutes = avgEta,
                    qualityApprovalPct = approvalPct,
                    lotsInspected = qualityLots.size,
                    criticalAlerts = alerts.count { it.severity == AlertSeverity.CRITICAL },
                    warningAlerts = alerts.count { it.severity == AlertSeverity.WARNING }
                ),
                tasks = buildList {
                    orderList.firstOrNull { it.status == OrderStatus.IN_PREPARATION }?.let { prepOrder ->
                        add(
                            DistributorTask.MissingDispatchGuide(
                                id = "task-guide-${prepOrder.id}",
                                orderId = prepOrder.id,
                                producerName = "Agro Valle Verde"
                            )
                        )
                    }
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