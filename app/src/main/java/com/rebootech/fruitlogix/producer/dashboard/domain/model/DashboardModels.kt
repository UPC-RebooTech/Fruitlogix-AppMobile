package com.rebootech.fruitlogix.producer.dashboard.domain.model

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * A task shown in "Today's tasks". The domain holds only data (IDs, names, quantities);
 * titles and descriptions are built from string resources in the UI, so they follow
 * the selected language.
 */
sealed class ProducerTask {
    abstract val id: String
    abstract val priority: TaskPriority
    abstract val destinationRoute: String

    /** New order waiting to be accepted. */
    data class NewOrder(
        override val id: String,
        override val destinationRoute: String,
        val orderId: String,
        val clientName: String,
        val itemsSummary: String,
        override val priority: TaskPriority = TaskPriority.MEDIUM
    ) : ProducerTask()

    /** Quality report rejected; the lot must be re-inspected. */
    data class QualityRejected(
        override val id: String,
        override val destinationRoute: String,
        val orderId: String,
        val reason: String?,
        override val priority: TaskPriority = TaskPriority.HIGH
    ) : ProducerTask()

    /** Order in preparation without a quality report yet. */
    data class InspectionPending(
        override val id: String,
        override val destinationRoute: String,
        val orderId: String,
        val clientName: String,
        val itemsSummary: String,
        override val priority: TaskPriority = TaskPriority.MEDIUM
    ) : ProducerTask()

    /** Truck assigned and ready to load. */
    data class TruckAssigned(
        override val id: String,
        override val destinationRoute: String,
        val orderId: String,
        val truckPlate: String,
        val eta: String,
        override val priority: TaskPriority = TaskPriority.LOW
    ) : ProducerTask()

    /** Product below its minimum stock threshold. */
    data class CriticalStock(
        override val id: String,
        override val destinationRoute: String,
        val productName: String,
        val availableKg: Int,
        val minThresholdKg: Int,
        override val priority: TaskPriority = TaskPriority.HIGH
    ) : ProducerTask()
}

data class PipelineCount(
    val pending: Int,
    val inPrep: Int,
    val inTransit: Int,
    val delivered: Int
) {
    val totalActive: Int get() = pending + inPrep + inTransit
}

data class DashboardSummary(
    val pendingOrdersCount: Int,
    val lotsDispatchedThisMonth: Int,
    val qualityApprovalPct: Int,
    val pendingPaymentsAmount: Double,
    val paidPaymentsAmount: Double
)
