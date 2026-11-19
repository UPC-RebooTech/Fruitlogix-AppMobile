package com.rebootech.fruitlogix.qualityControl

import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.RejectionKind
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageCause
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageRecord
import com.rebootech.fruitlogix.distributor.qualityControl.domain.model.ShrinkageStage
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.FieldInspectionRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.RejectionRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.ShrinkageRules
import com.rebootech.fruitlogix.distributor.qualityControl.domain.rules.VoiceIncidentRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class QualityRulesTest {

    // US10 ----------------------------------------------------------------------

    @Test
    fun rejection_needsAReason() {
        assertEquals(RejectionRules.Error.REASON_REQUIRED, RejectionRules.validate(null, ""))
        assertNull(RejectionRules.validate(RejectionKind.BRUISED, ""))
    }

    @Test
    fun rejection_other_needsANote() {
        assertEquals(RejectionRules.Error.NOTE_REQUIRED, RejectionRules.validate(RejectionKind.OTHER, "  "))
        assertNull(RejectionRules.validate(RejectionKind.OTHER, "Cajas sin etiqueta"))
    }

    // US53 ----------------------------------------------------------------------

    @Test
    fun fieldInspection_valid() {
        assertTrue(FieldInspectionRules.validate("LOT-1", "Palta Hass", 6.0, 88.0).isEmpty())
    }

    @Test
    fun fieldInspection_reportsEveryWrongField() {
        val errors = FieldInspectionRules.validate("", " ", 45.0, 120.0)
        assertEquals(FieldInspectionRules.Error.entries.toSet(), errors)
    }

    @Test
    fun fieldInspection_temperatureLimitsAreInclusive() {
        assertTrue(FieldInspectionRules.validate("L", "P", 0.0, 50.0).isEmpty())
        assertTrue(FieldInspectionRules.validate("L", "P", 40.0, 50.0).isEmpty())
        assertEquals(setOf(FieldInspectionRules.Error.TEMP_INVALID), FieldInspectionRules.validate("L", "P", -0.5, 50.0))
    }

    // US57 ----------------------------------------------------------------------

    @Test
    fun voice_recordingUnderOneSecond_isDiscarded() {
        assertEquals(VoiceIncidentRules.Error.RECORDING_TOO_SHORT, VoiceIncidentRules.validate("LOT-1", "Cajas mojadas", 0))
        assertEquals(VoiceIncidentRules.Error.RECORDING_TOO_SHORT, VoiceIncidentRules.validate("LOT-1", "Cajas mojadas", null))
        assertNull(VoiceIncidentRules.validate("LOT-1", "Cajas mojadas", 4))
    }

    @Test
    fun voice_needsLotAndDescription() {
        assertEquals(VoiceIncidentRules.Error.LOT_REQUIRED, VoiceIncidentRules.validate(" ", "x", 4))
        assertEquals(VoiceIncidentRules.Error.DESCRIPTION_REQUIRED, VoiceIncidentRules.validate("LOT-1", "", 4))
    }

    // US73 ----------------------------------------------------------------------

    @Test
    fun shrinkage_totalsOnlyTheSelectedMonth() {
        val oct = YearMonth.of(2026, 10)
        val records = listOf(
            ShrinkageRecord("a", oct, ShrinkageStage.TRANSPORT, ShrinkageCause.THERMAL_EXCURSION, "Arándano", 45, 810.0),
            ShrinkageRecord("b", oct, ShrinkageStage.RECEPTION, ShrinkageCause.MECHANICAL_DAMAGE, "Palta Hass", 62, 558.0),
            ShrinkageRecord("c", oct.minusMonths(1), ShrinkageStage.HARVEST, ShrinkageCause.UNDERSIZE, "Mango Kent", 30, 210.0)
        )
        val summary = ShrinkageRules.summary(records, oct)
        assertEquals(107, summary.totalKg)
        assertEquals(1368.0, summary.totalCost, 0.0)
        assertEquals("b", summary.records.first().id) // biggest loss first
        assertTrue(ShrinkageRules.summary(records, oct.minusMonths(2)).isEmpty)
    }
}
