package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

interface FleetRepository {
    val dispatches: StateFlow<List<ActiveDispatch>>

    fun dispatch(unitId: String): ActiveDispatch?

    suspend fun updateDeliveryState(unitId: String, state: DeliveryState)

    suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason)

    suspend fun recordReeferCheck(unitId: String)

    suspend fun completeDelivery(unitId: String)

    suspend fun rejectDelivery(unitId: String, reason: String)

    suspend fun refresh() {}
}