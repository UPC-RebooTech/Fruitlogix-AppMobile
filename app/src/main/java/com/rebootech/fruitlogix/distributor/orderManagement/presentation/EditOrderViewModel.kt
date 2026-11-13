package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.components.OrderFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** What happened after an action, to tell the user and leave the screen. */
enum class EditOutcome { SAVED, DELETED }

data class EditOrderUiState(
    val order: DistributorOrder? = null,
    val form: OrderFormState = OrderFormState(),
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteExpired: Boolean = false,
    val outcome: EditOutcome? = null
) {
    val isEditable: Boolean get() = order?.isEditable == true
}

class EditOrderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository: OrderRepository = com.rebootech.fruitlogix.distributor.DistributorDependencies.orderRepository
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()

    private val _uiState = MutableStateFlow(load())
    val uiState: StateFlow<EditOrderUiState> = _uiState.asStateFlow()

    private fun load(): EditOrderUiState {
        val order = repository.orderById(orderId) ?: return EditOrderUiState()
        return EditOrderUiState(
            order = order,
            form = OrderFormState(
                clientName = order.clientName,
                productName = order.productName,
                // Shown with the decimal separator the user types (12.0 → "12")
                quantity = order.quantityTons.toString().removeSuffix(".0"),
                deliveryDate = order.deliveryDate
            )
        )
    }

    fun onClientChange(value: String) = updateForm { it.copy(clientName = value, clientError = null) }
    fun onProductChange(value: String) = updateForm { it.copy(productName = value, productError = null) }
    fun onQuantityChange(value: String) =
        updateForm { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(6), quantityError = null) }
    fun onDateChange(value: LocalDate) = updateForm { it.copy(deliveryDate = value, dateError = null) }

    private fun updateForm(change: (OrderFormState) -> OrderFormState) {
        if (!_uiState.value.isEditable) return
        _uiState.update { it.copy(form = change(it.form)) }
    }

    fun save() {
        val state = _uiState.value
        val order = state.order ?: return
        if (state.isSaving || !state.isEditable) return
        val (checked, draft) = state.form.validate()
        if (draft == null) {
            _uiState.update { it.copy(form = checked) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val ok = repository.updateOrder(order.id, draft)
            _uiState.update {
                if (ok) it.copy(isSaving = false, outcome = EditOutcome.SAVED)
                // Someone moved it out of "pending" meanwhile: reload to show it locked
                else load().copy(isSaving = false)
            }
        }
    }

    fun askDelete() = _uiState.update { it.copy(showDeleteDialog = true, deleteExpired = false) }
    fun dismissDelete() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        val order = _uiState.value.order ?: return
        viewModelScope.launch {
            when (repository.deleteOrder(order.id)) {
                DeleteOrderResult.SUCCESS -> _uiState.update { it.copy(showDeleteDialog = false, outcome = EditOutcome.DELETED) }
                DeleteOrderResult.WINDOW_EXPIRED -> _uiState.update { it.copy(showDeleteDialog = false, deleteExpired = true) }
                DeleteOrderResult.NOT_FOUND -> _uiState.update { it.copy(showDeleteDialog = false, order = null) }
            }
        }
    }
}
