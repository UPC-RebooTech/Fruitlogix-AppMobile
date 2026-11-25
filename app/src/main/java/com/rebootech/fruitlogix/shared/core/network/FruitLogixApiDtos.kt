package com.rebootech.fruitlogix.shared.core.network

/*
 * DTOs added with the backend update (CAMBIOS-API.md, blocks P0–P2).
 * Statuses travel as text: they are kept as String and mapped by hand with a default case,
 * so a new value from the server never breaks the app.
 */

// ─── Orders ────────────────────────────────────────────────────────────────

/** reception inside OrderResource and GET /orders/{id}/reception. rating null = not rated. */
data class OrderReceptionDto(
    val orderId: Int? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val hasIncidence: Boolean? = null,
    val sealAndTemperatureOk: Boolean? = null,
    val receivedAt: String? = null
)

/** PATCH /orders/{id}/status: action "accept" or "reject" (reject needs a reason). */
data class UpdateOrderStatusRequestDto(val action: String, val reason: String? = null)

/** PATCH /orders/{id}/batch */
data class AssignOrderBatchRequestDto(val batchId: Int)

/** PATCH /orders/{id}/cancel */
data class CancelOrderRequestDto(val reason: String)

/** POST /orders/{id}/reception. rating null (or 0) = not rated. */
data class ConfirmReceptionRequestDto(
    val rating: Int? = null,
    val comment: String? = null,
    val hasIncidence: Boolean = false,
    val sealAndTemperatureOk: Boolean = true
)

// ─── Deliveries ────────────────────────────────────────────────────────────

/** POST /deliveries/{id}/arrive (the body may be empty). */
data class ArriveDeliveryRequestDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val note: String? = null
)

/** PATCH /deliveries/{id}/assignment: only the fields that change. */
data class UpdateDeliveryAssignmentRequestDto(
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehiclePlate: String? = null,
    val vehicleType: String? = null,
    val driverId: Int? = null,
    val vehicleId: Int? = null
)

/** POST /deliveries/{id}/checkpoints. Latitude and longitude go together or not at all. */
data class AddCheckpointRequestDto(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val note: String? = null,
    val occurredAt: String? = null
)

/** DeliveryEventResource. type: Created, Dispatched, Delayed, Checkpoint, Arrived, Delivered. */
data class DeliveryEventDto(
    val id: Int? = null,
    val deliveryId: Int? = null,
    val type: String? = null,
    val name: String? = null,
    val note: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val occurredAt: String? = null,
    val createdByUserId: Int? = null
)

/** POST /tracking-logs. Without GPS signal send 0,0 (the server ignores it). */
data class CreateTrackingLogRequestDto(
    val deliveryId: Int,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val humidity: Double
)

// ─── Products (catalog) ────────────────────────────────────────────────────

data class ProductDto(
    val id: Int,
    val producerId: Int? = null,
    val producerName: String? = null,
    val name: String? = null,
    val fruitType: String? = null,
    val variety: String? = null,
    val description: String? = null,
    /** Price per kg. */
    val unitPrice: Double? = null,
    val availableKg: Double? = null,
    val minOrderKg: Double? = null,
    val imageUrl: String? = null,
    val isActive: Boolean? = null,
    val isAvailable: Boolean? = null
)

/** POST /products (producerId only for Distributor/Admin) and PUT /products/{id} (with isActive). */
data class SaveProductRequestDto(
    val name: String,
    val fruitType: String,
    val unitPrice: Double,
    val availableKg: Double,
    val variety: String? = null,
    val description: String? = null,
    val minOrderKg: Double,
    val imageUrl: String? = null,
    val producerId: Int? = null,
    val isActive: Boolean? = null
)

// ─── Harvest batches and quality ───────────────────────────────────────────

/** HarvestBatchResource. status: Pending, Inspected, Approved, Rejected. */
data class HarvestBatchDto(
    val id: Int,
    val producerId: Int? = null,
    val fruitType: String? = null,
    val quantityKg: Double? = null,
    val harvestDate: String? = null,
    val status: String? = null
)

/** POST /harvest-batches. For a Producer the server takes producerId from the profile. */
data class CreateHarvestBatchRequestDto(
    val producerId: Int,
    val fruitType: String,
    val quantityKg: Double,
    val harvestDate: String
)

data class UpdateHarvestBatchStatusRequestDto(val status: String)

