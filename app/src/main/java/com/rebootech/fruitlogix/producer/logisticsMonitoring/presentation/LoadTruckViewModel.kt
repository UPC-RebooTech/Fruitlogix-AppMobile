package com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.logisticsMonitoring.data.OrderDispatchRepository
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.LoadCheck
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.LoadRules
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.model.TruckLoad
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.repository.DispatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class LoadTruckUiState(
    val isLoading: Boolean = true,
    val load: TruckLoad? = null,
    val checked: Set<LoadCheck> = emptySet(),
    val isSending: Boolean = false,
    /** Set when the dispatch was confirmed from this screen. */
    val dispatchedAt: LocalDateTime? = null
) {
    val isDispatched: Boolean get() = dispatchedAt != null || load?.isDispatched == true
    val canConfirm: Boolean get() = !isSending && load != null && LoadRules.canConfirm(load, checked)
}

/** Load the truck at the farm: check the cargo and confirm the truck left. */
class LoadTruckViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository: DispatchRepository = OrderDispatchRepository.instance
    private val orderId = savedStateHandle.get<String>("orderId").orEmpty().removePrefix("#")

    private val _uiState = MutableStateFlow(LoadTruckUiState())
    val uiState: StateFlow<LoadTruckUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeLoad(orderId).collect { load ->
                _uiState.update { it.copy(isLoading = false, load = load) }
            }
        }
    }

    fun toggle(check: LoadCheck) {
        _uiState.update {
            it.copy(checked = if (check in it.checked) it.checked - check else it.checked + check)
        }
    }

    fun confirmDispatch() {
        if (!_uiState.value.canConfirm) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            val ok = repository.confirmDispatch(orderId)
            _uiState.update {
                it.copy(isSending = false, dispatchedAt = if (ok) LocalDateTime.now() else null)
            }
        }
    }
}
