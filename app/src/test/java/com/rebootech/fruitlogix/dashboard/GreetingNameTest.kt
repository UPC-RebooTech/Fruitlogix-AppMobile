package com.rebootech.fruitlogix.dashboard

import com.rebootech.fruitlogix.producer.dashboard.presentation.firstName
import org.junit.Assert.assertEquals
import org.junit.Test

/** Home greets the producer by the first word of the profile's full name. */
class GreetingNameTest {

    @Test
    fun firstWordOfTheFullName() {
        assertEquals("Carlos", firstName("Carlos Alberto Mendoza Quispe"))
        assertEquals("Carlos", firstName("  Carlos   Alberto "))
        assertEquals("Rosa", firstName("Rosa"))
        assertEquals("", firstName(""))
    }
}
