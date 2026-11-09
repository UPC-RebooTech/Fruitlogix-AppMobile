package com.rebootech.fruitlogix.producer.qualityControl

object ProducerQualityControlRoutes {
    const val Reports = "quality_reports_main"                  // <--- Añadimos esta constante
    const val PrepareOrder = "quality_prepare/{orderId}"
    const val ScanLot = "quality_scan/{orderId}"
    const val ScanLotAny = "quality_scan_any"                   // desde Home: resuelve lote → pedido
    const val Inspection = "quality_inspection/{orderId}"
    const val SendReport = "quality_send/{orderId}"
    const val ReviewResult = "quality_result/{orderId}"
    const val ReportDetail = "quality_report/{reportId}"
    const val StockDetail = "quality_stock/{productId}"         // stock
    const val Harvests = "quality_harvests"                     // harvests
    const val RegisterHarvest = "quality_harvest_register"      // harvests

    fun reports() = "quality_reports_main"                      // <--- Función helper
    fun prepareOrder(id: String) = "quality_prepare/$id"
    fun scanLot(id: String) = "quality_scan/$id"
    fun inspection(id: String) = "quality_inspection/$id"
    fun sendReport(id: String) = "quality_send/$id"
    fun reviewResult(id: String) = "quality_result/$id"
    fun reportDetail(id: String) = "quality_report/$id"
    fun stockDetail(id: String) = "quality_stock/$id"
}