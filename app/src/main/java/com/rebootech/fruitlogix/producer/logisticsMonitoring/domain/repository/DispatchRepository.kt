package com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.repository

import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.TruckLoad
import kotlinx.coroutines.flow.Flow

interface DispatchRepository {
    fun observeLoad(orderId: String): Flow<TruckLoad?>

    /** Confirms the truck left the farm: the order goes "in transit". */
    suspend fun confirmDispatch(orderId: String): Boolean
}
