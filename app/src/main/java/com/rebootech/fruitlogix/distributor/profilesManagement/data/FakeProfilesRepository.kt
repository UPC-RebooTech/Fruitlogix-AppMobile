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
 * - Sample producers (the names used by Orders, Fleet and Invoices) and invitations stay on this
 *   phone, so the rest of the app keeps working with or without connection.
 * - If the server does not answer, a new or edited producer is kept on this phone only.
 */
class FakeProfilesRepository private constructor() : ProfilesRepository {

    private var nextNumber = 10

    private val _producers = MutableStateFlow(initialProducers())
    override val producers: StateFlow<List<Producer>> = _producers.asStateFlow()

    /** Last server copy of each producer, so an edit keeps the fields the app does not show. */
    private val serverCopies = ConcurrentHashMap<Int, ProducerResourceDto>()

    private val _profile = MutableStateFlow(
        DistributorProfile(
            fullName = "Carlos Mendoza",
            roleTitle = "Jefe de operaciones",
            companyHub = "Lima Central Hub · Callao",
            email = "carlos.mendoza@fruitlogix.pe",
            phone = "+51 987 654 321",
            ruc = "20601234567"
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
            // Sample producers only live on this phone
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

        private fun initialProducers() = listOf(
            Producer("PROD-01", "Fundo Santa Rosa", "Fundo Santa Rosa", "Huaraz, Áncash", "Palta Hass",
                "contacto@fundosantarosa.pe", "+51 956 410 228", ProducerEntityType.COMPANY,
                qualityRating = 4.8, weeklyTons = 4.2, stockKg = 570, activeOrders = 1, incidents30d = 0, ruc = "20481234561"),
            Producer("PROD-02", "Viñedos de Ica", "Viñedos de Ica", "Ica", "Uva Red Globe",
                "ventas@vinedosdeica.pe", "+51 987 112 340", ProducerEntityType.COMPANY,
                qualityRating = 4.9, weeklyTons = 5.4, stockKg = 800, activeOrders = 1, incidents30d = 0, ruc = "20512345672"),
            Producer("PROD-03", "Campos del Norte", "Sector B3", "Piura", "Mango Kent",
                "operaciones@camposdelnorte.pe", "+51 944 870 615", ProducerEntityType.COMPANY,
                qualityRating = 4.4, weeklyTons = 3.9, stockKg = 600, activeOrders = 2, incidents30d = 1, ruc = "20456789013"),
            Producer("PROD-04", "Agro Valle Verde", "Fundo El Paraíso", "Huaral, Lima", "Fresa",
                "juan.quispe@vallev.pe", "+51 977 445 566", ProducerEntityType.INDIVIDUAL,
                qualityRating = 4.5, weeklyTons = 2.1, stockKg = 150, activeOrders = 1, incidents30d = 0, ruc = "10456789014"),
            Producer("PROD-05", "Finca Los Andes", "Finca Los Andes", "Arequipa", "Uva Red Globe",
                "fincalosandes@agro.pe", "+51 944 332 211", ProducerEntityType.COMPANY,
                qualityRating = 4.2, weeklyTons = 1.8, stockKg = 300, activeOrders = 0, incidents30d = 1, ruc = "20567890125"),
            Producer("PROD-06", "Rosa Quispe", "", "Chanchamayo, Junín", "Mango Kent",
                "", "+51 912 345 678", ProducerEntityType.INDIVIDUAL,
                qualityRating = 0.0, weeklyTons = 0.0, stockKg = 0, activeOrders = 0, incidents30d = 0,
                status = ProducerStatus.INVITED)
        )
    }
}
