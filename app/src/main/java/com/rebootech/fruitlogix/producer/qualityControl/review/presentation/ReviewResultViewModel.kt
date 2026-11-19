package com.rebootech.fruitlogix.producer.qualityControl.review.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.FakeInspectionRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionRules
import com.rebootech.fruitlogix.producer.qualityControl.reports.data.QualityReportRepository
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class ReviewUiState(
    val lot: InspectionLot? = null,
    val draft: InspectionDraft? = null,
    val distributorName: String? = null,
    val isSending: Boolean = false,
    val sentAt: LocalDateTime? = null
) {
    val isSent: Boolean get() = sentAt != null
}

/**
 * Step 3 of the inspection: review the recorded data and send it to the distributor.
 * Sending marks the order's quality as "in review" and publishes the report in Reports.
 */
class ReviewResultViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val inspectionRepository = FakeInspectionRepository.instance
    private val orderRepository = FakeOrderRepository.instance
    private val orderId = savedStateHandle.get<String>("orderId").orEmpty().removePrefix("#")

    private val _uiState = MutableStateFlow(
        ReviewUiState(
            lot = inspectionRepository.lotForOrder(orderId),
            draft = inspectionRepository.draftFor(orderId)
        )
    )
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val order = orderRepository.observeOrders().first().find { it.id.removePrefix("#") == orderId }
            _uiState.update { it.copy(distributorName = order?.distributorName) }
        }
    }

    fun send() {
        val state = _uiState.value
        val lot = state.lot ?: return
        val draft = state.draft ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            val now = LocalDateTime.now()
            orderRepository.submitQualityReport(orderId)
            QualityReportRepository.submit(
                QualityReportItem(
                    id = lot.orderId,
                    orderNumber = "#${lot.orderId}",
                    clientName = lot.clientName,
                    inspectedAt = now,
                    locationInfo = "",
                    status = QualityStatus.PENDING,
                    productName = lot.productName,
                    weightKg = lot.netWeightKg,
                    cratesCount = lot.crates,
                    lotCode = lot.lotCode,
                    visualScore = draft.visualScore.toDouble(),
                    defectsPercentage = InspectionRules.parse(draft.defects) ?: 0.0,
                    coreTemp = InspectionRules.parse(draft.coreTemp) ?: 0.0,
                    humidityPercentage = (InspectionRules.parse(draft.moisture) ?: 0.0).toInt(),
                    // The producer inspects their own lot
                    inspectorName = FakeProfileRepository.instance.profile.value.fullName,
                    inspectorNotes = draft.notes.ifBlank { null }
                )
            )
            _uiState.update { it.copy(isSending = false, sentAt = now) }
        }
    }
}
