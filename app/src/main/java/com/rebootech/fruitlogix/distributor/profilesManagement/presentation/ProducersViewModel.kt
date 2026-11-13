package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerKpis
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerSaveResult
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.InviteRules
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ProducerRules
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProducerSheet {
    data object Register : ProducerSheet
    data class Edit(val producer: Producer) : ProducerSheet
    data object Invite : ProducerSheet
}

/** Invitation ready to be shared (opens the system share sheet once). */
data class PendingInvite(val name: String, val crop: String, val inviterName: String, val link: String)

data class ProducersUiState(
    val producers: List<Producer> = emptyList(),
    val query: String = "",
    val sheet: ProducerSheet? = null,
    val formErrors: Set<ProducerRules.Error> = emptySet(),
    /** Error from the server shown inside the form (e.g. data refused). */
    @field:StringRes val sheetError: Int? = null,
    /** A save or delete is running on the server: the buttons wait. */
    val saving: Boolean = false,
    @field:StringRes val inviteError: Int? = null,
    val deleting: Producer? = null,
    val invite: PendingInvite? = null,
    val message: UiText? = null
) {
    val kpis: ProducerKpis get() = ProducerRules.kpis(producers)
    val visible: List<Producer> get() = ProducerRules.search(producers, query)
}

/** Producer network: KPIs (US42), search (US06), register (US05), edit (US39), remove (US40), invite (US41). */
class ProducersViewModel(private val repository: ProfilesRepository) : ViewModel() {

    /** Used by the default ViewModel factory. */
    constructor() : this(DistributorDependencies.profilesRepository)

    private val local = MutableStateFlow(ProducersUiState())

    val uiState: StateFlow<ProducersUiState> = combine(repository.producers, local) { producers, local ->
        local.copy(producers = producers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProducersUiState())

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun onQueryChange(value: String) = local.update { it.copy(query = value) }

    fun openSheet(sheet: ProducerSheet) = local.update { it.copy(sheet = sheet, formErrors = emptySet(), sheetError = null, inviteError = null) }
    fun closeSheet() = local.update { it.copy(sheet = null, formErrors = emptySet(), sheetError = null, inviteError = null) }
    fun onMessageShown() = local.update { it.copy(message = null) }
    fun onInviteShared() = local.update { it.copy(invite = null) }

    fun save(draft: ProducerDraft) {
        val editing = (local.value.sheet as? ProducerSheet.Edit)?.producer
        val others = uiState.value.producers.filter { it.id != editing?.id }
        val errors = ProducerRules.validate(draft, others)
        if (errors.isNotEmpty()) {
            local.update { it.copy(formErrors = errors) }
            return
        }
        if (local.value.saving) return
        local.update { it.copy(saving = true, formErrors = emptySet(), sheetError = null) }
        viewModelScope.launch {
            val result = if (editing == null) repository.registerProducer(draft) else repository.updateProducer(editing.id, draft)
            val name = draft.name.trim()
            when (result) {
                is ProducerSaveResult.Saved -> closeWith(
                    UiText.StringResource(if (editing == null) R.string.prod_registered else R.string.prod_updated, arrayOf(name))
                )
                is ProducerSaveResult.SavedOnPhoneOnly -> closeWith(UiText.StringResource(R.string.prod_saved_phone_only, arrayOf(name)))
                ProducerSaveResult.RucTaken -> local.update { it.copy(saving = false, formErrors = setOf(ProducerRules.Error.RUC_TAKEN)) }
                ProducerSaveResult.Rejected -> local.update { it.copy(saving = false, sheetError = R.string.prod_server_rejected) }
            }
        }
    }

    private fun closeWith(message: UiText) = local.update {
        it.copy(sheet = null, saving = false, formErrors = emptySet(), sheetError = null, message = message)
    }

    fun invite(name: String, contact: String, location: String, crop: String) {
        when (InviteRules.validate(name, contact, uiState.value.producers)) {
            InviteRules.Error.NAME -> local.update { it.copy(inviteError = R.string.prod_error_name) }
            InviteRules.Error.CONTACT_FORMAT -> local.update { it.copy(inviteError = R.string.prod_invite_error_contact) }
            InviteRules.Error.ALREADY_MEMBER -> local.update { it.copy(inviteError = R.string.prod_invite_error_member) }
            null -> viewModelScope.launch {
                val invited = repository.inviteProducer(name, contact, location, crop)
                local.update {
                    it.copy(
                        sheet = null,
                        inviteError = null,
                        invite = PendingInvite(
                            name = invited.name,
                            crop = invited.cropType,
                            inviterName = repository.distributorProfile.value.fullName,
                            link = "$INVITE_URL${invited.id}"
                        ),
                        message = UiText.StringResource(R.string.prod_invited_msg, arrayOf(invited.name))
                    )
                }
            }
        }
    }

    fun askDelete(producer: Producer) = local.update { it.copy(deleting = producer) }
    fun cancelDelete() = local.update { it.copy(deleting = null) }

    fun confirmDelete() {
        val producer = local.value.deleting ?: return
        if (local.value.saving) return
        local.update { it.copy(saving = true) }
        viewModelScope.launch {
            val removed = repository.deleteProducer(producer.id)
            local.update {
                it.copy(
                    deleting = null,
                    saving = false,
                    message = UiText.StringResource(if (removed) R.string.prod_deleted else R.string.prod_delete_failed, arrayOf(producer.name))
                )
            }
        }
    }

    private companion object {
        const val INVITE_URL = "https://upc-rebootech.github.io/Fruitlogix-website-v2/?invite="
    }
}