/** CreateQualityInspectionResource: flat fields. appearanceRating goes from 1 to 5. */
data class QualityInspectionRequestDto(
    val batchId: Int,
    val notes: String? = null,
    val appearanceRating: Int,
    val hasStains: Boolean,
    val hasBruises: Boolean,
    val hasDeformations: Boolean,
    val hasRot: Boolean,
    val wastePercentage: Double,
    val temperatureCelsius: Double,
    val humidityPercent: Double,
    val ph: Double,
    val brixDegrees: Double,
    val dryCleaningDone: Boolean,
    val caliberSortingConfirmed: Boolean,
    val packagingMaterialInspected: Boolean,
    val finalBoxSealing: Boolean,
    val inspectorName: String? = null
)

/** QualityInspectionResource (only the fields the app reads). */
data class QualityInspectionDto(
    val id: Int? = null,
    val batchId: Int? = null,
    val notes: String? = null,
    val appearanceRating: Int? = null,
    val wastePercentage: Double? = null,
    val temperatureCelsius: Double? = null,
    val humidityPercent: Double? = null,
    val ph: Double? = null,
    val brixDegrees: Double? = null,
    val inspectorName: String? = null,
    val createdAt: String? = null
)

// ─── Incidents, users ──────────────────────────────────────────────────────

/** IncidentResource. status: Open, InReview, Resolved. */
data class IncidentDto(
    val id: Int,
    val batchId: Int? = null,
    val description: String? = null,
    val evidenceUrls: List<String>? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val resolvedAt: String? = null
)

/** POST /incidents. batchId is required; orderId is optional (recommended). Up to 5 evidence URLs. */
data class CreateIncidentRequestDto(
    val batchId: Int,
    val description: String,
    val evidenceUrls: List<String>? = null,
    val orderId: Int? = null
)

/** UserResource: a Distributor sees every user; the others only themselves. */
data class UserDto(val id: Int, val username: String? = null)

// ─── Fleet (CAMBIOS-API 12.2–12.4) ─────────────────────────────────────────

/** DriverResource. licenseClass: A-IIb, A-IIIa, A-IIIb, A-IIIc. status: Available, OnRoute, OffDuty. */
data class DriverDto(
    val id: Int,
    val name: String? = null,
    val phone: String? = null,
    val licenseClass: String? = null,
    val licenseExpiry: String? = null,
    val coldChainCertified: Boolean? = null,
    val status: String? = null,
    val restUntil: String? = null
)

/** POST /drivers and PUT /drivers/{id} (PUT replaces every field; restUntil null clears it). */
data class SaveDriverRequestDto(
    val name: String,
    val phone: String?,
    val licenseClass: String,
    val licenseExpiry: String,
    val coldChainCertified: Boolean,
    val status: String?,
    val restUntil: String?
)

/** VehicleResource. type: HeavyReefer, ReeferTruck, MediumReeferVan, LightReefer. status: Available, OnRoute, Maintenance. */
data class VehicleDto(
    val id: Int,
    val unitCode: String? = null,
    val plate: String? = null,
    val type: String? = null,
    val capacityKg: Double? = null,
    val minCelsius: Double? = null,
    val maxCelsius: Double? = null,
    val status: String? = null
)

/** POST /vehicles and PUT /vehicles/{id}. Plate ABC-123, unique (409 when repeated). */
data class SaveVehicleRequestDto(
    val unitCode: String,
    val plate: String,
    val type: String,
    val capacityKg: Double,
    val minCelsius: Double,
    val maxCelsius: Double,
    val status: String?
)

// ─── Planned harvests (CAMBIOS-API 12.5) ───────────────────────────────────

/** HarvestPlanResource. scheduledFor may come in UTC. */
data class HarvestPlanDto(
    val id: Int,
    val producerId: Int? = null,
    val producerName: String? = null,
    val fruitType: String? = null,
    val scheduledFor: String? = null,
    val estimatedYieldKg: Double? = null,
    val zone: String? = null,
    val crewName: String? = null,
    val pickers: Int? = null,
    val notes: String? = null
)

/** POST /harvest-plans and PUT /harvest-plans/{id}. For a Producer the server ignores producerId. */
data class SaveHarvestPlanRequestDto(
    val fruitType: String,
    val scheduledFor: String,
    val estimatedYieldKg: Double,
    val zone: String,
    val crewName: String,
    val pickers: Int,
    val notes: String? = null,
    val producerId: Int? = null
)
