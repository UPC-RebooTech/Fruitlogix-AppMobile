package com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository

import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class HarvestItem(
    val fruitName: String,
    val dateTime: String,
    val weight: String,
    val lotCode: String,
    val crewOrTransport: String,
    val sectorOrVault: String
)

object HarvestRepository {
    // Lista centralizada en memoria compartida
    val recentHarvests = mutableStateListOf(
        HarvestItem(
            fruitName = "Red Globe Grapes",
            dateTime = "Today • 08:30 AM",
            weight = "120 kg",
            lotCode = "#RG-402",
            crewOrTransport = "Crew Alpha",
            sectorOrVault = "Vault ledger updated"
        ),
        HarvestItem(
            fruitName = "Hass Avocado",
            dateTime = "Oct 30 • 14:15 PM",
            weight = "320 kg",
            lotCode = "#HA-882",
            crewOrTransport = "Crew Delta",
            sectorOrVault = "Chamber 02-B"
        ),
        HarvestItem(
            fruitName = "Kent Mango",
            dateTime = "Oct 29 • 09:00 AM",
            weight = "280 kg",
            lotCode = "#KM-104",
            crewOrTransport = "Transport Cleared",
            sectorOrVault = "All weight verified"
        )
    )

    fun addHarvest(
        fruitName: String,
        weight: String,
        sector: String
    ) {
        val newItem = HarvestItem(
            fruitName = fruitName,
            dateTime = "Today • Just now",
            weight = "$weight kg",
            lotCode = "#H-2024-884",
            crewOrTransport = "Crew Alpha",
            sectorOrVault = sector
        )
        recentHarvests.add(0, newItem) // Añade el elemento al inicio de la lista
    }

    fun observeUpcomingHarvest(): Flow<PlannedHarvest?> {
        return flowOf(null)
    }
}