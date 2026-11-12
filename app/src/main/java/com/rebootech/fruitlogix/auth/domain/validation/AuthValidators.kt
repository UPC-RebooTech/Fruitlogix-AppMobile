package com.rebootech.fruitlogix.auth.domain.validation

object AuthValidators {
    private val USERNAME_REGEX = Regex("^[a-z][a-z0-9._]{3,19}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^\\+?[0-9\\s-]{7,15}$")

    fun isValidUsername(username: String): Boolean =
        username.length in 4..20 && USERNAME_REGEX.matches(username)

    fun isPasswordLongEnough(password: String): Boolean = password.length >= 8

    fun hasPasswordLetter(password: String): Boolean = password.any { it.isLetter() }

    fun hasPasswordNumber(password: String): Boolean = password.any { it.isDigit() }

    fun isValidPassword(password: String): Boolean =
        isPasswordLongEnough(password) && hasPasswordLetter(password) && hasPasswordNumber(password)

    fun doPasswordsMatch(password: String, confirm: String): Boolean =
        password.isNotEmpty() && password == confirm

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_REGEX.matches(email.trim())

    fun isValidName(name: String): Boolean =
        name.trim().length in 2..60

    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return true
        return PHONE_REGEX.matches(phone.trim())
    }

    /** Keeps only the characters a username accepts ("Rosa Quispe" typed as is → "rosaquispe"). */
    fun cleanUsername(input: String): String =
        stripAccents(input).lowercase().filter { it.isLetterOrDigit() || it == '.' || it == '_' }.take(20)

    /**
     * Suggests a username from the full name so the user does not have to invent one:
     * "Rosa María Quispe" → "rosa.quispe". Returns null when nothing valid can be built.
     */
    fun suggestUsername(fullName: String): String? {
        val parts = stripAccents(fullName).lowercase()
            .split(Regex("\\s+"))
            .map { part -> part.filter { it in 'a'..'z' || it.isDigit() } }
            .filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null
        val base = if (parts.size == 1) parts.first() else "${parts.first()}.${parts.last()}"
        val suggestion = base.take(20)
        return suggestion.takeIf { isValidUsername(it) }
    }

    private fun stripAccents(text: String): String =
        java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
}

