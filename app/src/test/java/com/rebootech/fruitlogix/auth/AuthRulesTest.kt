package com.rebootech.fruitlogix.auth

import com.rebootech.fruitlogix.auth.domain.rules.JwtExpiry
import com.rebootech.fruitlogix.auth.domain.validation.AuthValidators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.Base64

class AuthRulesTest {

    private fun token(payloadJson: String): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        return enc.encodeToString("""{"alg":"HS256"}""".toByteArray()) + "." +
            enc.encodeToString(payloadJson.toByteArray()) + ".signature"
    }

    @Test
    fun `reads the expiry of a token`() {
        val jwt = token("""{"sid":"7","unique_name":"rosa.quispe","exp":1800000000}""")
        assertEquals(Instant.ofEpochSecond(1_800_000_000), JwtExpiry.expiresAt(jwt))
    }

    @Test
    fun `a token is expired from its exp time on`() {
        val jwt = token("""{"exp":1000}""")
        assertFalse(JwtExpiry.isExpired(jwt, now = Instant.ofEpochSecond(999)))
        assertTrue(JwtExpiry.isExpired(jwt, now = Instant.ofEpochSecond(1000)))
    }

    @Test
    fun `a token without readable expiry is left to the server`() {
        assertNull(JwtExpiry.expiresAt("not-a-jwt"))
        assertFalse(JwtExpiry.isExpired("not-a-jwt"))
        assertFalse(JwtExpiry.isExpired(token("""{"sid":"7"}""")))
    }

    @Test
    fun `builds a display name from the username`() {
        assertEquals("Rosa Quispe", AuthValidators.nameFromUsername("rosa.quispe"))
        assertEquals("Carlos Mendoza", AuthValidators.nameFromUsername("carlos_mendoza2"))
        assertEquals("Ana", AuthValidators.nameFromUsername("ana"))
    }
}
