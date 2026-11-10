package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType

/**
 * Maps a nullable [QualityStatus] to a [StatusBadgeType] + string resource pair.
 *
 * Semantics:
 *  null     → "QA: Not started"   (no report submitted yet)
 *  PENDING  → "Quality in review" (report submitted, awaiting distributor decision)
 *  APPROVED → "Quality Approved"
 *  REJECTED → "QA: Rejected"
 *
 * Lives in orderManagement/presentation/components because it depends on QualityStatus,
 * which is an order-management domain type.
 */
data class QualityBadgeConfig(
    val badgeType: StatusBadgeType,
    val labelRes: Int
)

fun qualityBadgeConfig(qualityStatus: QualityStatus?): QualityBadgeConfig = when (qualityStatus) {
    null -> QualityBadgeConfig(
        badgeType = StatusBadgeType.NEUTRAL,
        labelRes = R.string.producer_orders_quality_not_started
    )
    QualityStatus.PENDING -> QualityBadgeConfig(
        badgeType = StatusBadgeType.WARNING,
        labelRes = R.string.producer_orders_quality_in_review
    )
    QualityStatus.APPROVED -> QualityBadgeConfig(
        badgeType = StatusBadgeType.SUCCESS,
        labelRes = R.string.producer_orders_quality_approved
    )
    QualityStatus.REJECTED -> QualityBadgeConfig(
        badgeType = StatusBadgeType.DANGER,
        labelRes = R.string.producer_orders_quality_rejected_badge
    )
}

