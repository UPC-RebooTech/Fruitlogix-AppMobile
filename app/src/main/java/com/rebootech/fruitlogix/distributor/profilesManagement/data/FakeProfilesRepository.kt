package com.rebootech.fruitlogix.distributor.profilesManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerSaveResult
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ContactRules
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.ProducerResourceDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Producers of the distributor's network.
 * - Server producers (/api/v1/producers) are read, created, edited and removed on Render.
 * - There are no sample producers. Invitations stay on this phone (the server has no endpoint).
 * - The distributor profile has no endpoint either: it starts with the account name only.
 * - If the server does not answer, a new or edited producer is kept on this phone only.
 */
class FakeProfilesRepository private constructor() : ProfilesRepository {

    private var nextNumber = 10

    private val _producers = MutableStateFlow<List<Producer>>(emptyList())
    override val producers: StateFlow<List<Producer>> = _producers.asStateFlow()

    /** Last server copy of each producer, so an edit keeps the fields the app does not show. */
    private val serverCopies = ConcurrentHashMap<Int, ProducerResourceDto>()

    private val _profile = MutableStateFlow(
        DistributorProfile(
            fullName = "",
            roleTitle = "",
            companyHub = "",
            email = "",
            phone = "",
            ruc = ""
        )
    )
    override val distributorProfile: StateFlow<DistributorProfile> = _profile.asStateFlow()

    @Volatile private var accountNameAdopted = false

    override fun adoptAccountName(name: String) {
        if (accountNameAdopted || name.isBlank()) return
        accountNameAdopted = true
        _profile.value = _profile.value.copy(fullName = name.trim())
    }

    override suspend fun registerProducer(draft: ProducerDraft): ProducerSaveResult =
        callServer(
            request = { RetrofitClient.api.createProducer(ProducerMapper.toRequest(draft)) },
            onSaved = { saved -> _producers.update { listOf(saved) + it } },
            onOffline = {
                localProducer("PROD-${nextNumber++}", draft).also { producer -> _producers.update { listOf(producer) + it } }
            }
        )

    override suspend fun inviteProducer(name: String, contact: String, location: String, cropType: String): Producer {
        // Invitations stay on this phone: the server needs RUC, email and phone, which arrive when they sign up
        val clean = contact.trim()
        val producer = Producer(
            id = "PROD-${nextNumber++}",
            name = name.trim(), farmName = "", location = location.trim(), cropType = cropType.trim(),
            email = if (ContactRules.isEmail(clean)) clean else "",
            phone = if (ContactRules.isPhone(clean)) clean else "",
            entityType = ProducerEntityType.INDIVIDUAL, qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0,
            activeOrders = 0, incidents30d = 0, status = ProducerStatus.INVITED
        )
        _producers.update { listOf(producer) + it }
        return producer
    }

    override suspend fun updateProducer(id: String, draft: ProducerDraft): ProducerSaveResult {
        val current = _producers.value.firstOrNull { it.id == id } ?: return ProducerSaveResult.Rejected
        val remoteId = current.remoteId
        if (remoteId == null) {
            // Producers saved without connection only live on this phone
            val updated = current.withDraft(draft)
            replace(id, updated)
            return ProducerSaveResult.Saved(updated)
        }
        return callServer(
            request = { RetrofitClient.api.updateProducer(remoteId, ProducerMapper.toRequest(draft, serverCopies[remoteId])) },
            // Keep what the server does not know (orders, volume…) from the current copy
            onSaved = { saved ->
                replace(id, saved.copy(weeklyTons = current.weeklyTons, stockKg = current.stockKg,
                    activeOrders = current.activeOrders, incidents30d = current.incidents30d))
            },
            onOffline = { current.withDraft(draft).also { replace(id, it) } }
        )
    }

    override suspend fun deleteProducer(id: String): Boolean {
        val current = _producers.value.firstOrNull { it.id == id } ?: return true
        val remoteId = current.remoteId
        if (remoteId != null) {
            val removed = withContext(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.api.deleteProducer(remoteId)
                    // 404: someone else already removed it
                    response.isSuccessful || response.code() == 404
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Log.w(TAG, "Delete failed: ${e.javaClass.simpleName}")
                    false
                }
            }
            if (!removed) return false
            serverCopies.remove(remoteId)
        }
        _producers.update { list -> list.filterNot { it.id == id } }
        return true
    }

    override suspend fun updateDistributorProfile(profile: DistributorProfile) {
        accountNameAdopted = true
        _profile.value = profile.copy(fullName = profile.fullName.trim(), email = profile.email.trim(), phone = profile.phone.trim())
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getProducers() } }
        remote.onSuccess { dtos ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            serverCopies.clear()
            dtos.forEach { serverCopies[it.id] = it }
            val fromServer = dtos.mapNotNull(ProducerMapper::toProducer)
            val serverRucs = fromServer.map { it.ruc }.filter { it.isNotBlank() }.toSet()
            // The server list replaces the previous server copy; phone-only producers stay unless the server has the same RUC
            _producers.update { list ->
                fromServer + list.filter { it.remoteId == null && (it.ruc.isBlank() || it.ruc !in serverRucs) }
            }
        }.onFailure {
            Log.w(TAG, "Refresh failed: ${it.javaClass.simpleName}")
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
        }
    }

    /** Runs a create/update call and turns the answer into a [ProducerSaveResult]. */
    private suspend fun callServer(
        request: suspend () -> ProducerResourceDto,
        onSaved: (Producer) -> Unit,
        onOffline: () -> Producer
    ): ProducerSaveResult {
        val outcome = withContext(Dispatchers.IO) {
            try {
                Result.success(request())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
        outcome.getOrNull()?.let { dto ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            serverCopies[dto.id] = dto
            val saved = ProducerMapper.toProducer(dto) ?: return ProducerSaveResult.Rejected
            onSaved(saved)
            return ProducerSaveResult.Saved(saved)
        }
        val error = outcome.exceptionOrNull()
        Log.w(TAG, "Save failed: ${error?.javaClass?.simpleName} ${(error as? HttpException)?.code() ?: ""}")
        return when ((error as? HttpException)?.code()) {
            409 -> ProducerSaveResult.RucTaken
            400, 404 -> ProducerSaveResult.Rejected
            else -> {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
                ProducerSaveResult.SavedOnPhoneOnly(onOffline())
            }
        }
    }

    private fun replace(id: String, producer: Producer) {
        _producers.update { list -> list.map { if (it.id == id) producer else it } }
    }

    private fun localProducer(id: String, draft: ProducerDraft) = Producer(
        id = id, name = draft.name.trim(), farmName = draft.farmName.trim(), location = draft.location.trim(),
        cropType = draft.cropType.trim(), email = draft.email.trim(), phone = draft.phone.trim(),
        entityType = draft.entityType, qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0,
        activeOrders = 0, incidents30d = 0, ruc = draft.ruc.trim()
    )

    private fun Producer.withDraft(draft: ProducerDraft) = copy(
        name = draft.name.trim(), farmName = draft.farmName.trim(), location = draft.location.trim(),
        cropType = draft.cropType.trim(), email = draft.email.trim(), phone = draft.phone.trim(),
        entityType = draft.entityType, ruc = draft.ruc.trim()
    )

    companion object {
        private const val TAG = "FruitLogix-Producers"

        val instance: FakeProfilesRepository by lazy { FakeProfilesRepository() }
    }
}
