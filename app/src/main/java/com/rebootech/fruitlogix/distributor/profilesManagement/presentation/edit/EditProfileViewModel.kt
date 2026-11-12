package com.rebootech.fruitlogix.distributor.profilesManagement.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.DistributorProfileRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val form: DistributorProfile,
    val errors: Set<DistributorProfileRules.Error> = emptySet(),
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class EditProfileViewModel(private val repository: ProfilesRepository) : ViewModel() {

    constructor() : this(DistributorDependencies.profilesRepository)

    private val _uiState = MutableStateFlow(EditProfileUiState(form = repository.distributorProfile.value))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    /** Updates the form and clears the error of the field being typed in. */
    fun onChange(field: DistributorProfileRules.Error?, change: (DistributorProfile) -> DistributorProfile) =
        _uiState.update { it.copy(form = change(it.form), errors = if (field == null) it.errors else it.errors - field) }

    fun onSave() {
        val form = _uiState.value.form
        val errors = DistributorProfileRules.validate(form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.updateDistributorProfile(form)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
