package com.rebootech.fruitlogix.producer.profilesManagement.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerType
import com.rebootech.fruitlogix.producer.profilesManagement.domain.repository.ProfileRepository
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileError
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileField
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateProfileUiState(
    val form: ProducerProfile,
    val errors: Map<ProfileField, ProfileError> = emptyMap(),
    val isSaving: Boolean = false,
    /** The server refused it: its own text (blank = generic message). */
    val serverError: String? = null
)

/** First step of a new producer account: the producer profile on the server (POST /producers). */
class CreateProducerProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    constructor() : this(FakeProfileRepository.instance)

    private val _uiState = MutableStateFlow(
        CreateProfileUiState(
            form = ProducerProfile(
                fullName = "", email = "", phone = "", farmName = "", legalName = "", ruc = "",
                location = "", globalGapValidUntil = null, crop = "", type = ProducerType.INDIVIDUAL
            )
        )
    )
    val uiState: StateFlow<CreateProfileUiState> = _uiState.asStateFlow()

    fun onChange(field: ProfileField?, transform: (ProducerProfile) -> ProducerProfile) = _uiState.update {
        it.copy(form = transform(it.form), errors = if (field != null) it.errors - field else it.errors, serverError = null)
    }

    fun onTypeChange(type: ProducerType) = onChange(null) { it.copy(type = type) }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = ProfileValidator.validate(state.form, requireCrop = true)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, serverError = null) }
            val result = repository.createProfile(state.form)
            // On success the shell sees the profile READY and opens the app
            _uiState.update { it.copy(isSaving = false, serverError = if (result.ok) null else result.serverMessage ?: "") }
        }
    }
}
