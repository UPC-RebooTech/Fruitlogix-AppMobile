package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ColdChainCertificate
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryProof
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.repository.FleetRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ColdChainCertificateBuilder
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.DeliveryProofRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.DeliveryStateMachine
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.EtaRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ReeferRules
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class ChartWindow(val minutes: Int?) { ONE_HOUR(60), SIX_HOURS(360), TRIP(null) }

/** The action sheet that is open, if any. */
enum class DispatchSheet { STATUS, REEFER, DELAY, DELIVERY, CERTIFICATE, TRACEABILITY }

data class DispatchDetailUiState(
    val item: DispatchItem? = null,
    val history: List<TemperatureReading> = emptyList(),
    val window: ChartWindow = ChartWindow.SIX_HOURS,
    val sheet: DispatchSheet? = null,
    /** Error shown inside the open sheet. */
    @field:StringRes val sheetError: Int? = null,
    val sheetErrorArg: String? = null,
    /** One-off confirmation shown in a snackbar. */
    val message: UiText? = null
) {
    val chartReadings: List<TemperatureReading>
        get() = window.minutes?.let { limit -> history.filter { it.minutesAgo <= limit } } ?: history

    val certificate: ColdChainCertificate?
        get() {
            val item = item ?: return null
            val t = item.threshold ?: return null
            return ColdChainCertificateBuilder.build(item.dispatch, history, t.minTempCelsius, t.maxTempCelsius)
        }
}

/**
 * Dispatch detail. Every rule (status steps, ETA, reefer range, delivery proof) is in domain;
 * this ViewModel only calls the rules and saves the result in the repository, so the state is
 * still there after leaving the screen.
 */
class DispatchDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val fleet: FleetRepository,
    private val iot: IotRepository
) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor(savedStateHandle: SavedStateHandle) :
        this(savedStateHandle, DistributorDependencies.fleetRepository, DistributorDependencies.iotRepository)

    private val unitId: String = savedStateHandle.get<String>("id").orEmpty()
    private val local = MutableStateFlow(DispatchDetailUiState())

    val uiState: StateFlow<DispatchDetailUiState> =
        combine(fleet.dispatches, iot.devices, iot.thresholds, local) { dispatches, _, _, local ->
            val dispatch = dispatches.firstOrNull { it.unitId.equals(unitId, ignoreCase = true) }
            local.copy(
                item = dispatch?.let { buildDispatchItem(it, iot) },
                history = iot.temperatureHistory(unitId)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DispatchDetailUiState())

    private val time: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    fun onWindowChange(value: ChartWindow) = local.update { it.copy(window = value) }

    fun openSheet(sheet: DispatchSheet) = local.update { it.copy(sheet = sheet, sheetError = null, sheetErrorArg = null) }

    fun closeSheet() = local.update { it.copy(sheet = null, sheetError = null, sheetErrorArg = null) }

    fun onMessageShown() = local.update { it.copy(message = null) }

    private fun sheetError(@StringRes res: Int, arg: String? = null) =
        local.update { it.copy(sheetError = res, sheetErrorArg = arg) }

    private fun done(message: UiText) = local.update { it.copy(sheet = null, sheetError = null, sheetErrorArg = null, message = message) }

    // US14 -------------------------------------------------------------------

    fun changeState(target: DeliveryState) {
        val current = fleet.dispatch(unitId)?.deliveryState ?: return
        when (DeliveryStateMachine.check(current, target)) {
            DeliveryStateMachine.Result.Allowed -> viewModelScope.launch {
                fleet.updateDeliveryState(unitId, target)
                done(UiText.StringResource(R.string.status_updated, arrayOf(UiText.StringResource(deliveryStateLabel(target)))))
            }
            DeliveryStateMachine.Result.NoChange -> closeSheet()
            DeliveryStateMachine.Result.CannotSkipOnRoute -> sheetError(R.string.status_error_skip)
            DeliveryStateMachine.Result.AlreadyClosed -> sheetError(R.string.status_error_closed)
            // Delivered needs the proof: open the delivery sheet instead
            DeliveryStateMachine.Result.RequiresDeliveryProof -> openSheet(DispatchSheet.DELIVERY)
        }
    }

    // US64 -------------------------------------------------------------------

    /** @return the check result for the typed reading, to show it live in the sheet. */
    fun reeferStatus(reading: Double?): ReeferRules.Status? {
        val t = uiState.value.item?.threshold ?: return null
        return reading?.let { ReeferRules.check(it, t.minTempCelsius, t.maxTempCelsius) }
    }

    fun approveReefer(reading: Double?) {
        if (reeferStatus(reading) != ReeferRules.Status.WITHIN_RANGE) return
        viewModelScope.launch {
            fleet.recordReeferCheck(unitId)
            done(UiText.StringResource(R.string.reefer_approved, arrayOf(LocalTime.now().format(time))))
        }
    }

    // US17 -------------------------------------------------------------------

    fun notifyDelay(newEta: LocalTime?, reason: DelayReason?) {
        val dispatch = fleet.dispatch(unitId) ?: return
        when (EtaRules.validate(dispatch.eta, newEta, reason)) {
            EtaRules.Error.NEW_ETA_REQUIRED -> sheetError(R.string.delay_error_required)
            EtaRules.Error.NOT_LATER_THAN_CURRENT -> sheetError(R.string.delay_error_not_later, dispatch.eta.format(time))
            EtaRules.Error.REASON_REQUIRED -> sheetError(R.string.delay_error_reason)
            null -> viewModelScope.launch {
                // TODO(backend): send the push notification to the client
                fleet.updateEta(unitId, newEta!!, reason!!)
                done(UiText.StringResource(R.string.delay_sent, arrayOf(newEta.format(time))))
            }
        }
    }

    // US16 / US52 / US56 -----------------------------------------------------

    fun confirmDelivery(proof: DeliveryProof) {
        val dispatch = fleet.dispatch(unitId) ?: return
        if (DeliveryStateMachine.check(dispatch.deliveryState, DeliveryState.DELIVERED) == DeliveryStateMachine.Result.CannotSkipOnRoute) {
            sheetError(R.string.status_error_skip)
            return
        }
        when (DeliveryProofRules.validate(proof)) {
            DeliveryProofRules.Error.MISSING_SIGNATURE -> sheetError(R.string.delivery_error_signature)
            DeliveryProofRules.Error.MISSING_PHOTO -> sheetError(R.string.delivery_error_photo)
            DeliveryProofRules.Error.LOCATION_NOT_CONFIRMED -> sheetError(R.string.delivery_error_location)
            else -> viewModelScope.launch {
                fleet.completeDelivery(unitId)
                done(UiText.StringResource(R.string.delivery_done, arrayOf(dispatch.orderId)))
            }
        }
    }

    fun rejectDelivery(reason: String) {
        if (DeliveryProofRules.validateRejection(reason) != null) {
            sheetError(R.string.delivery_error_reason)
            return
        }
        viewModelScope.launch {
            fleet.rejectDelivery(unitId, reason)
            done(UiText.StringResource(R.string.delivery_rejected))
        }
    }
}
