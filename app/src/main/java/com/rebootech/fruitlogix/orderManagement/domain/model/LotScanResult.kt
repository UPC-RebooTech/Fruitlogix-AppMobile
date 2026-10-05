package com.rebootech.fruitlogix.orderManagement.domain.model

data class LotScanResult(
    val scannedCode: String,
    val status: LotScanStatus,
    val orderId: String? = null,
    val productName: String? = null,
    val quantityLabel: String? = null
)