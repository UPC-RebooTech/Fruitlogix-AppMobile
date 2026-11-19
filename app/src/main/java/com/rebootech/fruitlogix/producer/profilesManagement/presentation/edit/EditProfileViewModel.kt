package com.rebootech.fruitlogix.producer.profilesManagement.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerProfile
import com.rebootech.fruitlogix.producer.profilesManagement.domain.repository.ProfileRepository
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileError
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileField
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Edit form state. Errors are enums; the screen maps them to localized messages. */
data class EditProfileUiState(
    val form: ProducerProfile,
    val errors: Map<ProfileField, ProfileError> = emptyMap(),
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class EditProfileViewModel(
    private val repository: ProfileRepository = FakeProfileRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(form = repository.profile.value))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onChange(field: ProfileField?, transform: (ProducerProfile) -> ProducerProfile) {
        _uiState.update { state ->
            state.copy(
                form = transform(state.form),
                // Clear the error of the field being edited
                errors = if (field != null) state.errors - field else state.errors
            )
        }
    }

    fun onSave() {
        val form = _uiState.value.form
        val errors = ProfileValidator.validate(form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.updateProfile(form)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
