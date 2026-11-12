package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.repository.IotRepository
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlert
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlertStatus
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ReeferRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.service.PredictTemperatureBreachUseCase
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal

/** How the reefer is doing, combining the IoT reading with the fruit's limits. */
enum class ReeferRisk { OK, RISK, OUT_OF_RANGE }

/**
 * A trip as shown on screen: the logistics dispatch plus its sensor reading and the
 * fruit limits, both read from IoT through its domain interface.
 */
data class DispatchItem(
    val dispatch: ActiveDispatch,
    val device: IotDevice?,
    val threshold: FruitThreshold?,
    val prediction: PredictiveAlert?
) {
    val celsius: Double? get() = device?.lastReading?.celsius

    val risk: ReeferRisk
        get() {
            val t = celsius ?: return ReeferRisk.OK
            val range = threshold ?: return ReeferRisk.OK
            return when {
                ReeferRules.check(t, range.minTempCelsius, range.maxTempCelsius) != ReeferRules.Status.WITHIN_RANGE -> ReeferRisk.OUT_OF_RANGE
                prediction?.status == PredictiveAlertStatus.PREDICTIVE -> ReeferRisk.RISK
                else -> ReeferRisk.OK
            }
        }

    /** Riskiest first, then delayed, then by distance left. */
    val sortKey: Int get() = when {
        risk == ReeferRisk.OUT_OF_RANGE -> 0
        risk == ReeferRisk.RISK -> 1
        dispatch.isDelayed -> 2
        else -> 3
    }
}

/** Minutes of readings used to predict a breach. */
private const val PREDICTION_WINDOW_MINUTES = 45

fun buildDispatchItem(
    dispatch: ActiveDispatch,
    iot: IotRepository,
    predictor: PredictTemperatureBreachUseCase = PredictTemperatureBreachUseCase()
): DispatchItem {
    val threshold = iot.thresholdFor(dispatch.productName)
    val recent = iot.temperatureHistory(dispatch.unitId).filter { it.minutesAgo <= PREDICTION_WINDOW_MINUTES }
    val prediction = threshold?.let {
        predictor.execute(recent, it.maxTempCelsius, dispatch.unitId, "${dispatch.productName} · ${dispatch.originName}")
    }
    return DispatchItem(dispatch, iot.deviceForUnit(dispatch.unitId), threshold, prediction)
}

// Labels ---------------------------------------------------------------------

@Composable
fun celsius(value: Double): String = "${formatDecimal(value)} °C"

@Composable
fun rangeText(threshold: FruitThreshold): String =
    stringResource(R.string.sensors_temp_range, celsius(threshold.minTempCelsius), celsius(threshold.maxTempCelsius))

/** Status badge of a trip: temperature first (it can ruin the cargo), then delay. */
@Composable
fun dispatchBadge(item: DispatchItem): Pair<String, StatusBadgeType> = when {
    item.dispatch.deliveryState == DeliveryState.PENDING -> stringResource(R.string.fleet_status_pending) to StatusBadgeType.NEUTRAL
    item.risk == ReeferRisk.OUT_OF_RANGE -> stringResource(R.string.fleet_status_temp_out) to StatusBadgeType.DANGER
    item.risk == ReeferRisk.RISK -> stringResource(R.string.fleet_status_temp_risk) to StatusBadgeType.DANGER
    item.dispatch.isDelayed -> stringResource(R.string.fleet_status_delayed) to StatusBadgeType.WARNING
    else -> stringResource(R.string.fleet_status_on_time) to StatusBadgeType.SUCCESS
}

@StringRes
fun deliveryStateLabel(state: DeliveryState): Int = when (state) {
    DeliveryState.PENDING -> R.string.delivery_state_pending
    DeliveryState.ON_ROUTE -> R.string.delivery_state_on_route
    DeliveryState.DELIVERED -> R.string.delivery_state_delivered
    DeliveryState.REJECTED -> R.string.delivery_state_rejected
}

@StringRes
fun delayReasonLabel(reason: DelayReason): Int = when (reason) {
    DelayReason.TRAFFIC -> R.string.delay_reason_traffic
    DelayReason.WEIGH_STATION -> R.string.delay_reason_weigh_station
    DelayReason.ROAD_WORKS -> R.string.delay_reason_road_works
    DelayReason.WEATHER -> R.string.delay_reason_weather
    DelayReason.MECHANICAL -> R.string.delay_reason_mechanical
    DelayReason.OTHER -> R.string.delay_reason_other
}
