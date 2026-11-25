package com.rebootech.fruitlogix.producer.qualityControl.reports.data

import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualitySummary
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.QualityInspectionDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.roundToInt
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus as OrderQualityStatus

/**
 * The producer's quality reports: the inspections saved on the server for the lots linked to
 * their orders (GET /quality-inspections/batch/{batchId}). There are no sample reports.
 * The status is the distributor's decision on the lot (Approved / Rejected / waiting).
 */
object QualityReportRepository {
    private const val CRATE_KG = 20

    val reports = mutableStateListOf<QualityReportItem>()

    /** Pass rate of decided lots, its change against the previous 7 days, and today's inspections. */
    val summary: QualitySummary
        get() {
            val today = LocalDate.now()
            fun passRate(items: List<QualityReportItem>): Double {
                val decided = items.filter { it.status != QualityStatus.PENDING }
                return if (decided.isEmpty()) 0.0 else decided.count { it.status == QualityStatus.APPROVED } * 100.0 / decided.size
            }
            val lastWeek = reports.filter { !it.inspectedAt.toLocalDate().isBefore(today.minusDays(6)) }
            val weekBefore = reports.filter {
                val day = it.inspectedAt.toLocalDate()
                !day.isBefore(today.minusDays(13)) && day.isBefore(today.minusDays(6))
            }
            val todays = reports.filter { it.inspectedAt.toLocalDate() == today }
            return QualitySummary(
                passRatePct = passRate(reports),
                weeklyDeltaPct = if (weekBefore.isEmpty()) 0.0 else passRate(lastWeek) - passRate(weekBefore),
                sampledTodayKg = todays.sumOf { it.weightKg },
                cratesInspectedToday = todays.sumOf { it.cratesCount }
            )
        }

    /** Reads the inspections of every order that has a lot. */
    suspend fun refresh() {
        val orders = FakeOrderRepository.instance.orderSnapshot().filter { it.batchId != null }
        val items = withContext(Dispatchers.IO) {
            orders.mapNotNull { order ->
                val batchId = order.batchId ?: return@mapNotNull null
                val list = (apiCall { RetrofitClient.api.getQualityInspectionsByBatch(batchId) } as? ApiResult.Ok)?.value
                list?.maxByOrNull { it.id ?: 0 }?.let { toReport(order, batchId, it) }
            }
        }
        reports.clear()
        reports.addAll(items.sortedByDescending { it.inspectedAt })
    }

    /** Adds or replaces the report of an order after the producer sends it (status PENDING). */
    fun submit(report: QualityReportItem) {
        val index = reports.indexOfFirst { it.id == report.id }
        if (index >= 0) reports[index] = report else reports.add(0, report)
    }

    private fun toReport(order: Order, batchId: Int, dto: QualityInspectionDto): QualityReportItem {
        val kg = order.totalKg
        return QualityReportItem(
            id = order.id,
            orderNumber = order.id,
            clientName = order.clientName,
            inspectedAt = parseDateTime(dto.createdAt) ?: LocalDateTime.now(),
            locationInfo = "",
            status = when (order.qualityStatus) {
                OrderQualityStatus.APPROVED -> QualityStatus.APPROVED
                OrderQualityStatus.REJECTED -> QualityStatus.REJECTED
                else -> QualityStatus.PENDING
            },
            productName = order.items.firstOrNull()?.productName.orEmpty(),
            weightKg = kg,
            cratesCount = ceil(kg / CRATE_KG.toDouble()).toInt(),
            lotCode = "LOT-$batchId",
            visualScore = (dto.appearanceRating ?: 0).toDouble(),
            defectsPercentage = dto.wastePercentage ?: 0.0,
            coreTemp = dto.temperatureCelsius ?: 0.0,
            humidityPercentage = (dto.humidityPercent ?: 0.0).roundToInt(),
            inspectorName = dto.inspectorName.orEmpty(),
            inspectorNotes = dto.notes?.takeIf { it.isNotBlank() }
        )
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
    }
}
