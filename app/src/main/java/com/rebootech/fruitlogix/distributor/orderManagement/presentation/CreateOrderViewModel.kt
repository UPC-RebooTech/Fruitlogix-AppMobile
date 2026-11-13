package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderSaveException
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.OrderFormState
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.ProducerOption
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerStatus
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CreateOrderUiState(
    val form: OrderFormState = OrderFormState(),
    val isSaving: Boolean = false,
    val created: DistributorOrder? = null,
    @field:StringRes val error: Int? = null,
    /** Producers that exist on the server: only those can be assigned to a server order. */
    val producers: List<ProducerOption> = emptyList()
)

class CreateOrderViewModel(
    private val repository: OrderRepository,
    profilesRepository: ProfilesRepository
) : ViewModel() {

    constructor() : this(DistributorDependencies.orderRepository, DistributorDependencies.profilesRepository)

    private val local = MutableStateFlow(CreateOrderUiState())

    val uiState: StateFlow<CreateOrderUiState> = combine(local, profilesRepository.producers) { state, producers ->
        state.copy(
            producers = producers
                .filter { it.remoteId != null && it.status == ProducerStatus.ACTIVE }
                .map { ProducerOption(it.remoteId!!, it.name) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreateOrderUiState())

    fun onClientChange(id: Int) = updateForm { it.copy(clientId = id, clientError = null) }
    fun onProductChange(value: String) = updateForm { it.copy(productName = value, productError = null) }
    fun onQuantityChange(value: String) =
        updateForm { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(9), quantityError = null) }
    fun onDateChange(value: LocalDate) = updateForm { it.copy(deliveryDate = value, dateError = null) }
    fun onProducerChange(id: Int?) = updateForm { it.copy(producerId = id) }

    private fun updateForm(change: (OrderFormState) -> OrderFormState) =
        local.update { it.copy(form = change(it.form), error = null) }

    fun submit() {
        val state = local.value
        if (state.isSaving) return
        val (checked, draft) = state.form.validate()
        if (draft == null) {
            local.update { it.copy(form = checked) }
            return
        }
        viewModelScope.launch {
            local.update { it.copy(isSaving = true, error = null) }
            try {
                val order = repository.createOrder(draft)
                local.update { it.copy(isSaving = false, created = order) }
            } catch (e: OrderSaveException) {
                local.update {
                    it.copy(
                        isSaving = false,
                        error = when (e.reason) {
                            // Without an answer we cannot know whether it was saved: ask to check before retrying
                            OrderSaveException.Reason.NO_CONNECTION -> R.string.dist_order_error_no_connection
                            OrderSaveException.Reason.REJECTED -> R.string.dist_order_error_rejected
                        }
                    )
                }
            }
        }
    }

    fun createAnother() {
        local.value = CreateOrderUiState()
    }
}
