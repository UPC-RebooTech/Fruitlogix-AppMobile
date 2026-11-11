package com.rebootech.fruitlogix.producer.profilesManagement.domain.validation

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile

/** Field that failed validation, with the reason. The UI maps it to a localized message. */
enum class ProfileField { FULL_NAME, EMAIL, PHONE, FARM_NAME, RUC, LOCATION }

enum class ProfileError { REQUIRED, INVALID_EMAIL, INVALID_PHONE, INVALID_RUC }

object ProfileValidator {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** Returns the errors by field; empty when the profile is valid. */
    fun validate(profile: ProducerProfile): Map<ProfileField, ProfileError> {
        val errors = mutableMapOf<ProfileField, ProfileError>()
        if (profile.fullName.isBlank()) errors[ProfileField.FULL_NAME] = ProfileError.REQUIRED
        when {
            profile.email.isBlank() -> errors[ProfileField.EMAIL] = ProfileError.REQUIRED
            !EMAIL.matches(profile.email.trim()) -> errors[ProfileField.EMAIL] = ProfileError.INVALID_EMAIL
        }
        val phoneDigits = profile.phone.filter { it.isDigit() }
        if (profile.phone.isNotBlank() && phoneDigits.length !in 9..12) {
            errors[ProfileField.PHONE] = ProfileError.INVALID_PHONE
        }
        if (profile.farmName.isBlank()) errors[ProfileField.FARM_NAME] = ProfileError.REQUIRED
        // Peruvian RUC: 11 digits
        if (profile.ruc.length != 11 || !profile.ruc.all { it.isDigit() }) {
            errors[ProfileField.RUC] = ProfileError.INVALID_RUC
        }
        if (profile.location.isBlank()) errors[ProfileField.LOCATION] = ProfileError.REQUIRED
        return errors
    }
}
