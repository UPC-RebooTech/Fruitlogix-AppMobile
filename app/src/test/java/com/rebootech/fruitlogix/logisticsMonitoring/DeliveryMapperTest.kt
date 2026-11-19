package com.rebootech.fruitlogix.logisticsMonitoring

import com.rebootech.fruitlogix.distributor.logisticsMonitoring.data.DeliveryMapper
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.CheckpointKind
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.DeliveryState
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.LotStageType
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.LotTraceability
import com.rebootech.fruitlogix.shared.core.network.DeliveryResponseDto
import com.rebootech.fruitlogix.shared.core.network.TrackingLogResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DeliveryMapperTest {

    private val now = LocalDateTime.of(2026, 11, 13, 10, 0)

    private val serverDelivery = DeliveryResponseDto(
        id = 12, orderId = 45, driverName = "Pedro Salazar", driverPhone = "+51 933 210 456",
        vehiclePlate = "HJK-264", vehicleType = "HEAVY_REEFER", routeOrigin = "Lima Central Hub",
        routeDestination = "Mercados Lima Sur", routeDistanceKm = 380.0,
        estimatedTimeOfArrival = "2026-11-13T15:36:00", currentStatus = "InTransit"
    )

    @Test
    fun serverStatus_isTranslated() {
        assertEquals(DeliveryState.PENDING, DeliveryMapper.state("PendingDispatch"))
        assertEquals(DeliveryState.ON_ROUTE, DeliveryMapper.state("InTransit"))
        assertEquals(DeliveryState.ON_ROUTE, DeliveryMapper.state("Delayed"))
        assertEquals(DeliveryState.DELIVERED, DeliveryMapper.state("Delivered"))
        assertEquals(DeliveryState.REJECTED, DeliveryMapper.state("Failed"))
    }

    @Test
    fun serverDelivery_becomesATrip() {
        val log = TrackingLogResponseDto(deliveryId = 12, latitude = -12.0464, longitude = -77.0428)
        val trip = DeliveryMapper.toDispatch(serverDelivery, "FX-1045", "Fresa", 14, log, previous = null, now = now)
        assertEquals("D-12", trip.unitId)
        assertEquals(12, trip.remoteId)
        assertEquals("HJK-264", trip.plate)
        assertEquals("Pedro Salazar", trip.driver.name)
        assertEquals("FX-1045", trip.orderId)
        assertEquals(380.0, trip.totalKm, 0.0)
        assertEquals(LocalTime.of(15, 36), trip.eta)
        assertEquals(DeliveryState.ON_ROUTE, trip.deliveryState)
        assertEquals(listOf(CheckpointKind.ORIGIN, CheckpointKind.CURRENT, CheckpointKind.DESTINATION), trip.checkpoints.map { it.kind })
        assertEquals("-12.0464, -77.0428", trip.checkpoints[1].name)
    }

    @Test
    fun unknownLotTimes_areNotInvented() {
        val trip = DeliveryMapper.toDispatch(serverDelivery, "FX-1045", "Fresa", 14, null, previous = null, now = now)
        assertNull(trip.harvestedAt)
        val stages = LotTraceability.stages(trip).associateBy { it.type }
        assertFalse(stages.getValue(LotStageType.HARVESTED).done)
        assertTrue(stages.getValue(LotStageType.LOADED).done)
    }

    @Test
    fun delayed_countsMinutesOnlyAfterTheEta() {
        val late = serverDelivery.copy(currentStatus = "Delayed", estimatedTimeOfArrival = "2026-11-13T09:15:00")
        assertEquals(45, DeliveryMapper.toDispatch(late, "FX-1045", "Fresa", 14, null, null, now).delayMinutes)
        val stillOnTime = serverDelivery.copy(currentStatus = "Delayed")
        assertEquals(0, DeliveryMapper.toDispatch(stillOnTime, "FX-1045", "Fresa", 14, null, null, now).delayMinutes)
    }

    @Test
    fun deliveredOnThisPhone_isKeptUntilTheServerKnows() {
        val first = DeliveryMapper.toDispatch(serverDelivery, "FX-1045", "Fresa", 14, null, null, now)
        val delivered = first.copy(deliveryState = DeliveryState.DELIVERED, deliveredAt = now)
        val again = DeliveryMapper.toDispatch(serverDelivery, "FX-1045", "Fresa", 14, null, delivered, now)
        assertEquals(DeliveryState.DELIVERED, again.deliveryState)
        assertEquals(now, again.deliveredAt)
    }

    @Test
    fun dateTimes_withOrWithoutOffset() {
        assertEquals(LocalDateTime.of(2026, 11, 13, 15, 36), DeliveryMapper.parseDateTime("2026-11-13T15:36:00"))
        assertEquals(LocalDateTime.of(2026, 11, 13, 15, 36, 0, 500_000_000), DeliveryMapper.parseDateTime("2026-11-13T15:36:00.5"))
        assertEquals(
            LocalDateTime.of(2026, 11, 13, 10, 36),
            DeliveryMapper.parseDateTime("2026-11-13T15:36:00Z", ZoneId.of("America/Lima"))
        )
        assertNull(DeliveryMapper.parseDateTime("mañana"))
    }
}
