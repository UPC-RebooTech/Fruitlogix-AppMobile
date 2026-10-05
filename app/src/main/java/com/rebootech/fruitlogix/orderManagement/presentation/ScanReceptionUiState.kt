package com.rebootech.fruitlogix.orderManagement.presentation

import com.rebootech.fruitlogix.orderManagement.domain.model.LotScanResult

data class ScanReceptionUiState(
    val manualCode: String = "",
    val scanResult: LotScanResult? = null,
    val showManualEntry: Boolean = false
)