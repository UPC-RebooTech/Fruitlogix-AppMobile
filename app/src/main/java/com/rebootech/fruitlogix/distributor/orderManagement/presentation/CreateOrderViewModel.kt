package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.OrderFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CreateOrderUiState(
    val form: OrderFormState = OrderFormState(),
    val isSaving: Boolean = false,
    val created: DistributorOrder? = null
)

class CreateOrderViewModel : ViewModel() {

    private val repository: OrderRepository = FakeOrderRepository.instance

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    fun onClientChange(value: String) = updateForm { it.copy(clientName = value, clientError = null) }
    fun onProductChange(value: String) = updateForm { it.copy(productName = value, productError = null) }
    fun onQuantityChange(value: String) =
        updateForm { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(6), quantityError = null) }
    fun onDateChange(value: LocalDate) = updateForm { it.copy(deliveryDate = value, dateError = null) }

    private fun updateForm(change: (OrderFormState) -> OrderFormState) =
        _uiState.update { it.copy(form = change(it.form)) }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val (checked, draft) = state.form.validate()
        if (draft == null) {
            _uiState.update { it.copy(form = checked) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val order = repository.createOrder(draft)
            _uiState.update { it.copy(isSaving = false, created = order) }
        }
    }

    fun createAnother() {
        _uiState.value = CreateOrderUiState()
    }
}
