package com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerKpis
import kotlin.math.round

object ContactRules {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isEmail(text: String): Boolean = EMAIL.matches(text.trim())

    /** At least 9 digits; spaces, dashes and a leading + are allowed. */
    fun isPhone(text: String): Boolean {
        val clean = text.trim().removePrefix("+").replace(" ", "").replace("-", "")
        return clean.length >= 9 && clean.all { it.isDigit() }
    }

    fun digits(text: String) = text.filter { it.isDigit() }
}

/** Register / edit a producer (US05, US39). */
object ProducerRules {
    enum class Error { NAME, FARM, LOCATION, CROP, EMAIL_FORMAT, EMAIL_TAKEN, PHONE_FORMAT, RUC_FORMAT, RUC_TAKEN }

    /**
     * @param others producers already in the network (excluding the one being edited).
     * Email, phone and RUC are required: the FruitLogix server does not accept a producer without them.
     */
    fun validate(draft: ProducerDraft, others: List<Producer>): Set<Error> = buildSet {
        if (draft.name.isBlank()) add(Error.NAME)
        if (draft.farmName.isBlank()) add(Error.FARM)
        if (draft.location.isBlank()) add(Error.LOCATION)
        if (draft.cropType.isBlank()) add(Error.CROP)
        if (!ContactRules.isEmail(draft.email)) add(Error.EMAIL_FORMAT)
        else if (others.any { it.email.equals(draft.email.trim(), ignoreCase = true) }) add(Error.EMAIL_TAKEN)
        if (!ContactRules.isPhone(draft.phone)) add(Error.PHONE_FORMAT)
        if (!isRuc(draft.ruc)) add(Error.RUC_FORMAT)
        else if (others.any { it.ruc == draft.ruc.trim() }) add(Error.RUC_TAKEN)
    }

    /** Peruvian RUC: exactly 11 digits. */
    fun isRuc(text: String): Boolean = text.trim().length == 11 && text.trim().all { it.isDigit() }

    fun kpis(producers: List<Producer>): ProducerKpis {
        fun oneDecimal(v: Double) = round(v * 10) / 10
        val active = producers.filter { it.weeklyTons > 0 || it.activeOrders > 0 }
        return ProducerKpis(
            total = producers.size,
            averageQuality = if (active.isEmpty()) 0.0 else oneDecimal(active.map { it.qualityRating }.average()),
            weeklyTons = oneDecimal(producers.sumOf { it.weeklyTons }),
            incidents30d = producers.sumOf { it.incidents30d }
        )
    }

    /** Search by name, farm, location or crop (US06). */
    fun search(producers: List<Producer>, query: String): List<Producer> {
        val q = query.trim()
        if (q.isEmpty()) return producers
        return producers.filter { p -> listOf(p.name, p.farmName, p.location, p.cropType).any { it.contains(q, ignoreCase = true) } }
    }
}

/** Invite a producer by email or phone (US41). */
object InviteRules {
    enum class Error { NAME, CONTACT_FORMAT, ALREADY_MEMBER }

    fun validate(name: String, contact: String, producers: List<Producer>): Error? {
        val clean = contact.trim()
        val isPhone = ContactRules.isPhone(clean)
        // Phones are compared by their last 9 digits, so "+51 987…" and "987…" are the same
        val last9 = ContactRules.digits(clean).takeLast(9)
        return when {
            name.isBlank() -> Error.NAME
            !ContactRules.isEmail(clean) && !isPhone -> Error.CONTACT_FORMAT
            producers.any {
                it.email.equals(clean, ignoreCase = true) || (isPhone && ContactRules.digits(it.phone).takeLast(9) == last9)
            } -> Error.ALREADY_MEMBER
            else -> null
        }
    }
}

/** Distributor's own profile. Peru's RUC has 11 digits. */
object DistributorProfileRules {
    enum class Error { NAME, EMAIL, PHONE, RUC }

    fun validate(profile: DistributorProfile): Set<Error> = buildSet {
        if (profile.fullName.isBlank()) add(Error.NAME)
        if (!ContactRules.isEmail(profile.email)) add(Error.EMAIL)
        if (!ContactRules.isPhone(profile.phone)) add(Error.PHONE)
        if (profile.ruc.length != 11 || !profile.ruc.all { it.isDigit() }) add(Error.RUC)
    }
}
