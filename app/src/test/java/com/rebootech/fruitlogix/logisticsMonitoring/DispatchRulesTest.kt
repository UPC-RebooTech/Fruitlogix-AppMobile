package com.rebootech.fruitlogix.logisticsMonitoring

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.ActiveDispatch
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DelayReason
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryProof
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DriverInfo
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.LotStageType
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ArrivalRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ColdChainCertificateBuilder
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.DeliveryProofRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.DeliveryStateMachine
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.EtaRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.LotTraceability
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ReeferRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class DispatchRulesTest {

    private val departed = LocalDateTime.of(2026, 11, 11, 8, 0)

    private fun dispatch(
        state: DeliveryState = DeliveryState.ON_ROUTE,
        travelled: Double = 182.0,
        eta: LocalTime = LocalTime.of(14, 30),
        delay: Int = 0
    ) = ActiveDispatch(
        unitId = "FL-408", plate = "C7X-915", truckModel = "International LT",
        driver = DriverInfo("Mateo Silva", "+51 956 410 228", "Q-41872093"),
        orderId = "FX-1042", productName = "Palta Hass", pallets = 18, producerName = "Fundo Santa Rosa",
        originName = "Chavín de Huántar", destinationName = "Lima Central Hub", dock = "A",
        sealId = "PE-99410-X", sensorId = "TH-408", totalKm = 223.0, travelledKm = travelled,
        eta = eta, delayMinutes = delay, deliveryState = state,
        harvestedAt = departed.minusHours(26), inspectedAt = departed.minusHours(24), departedAt = departed,
        checkpoints = emptyList()
    )

    // US14: order status -------------------------------------------------------

    @Test
    fun pendingToOnRoute_isAllowed() {
        assertEquals(DeliveryStateMachine.Result.Allowed, DeliveryStateMachine.check(DeliveryState.PENDING, DeliveryState.ON_ROUTE))
    }

    @Test
    fun onRouteBackToPending_isAllowed() {
        assertEquals(DeliveryStateMachine.Result.Allowed, DeliveryStateMachine.check(DeliveryState.ON_ROUTE, DeliveryState.PENDING))
    }

    @Test
    fun pendingToDelivered_cannotSkipOnRoute() {
        assertEquals(
            DeliveryStateMachine.Result.CannotSkipOnRoute,
            DeliveryStateMachine.check(DeliveryState.PENDING, DeliveryState.DELIVERED)
        )
    }

    @Test
    fun onRouteToDelivered_requiresProof() {
        assertEquals(
            DeliveryStateMachine.Result.RequiresDeliveryProof,
            DeliveryStateMachine.check(DeliveryState.ON_ROUTE, DeliveryState.DELIVERED)
        )
    }

    @Test
    fun deliveredOrder_isClosed() {
        assertEquals(DeliveryStateMachine.Result.AlreadyClosed, DeliveryStateMachine.check(DeliveryState.DELIVERED, DeliveryState.ON_ROUTE))
        assertEquals(DeliveryStateMachine.Result.AlreadyClosed, DeliveryStateMachine.check(DeliveryState.REJECTED, DeliveryState.PENDING))
    }

    @Test
    fun sameState_isNoChange() {
        assertEquals(DeliveryStateMachine.Result.NoChange, DeliveryStateMachine.check(DeliveryState.ON_ROUTE, DeliveryState.ON_ROUTE))
    }

    // US17: new ETA -------------------------------------------------------------

    @Test
    fun newEta_isRequired() {
        assertEquals(EtaRules.Error.NEW_ETA_REQUIRED, EtaRules.validate(LocalTime.of(14, 30), null, DelayReason.TRAFFIC))
    }

    @Test
    fun newEta_mustBeLaterThanCurrent() {
        assertEquals(EtaRules.Error.NOT_LATER_THAN_CURRENT, EtaRules.validate(LocalTime.of(14, 30), LocalTime.of(14, 30), DelayReason.TRAFFIC))
        assertEquals(EtaRules.Error.NOT_LATER_THAN_CURRENT, EtaRules.validate(LocalTime.of(14, 30), LocalTime.of(13, 0), DelayReason.TRAFFIC))
    }

    @Test
    fun newEta_needsReason() {
        assertEquals(EtaRules.Error.REASON_REQUIRED, EtaRules.validate(LocalTime.of(14, 30), LocalTime.of(15, 0), null))
    }

    @Test
    fun validEta_addsUpTheDelay() {
        assertNull(EtaRules.validate(LocalTime.of(14, 30), LocalTime.of(15, 15), DelayReason.TRAFFIC))
        val updated = EtaRules.apply(dispatch(delay = 10), LocalTime.of(15, 15), DelayReason.TRAFFIC)
        assertEquals(LocalTime.of(15, 15), updated.eta)
        assertEquals(55, updated.delayMinutes)
        assertEquals(DelayReason.TRAFFIC, updated.delayReason)
    }

    // US64: reefer check --------------------------------------------------------

    @Test
    fun reefer_insideRange_isApproved() {
        assertEquals(ReeferRules.Status.WITHIN_RANGE, ReeferRules.check(6.4, 5.0, 7.0))
        assertEquals(ReeferRules.Status.WITHIN_RANGE, ReeferRules.check(7.0, 5.0, 7.0))
    }

    @Test
    fun reefer_outsideRange_isBlocked() {
        assertEquals(ReeferRules.Status.TOO_WARM, ReeferRules.check(7.1, 5.0, 7.0))
        assertEquals(ReeferRules.Status.TOO_COLD, ReeferRules.check(4.9, 5.0, 7.0))
    }

    // US16 / US52 / US56: delivery proof ----------------------------------------

    @Test
    fun delivery_needsSignature_thenPhoto() {
        assertEquals(
            DeliveryProofRules.Error.MISSING_SIGNATURE,
            DeliveryProofRules.validate(DeliveryProof(hasSignature = false, hasPhoto = true, insideDeliveryRadius = true))
        )
        assertEquals(
            DeliveryProofRules.Error.MISSING_PHOTO,
            DeliveryProofRules.validate(DeliveryProof(hasSignature = true, hasPhoto = false, insideDeliveryRadius = true))
        )
    }

    @Test
    fun delivery_outsideRadius_needsManualConfirmation() {
        val outside = DeliveryProof(hasSignature = true, hasPhoto = true, insideDeliveryRadius = false)
        assertEquals(DeliveryProofRules.Error.LOCATION_NOT_CONFIRMED, DeliveryProofRules.validate(outside))
        assertNull(DeliveryProofRules.validate(outside.copy(outsideLocationConfirmed = true)))
    }

    @Test
    fun rejection_needsReason() {
        assertEquals(DeliveryProofRules.Error.REJECTION_REASON_REQUIRED, DeliveryProofRules.validateRejection("  "))
        assertNull(DeliveryProofRules.validateRejection("Fruta golpeada"))
    }

    // US71: cold-chain certificate ----------------------------------------------

    @Test
    fun certificate_countsExcursions_andHashIsStable() {
        val readings = listOf(TemperatureReading(30, 5.5), TemperatureReading(20, 7.4), TemperatureReading(0, 6.4))
        val first = ColdChainCertificateBuilder.build(dispatch(), readings, 5.0, 7.0)
        val second = ColdChainCertificateBuilder.build(dispatch(), readings, 5.0, 7.0)
        assertNotNull(first)
        assertEquals(3, first!!.readingsCount)
        assertEquals(1, first.excursions)
        assertFalse(first.isCompliant)
        assertEquals(24, first.hash.length)
        assertEquals(first.hash, second!!.hash)
    }

    @Test
    fun certificate_withoutReadings_isNotIssued() {
        assertNull(ColdChainCertificateBuilder.build(dispatch(), emptyList(), 5.0, 7.0))
    }

    // US72: lot traceability ----------------------------------------------------

    @Test
    fun traceability_onRoute_hasReceiveAndInvoicePending() {
        val stages = LotTraceability.stages(dispatch())
        assertEquals(LotStageType.entries, stages.map { it.type })
        assertTrue(stages.take(3).all { it.done })
        assertTrue(stages.drop(3).none { it.done })
    }

    @Test
    fun traceability_delivered_isComplete() {
        val stages = LotTraceability.stages(dispatch(state = DeliveryState.DELIVERED).copy(deliveredAt = departed.plusHours(6)))
        assertTrue(stages.all { it.done })
    }

    // Arrivals ----------------------------------------------------------------

    @Test
    fun arrivals_onlyTrucksCloseToTheHub() {
        assertTrue(ArrivalRules.isArriving(dispatch(travelled = 182.0)))   // 41 km left
        assertFalse(ArrivalRules.isArriving(dispatch(travelled = 100.0)))  // 123 km left
        assertTrue(ArrivalRules.isAtGate(dispatch(travelled = 222.7)))
        assertFalse(ArrivalRules.isArriving(dispatch(state = DeliveryState.DELIVERED)))
    }
}
