package com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

/**
 * Trips of the distributor's fleet. State changes are kept here (not in a screen),
 * so leaving and coming back to a dispatch shows the same status, ETA and checks.
 */
interface FleetRepository {
    val dispatches: StateFlow<List<ActiveDispatch>>

    fun dispatch(unitId: String): ActiveDispatch?

    /** Pending ⇄ On route. Closing the order goes through [completeDelivery] / [rejectDelivery]. */
    suspend fun updateDeliveryState(unitId: String, state: DeliveryState)

    suspend fun updateEta(unitId: String, newEta: LocalTime, reason: DelayReason)

    suspend fun recordReeferCheck(unitId: String)

    /** Delivered with proof: the order is also closed in Orders. */
    suspend fun completeDelivery(unitId: String)

    suspend fun rejectDelivery(unitId: String, reason: String)
}
