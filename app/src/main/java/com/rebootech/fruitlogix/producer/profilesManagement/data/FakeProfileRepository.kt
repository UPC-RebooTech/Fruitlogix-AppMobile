package com.rebootech.fruitlogix.producer.profilesManagement.data

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import com.rebootech.fruitlogix.producer.profilesManagement.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

/** In-memory profile until the profiles backend exists. */
class FakeProfileRepository : ProfileRepository {

    private val _profile = MutableStateFlow(
        ProducerProfile(
            fullName = "Alexandra Jiménez",
            email = "alexandra.jimenez@example.com",
            phone = "+51 987 654 321",
            farmName = "Fundo Los Laureles",
            legalName = "Fundo Agrícola Los Laureles S.A.C.",
            ruc = "20491823901",
            location = "Ica, Valle Hermoso",
            globalGapValidUntil = YearMonth.of(2027, 10)
        )
    )
    override val profile: StateFlow<ProducerProfile> = _profile.asStateFlow()

    override suspend fun updateProfile(profile: ProducerProfile) {
        _profile.value = profile.copy(
            fullName = profile.fullName.trim(),
            email = profile.email.trim(),
            phone = profile.phone.trim(),
            farmName = profile.farmName.trim(),
            legalName = profile.legalName.trim(),
            location = profile.location.trim()
        )
    }

    companion object {
        val instance: ProfileRepository by lazy { FakeProfileRepository() }
    }
}
