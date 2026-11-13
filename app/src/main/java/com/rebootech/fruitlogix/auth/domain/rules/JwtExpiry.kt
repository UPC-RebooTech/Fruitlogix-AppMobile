package com.rebootech.fruitlogix.auth.domain.rules

import java.time.Instant
import java.util.Base64

/**
 * Reads the expiry ("exp", seconds since 1970) of a JWT without verifying it: only the
 * server can verify the signature. Used to drop a saved session whose token already expired
 * (the server issues tokens for 7 days).
 */
object JwtExpiry {

    private val EXP = Regex("\"exp\"\\s*:\\s*(\\d+)")

    fun expiresAt(token: String): Instant? {
        val payload = token.split('.').getOrNull(1) ?: return null
        val json = runCatching { String(Base64.getUrlDecoder().decode(payload.padBase64())) }.getOrNull() ?: return null
        val seconds = EXP.find(json)?.groupValues?.get(1)?.toLongOrNull() ?: return null
        return Instant.ofEpochSecond(seconds)
    }

    /** A token without a readable expiry is treated as valid; the server decides. */
    fun isExpired(token: String, now: Instant = Instant.now()): Boolean =
        expiresAt(token)?.let { !now.isBefore(it) } ?: false

    private fun String.padBase64(): String = this + "=".repeat((4 - length % 4) % 4)
}
