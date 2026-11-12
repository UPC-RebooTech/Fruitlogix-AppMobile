package com.rebootech.fruitlogix.infrastructureIot

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.ThresholdRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThresholdRulesTest {

    @Test
    fun validRanges_areAccepted() {
        assertNull(ThresholdRules.validate(5.0, 7.0, 85.0, 90.0))
    }

    @Test
    fun missingNumbers_areRejected() {
        assertEquals(ThresholdRules.Error.NOT_A_NUMBER, ThresholdRules.validate(null, 7.0, 85.0, 90.0))
    }

    @Test
    fun temperature_minMustBeBelowMax_andWithinLimits() {
        assertEquals(ThresholdRules.Error.TEMP_RANGE, ThresholdRules.validate(7.0, 7.0, 85.0, 90.0))
        assertEquals(ThresholdRules.Error.TEMP_RANGE, ThresholdRules.validate(-6.0, 2.0, 85.0, 90.0))
        assertEquals(ThresholdRules.Error.TEMP_RANGE, ThresholdRules.validate(5.0, 31.0, 85.0, 90.0))
    }

    @Test
    fun humidity_minMustBeBelowMax_andWithinLimits() {
        assertEquals(ThresholdRules.Error.HUMIDITY_RANGE, ThresholdRules.validate(5.0, 7.0, 90.0, 85.0))
        assertEquals(ThresholdRules.Error.HUMIDITY_RANGE, ThresholdRules.validate(5.0, 7.0, 30.0, 90.0))
    }
}
