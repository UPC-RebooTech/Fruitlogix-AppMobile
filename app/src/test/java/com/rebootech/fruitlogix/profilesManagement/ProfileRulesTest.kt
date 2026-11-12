package com.rebootech.fruitlogix.profilesManagement

import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.DistributorProfileRules
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.InviteRules
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ProducerRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRulesTest {

    private val existing = Producer(
        "P1", "Fundo Santa Rosa", "Fundo Santa Rosa", "Huaraz", "Palta Hass",
        "contacto@fundosantarosa.pe", "+51 956 410 228", ProducerEntityType.COMPANY,
        qualityRating = 4.8, weeklyTons = 4.2, stockKg = 570, activeOrders = 1, incidents30d = 0
    )

    private fun draft(email: String = "", phone: String = "", name: String = "Viñedos de Ica") =
        ProducerDraft(name, "Viñedos de Ica", "Ica", "Uva Red Globe", email, phone, ProducerEntityType.COMPANY)

    // US05 / US39 -----------------------------------------------------------------

    @Test
    fun register_requiredFields() {
        val errors = ProducerRules.validate(ProducerDraft("", " ", "", "", "", "", ProducerEntityType.INDIVIDUAL), emptyList())
        assertEquals(setOf(ProducerRules.Error.NAME, ProducerRules.Error.FARM, ProducerRules.Error.LOCATION, ProducerRules.Error.CROP), errors)
    }

    @Test
    fun register_contactIsOptional_butMustBeValid() {
        assertTrue(ProducerRules.validate(draft(), listOf(existing)).isEmpty())
        assertEquals(setOf(ProducerRules.Error.EMAIL_FORMAT), ProducerRules.validate(draft(email = "ventas@"), emptyList()))
        assertEquals(setOf(ProducerRules.Error.PHONE_FORMAT), ProducerRules.validate(draft(phone = "1234"), emptyList()))
    }

    @Test
    fun register_emailAlreadyInTheNetwork() {
        assertEquals(
            setOf(ProducerRules.Error.EMAIL_TAKEN),
            ProducerRules.validate(draft(email = "CONTACTO@fundosantarosa.pe"), listOf(existing))
        )
    }

    // US06 / US42 -------------------------------------------------------------------

    @Test
    fun search_byNameFarmLocationOrCrop() {
        val list = listOf(existing)
        assertEquals(1, ProducerRules.search(list, "huaraz").size)
        assertEquals(1, ProducerRules.search(list, "palta").size)
        assertTrue(ProducerRules.search(list, "mango").isEmpty())
    }

    @Test
    fun kpis_ignoreInactiveProducersInTheAverage() {
        val invited = existing.copy(id = "P2", qualityRating = 0.0, weeklyTons = 0.0, activeOrders = 0, incidents30d = 2)
        val kpis = ProducerRules.kpis(listOf(existing, invited))
        assertEquals(2, kpis.total)
        assertEquals(4.8, kpis.averageQuality, 0.0)
        assertEquals(4.2, kpis.weeklyTons, 0.0)
        assertEquals(2, kpis.incidents30d)
    }

    // US41 --------------------------------------------------------------------------

    @Test
    fun invite_acceptsEmailOrPhone() {
        assertNull(InviteRules.validate("Rosa", "rosa@chanchamayo.pe", listOf(existing)))
        assertNull(InviteRules.validate("Rosa", "912 345 678", listOf(existing)))
        assertEquals(InviteRules.Error.CONTACT_FORMAT, InviteRules.validate("Rosa", "rosa", listOf(existing)))
        assertEquals(InviteRules.Error.NAME, InviteRules.validate(" ", "rosa@chanchamayo.pe", emptyList()))
    }

    @Test
    fun invite_existingMember_isRefused() {
        assertEquals(InviteRules.Error.ALREADY_MEMBER, InviteRules.validate("X", "956410228", listOf(existing)))
        assertEquals(InviteRules.Error.ALREADY_MEMBER, InviteRules.validate("X", "contacto@fundosantarosa.pe", listOf(existing)))
    }

    // Distributor profile -----------------------------------------------------------

    @Test
    fun profile_rucMustHave11Digits() {
        val ok = DistributorProfile("Carlos Mendoza", "Ops", "Lima Central Hub", "carlos@fruitlogix.pe", "+51 987 654 321", "20601234567")
        assertTrue(DistributorProfileRules.validate(ok).isEmpty())
        assertEquals(setOf(DistributorProfileRules.Error.RUC), DistributorProfileRules.validate(ok.copy(ruc = "2060123")))
        assertEquals(setOf(DistributorProfileRules.Error.EMAIL), DistributorProfileRules.validate(ok.copy(email = "carlos")))
    }
}
