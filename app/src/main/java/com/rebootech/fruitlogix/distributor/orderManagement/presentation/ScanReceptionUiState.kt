package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult

data class ScanReceptionUiState(
    val manualCode: String = "",
    val scanResult: LotScanResult? = null,
    val showManualEntry: Boolean = false
)