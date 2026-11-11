package com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model

import java.time.LocalTime

/** Truck sent by the distributor to pick up the order at the farm. */
data class PickupTruck(
    val plate: String,
    val vehicleType: String,
    val driverName: String,
    val driverPhone: String,
    val etaFormatted: String?
)

data class LoadItem(
    val productName: String,
    val kg: Int
)

/** Why an order cannot be loaded yet. */
enum class LoadBlocker { QUALITY_NOT_APPROVED, NO_TRUCK_ASSIGNED }

/** Checks the producer confirms before the truck leaves the farm. */
enum class LoadCheck { CRATES_COUNTED, LOT_LABELS, TRUCK_COOLING, GUIDE_SIGNED }

/**
 * An order seen from logistics: what goes on the truck and who picks it up.
 */
data class TruckLoad(
    val orderId: String,
    val clientName: String,
    val distributorName: String,
    val items: List<LoadItem>,
    val truck: PickupTruck?,
    val pickupFrom: LocalTime?,
    val pickupTo: LocalTime?,
    val coldChainMinC: Double?,
    val coldChainMaxC: Double?,
    val qualityApproved: Boolean,
    val isDispatched: Boolean
) {
    val totalKg: Int get() = items.sumOf { it.kg }

    val blockers: List<LoadBlocker>
        get() = buildList {
            if (!qualityApproved) add(LoadBlocker.QUALITY_NOT_APPROVED)
            if (truck == null) add(LoadBlocker.NO_TRUCK_ASSIGNED)
        }

    val canLoad: Boolean get() = !isDispatched && blockers.isEmpty()
}

object LoadRules {
    /** The dispatch can only be confirmed once every check is done. */
    fun canConfirm(load: TruckLoad, checked: Set<LoadCheck>): Boolean =
        load.canLoad && checked.containsAll(LoadCheck.entries)
}
