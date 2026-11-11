package com.rebootech.fruitlogix.producer.logisticsMonitoring

object ProducerLogisticsMonitoringRoutes {
    const val LoadTruck = "logistics_load/{orderId}"
    // Order ids come as "#1041": a "#" in a route would be read as a URL fragment
    fun loadTruck(id: String) = "logistics_load/${id.removePrefix("#")}"
}

