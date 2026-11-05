package com.rebootech.fruitlogix.producer.logisticsMonitoring

object ProducerLogisticsMonitoringRoutes {
    const val LoadTruck = "logistics_load/{orderId}"
    fun loadTruck(id: String) = "logistics_load/$id"
}

