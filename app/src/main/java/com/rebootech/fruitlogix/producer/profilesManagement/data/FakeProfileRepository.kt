package com.rebootech.fruitlogix.producer.profilesManagement.data

import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerType
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProfileSaveResult
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProfileStatus
import com.rebootech.fruitlogix.producer.profilesManagement.domain.repository.ProfileRepository
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.ProducerResourceDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.SaveProducerRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Profile of the signed-in producer on the server (GET /producers/me, POST /producers, PUT /producers/{id}).
 * Field mapping: name ↔ fullName, legal name ↔ legalName, RUC ↔ taxId, farm ↔ address,
 * location "Ica, Valle Hermoso" ↔ city + region. The GLOBALG.A.P. date stays on this phone.
 * Without connection the last known profile keeps the screens usable (it starts blank: no sample data).
 */
class FakeProfileRepository : ProfileRepository {

    private val _profile = MutableStateFlow(EMPTY)
    override val profile: StateFlow<ProducerProfile> = _profile.asStateFlow()

    private val _status = MutableStateFlow(ProfileStatus.LOADING)
    override val status: StateFlow<ProfileStatus> = _status.asStateFlow()

    private val _producerId = MutableStateFlow<Int?>(null)
    override val producerId: StateFlow<Int?> = _producerId.asStateFlow()

    /** Last server copy, so an update keeps the fields the app does not show. */
    @Volatile private var serverCopy: ProducerResourceDto? = null

    override suspend fun refresh() {
        when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getMyProducerProfile() } }) {
            is ApiResult.Ok -> apply(result.value)
            is ApiResult.Failed -> _status.value = when {
                // 404: this account has not created its producer profile yet
                result.failure.isNotFound -> ProfileStatus.MISSING.also { _producerId.value = null }
                else -> if (_producerId.value != null) ProfileStatus.READY else ProfileStatus.OFFLINE
            }
        }
    }

    override suspend fun createProfile(profile: ProducerProfile): ProfileSaveResult {
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createMyProducerProfile(toRequest(profile)) } }
        return when (result) {
            is ApiResult.Ok -> {
                apply(result.value, local = profile)
                ProfileSaveResult(true)
            }
            is ApiResult.Failed -> {
                // 409: the account already has a profile (created from another phone): read it
                if (result.failure.isConflict) refresh()
                ProfileSaveResult(_status.value == ProfileStatus.READY, result.failure.serverMessage)
            }
        }
    }

    override suspend fun updateProfile(profile: ProducerProfile): ProfileSaveResult {
        val id = _producerId.value
        if (id == null) {
            _profile.value = clean(profile)
            return ProfileSaveResult(true)
        }
        return when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.updateMyProducerProfile(id, toRequest(profile)) } }) {
            is ApiResult.Ok -> {
                apply(result.value, local = profile)
                ProfileSaveResult(true)
            }
            is ApiResult.Failed -> ProfileSaveResult(false, result.failure.serverMessage)
        }
    }

    private fun apply(dto: ProducerResourceDto, local: ProducerProfile? = null) {
        serverCopy = dto
        _producerId.value = dto.id
        _status.value = ProfileStatus.READY
        _profile.value = ProducerProfile(
            fullName = dto.fullName.orEmpty(),
            email = dto.email.orEmpty(),
            phone = dto.phone.orEmpty(),
            farmName = dto.address.orEmpty(),
            legalName = dto.legalName.orEmpty(),
            ruc = dto.taxId.orEmpty(),
            location = listOf(dto.city, dto.region).filterNot { it.isNullOrBlank() }.joinToString(", "),
            globalGapValidUntil = local?.globalGapValidUntil ?: _profile.value.globalGapValidUntil.takeIf { _profile.value != EMPTY },
            crop = dto.crop.orEmpty(),
            type = if (dto.producerType.equals("Individual", ignoreCase = true)) ProducerType.INDIVIDUAL else ProducerType.COMPANY
        )
    }

    private fun toRequest(profile: ProducerProfile): SaveProducerRequestDto {
        val p = clean(profile)
        val parts = p.location.split(',', limit = 2).map { it.trim() }
        val copy = serverCopy
        return SaveProducerRequestDto(
            producerType = p.type.serverValue,
            fullName = p.fullName,
            taxId = p.ruc,
            legalName = p.legalName.ifBlank { p.fullName },
            email = p.email,
            phone = p.phone,
            country = copy?.country?.takeIf { it.isNotBlank() } ?: COUNTRY,
            region = parts.getOrElse(1) { "" },
            city = parts[0],
            address = p.farmName,
            crop = p.crop,
            cultivatedHectares = copy?.cultivatedHectares ?: 0.0,
            monthlyProduction = copy?.monthlyProduction.orEmpty(),
            operationsStartDate = copy?.operationsStartDate,
            certifications = copy?.certifications.orEmpty(),
            photo = copy?.photo.orEmpty()
        )
    }

    private fun clean(profile: ProducerProfile) = profile.copy(
        fullName = profile.fullName.trim(),
        email = profile.email.trim(),
        phone = profile.phone.trim(),
        farmName = profile.farmName.trim(),
        legalName = profile.legalName.trim(),
        location = profile.location.trim(),
        crop = profile.crop.trim(),
        ruc = profile.ruc.trim()
    )

    companion object {
        private const val COUNTRY = "Perú"

        /** Shown until the server answers (and offline): the demo farm. */
        private val EMPTY = ProducerProfile(
            fullName = "", email = "", phone = "", farmName = "", legalName = "", ruc = "",
            location = "", globalGapValidUntil = null, crop = ""
        )

        val instance: ProfileRepository by lazy { FakeProfileRepository() }
    }
}
