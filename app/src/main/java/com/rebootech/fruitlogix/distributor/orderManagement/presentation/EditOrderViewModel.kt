package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CatalogProduct
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CommercialClient
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderCatalogRepository
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
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

/** What happened after an action, to tell the user and leave the screen. */
enum class EditOutcome { SAVED, DELETED, PRODUCER_ASSIGNED, CANCELLED }

data class EditOrderUiState(
    val order: DistributorOrder? = null,
    val form: OrderFormState = OrderFormState(),
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteExpired: Boolean = false,
    val outcome: EditOutcome? = null,
    /** Producers on the server, offered to move a pending order to "in preparation". */
    val producers: List<ProducerOption> = emptyList(),
    val choosingProducer: Boolean = false,
    val assignFailed: Boolean = false,
    val clients: List<CommercialClient> = emptyList(),
    val products: List<CatalogProduct> = emptyList(),
    /** Delete answered 409: the order has a delivery, invoice or payment, so it can only be cancelled. */
    val deleteBlocked: Boolean = false,
    val showCancelDialog: Boolean = false,
    val cancelFailed: Boolean = false
) {
    /** Pending and in-preparation orders can be cancelled. */
    val canCancel: Boolean get() = order?.status == OrderStatus.PENDING || order?.status == OrderStatus.IN_PREPARATION

    val isEditable: Boolean get() = order?.isEditable == true

    /** Server orders: only the delivery date can change. */
    val onlyDateEditable: Boolean get() = order?.onlyDateEditable == true

    /** Name of the assigned producer, when it is in the network. */
    val producerName: String? get() = order?.producerName?.takeIf { it.isNotBlank() }
        ?: order?.producerId?.let { id -> producers.firstOrNull { it.id == id }?.name }
}

class EditOrderViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrderRepository,
    private val catalog: OrderCatalogRepository,
    profilesRepository: ProfilesRepository
) : ViewModel() {

    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle,
        DistributorDependencies.orderRepository,
        DistributorDependencies.orderCatalogRepository,
        DistributorDependencies.profilesRepository
    )

    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()

    private val local = MutableStateFlow(load())

    val uiState: StateFlow<EditOrderUiState> = combine(local, profilesRepository.producers, catalog.clients, catalog.products) { state, producers, clients, products ->
        state.copy(
            clients = clients,
            products = products,
            producers = producers
                .filter { it.remoteId != null && it.status == ProducerStatus.ACTIVE }
                .map { ProducerOption(it.remoteId!!, it.name) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), local.value)

    private fun load(): EditOrderUiState {
        val order = repository.orderById(orderId) ?: return EditOrderUiState()
        return EditOrderUiState(
            order = order,
            form = OrderFormState(
                clientId = order.clientId,
                productId = catalog.products.value.firstOrNull { it.name.equals(order.productName, ignoreCase = true) }?.id,
                productName = order.productName,
                quantity = formatKgForInput(order.quantityKg),
                deliveryDate = order.deliveryDate,
                deliveryAddress = catalog.clients.value.firstOrNull { it.id == order.clientId }?.deliveryAddress.orEmpty()
            )
        )
    }

    fun onClientChange(client: CommercialClient) = updateForm {
        it.copy(clientId = client.id, clientError = null, deliveryAddress = it.deliveryAddress.ifBlank { client.deliveryAddress })
    }
    fun onProductChange(product: CatalogProduct) =
        updateForm { it.copy(productId = product.id, productName = product.name, productError = null) }
    fun onAddressChange(value: String) = updateForm { it.copy(deliveryAddress = value.take(200), addressError = null) }
    fun onQuantityChange(value: String) =
        updateForm { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(9), quantityError = null) }
    fun onDateChange(value: LocalDate) = updateForm { it.copy(deliveryDate = value, dateError = null) }

    private fun updateForm(change: (OrderFormState) -> OrderFormState) {
        if (!local.value.isEditable) return
        local.update { it.copy(form = change(it.form)) }
    }

    fun save() {
        val state = local.value
        val order = state.order ?: return
        if (state.isSaving || !state.isEditable) return
        val (checked, draft) = uiState.value.let { current ->
            state.form.validate(current.clients, current.products, onlyDate = state.onlyDateEditable)
        }
        if (draft == null) {
            local.update { it.copy(form = checked) }
            return
        }
        viewModelScope.launch {
            local.update { it.copy(isSaving = true) }
            val ok = repository.updateOrder(order.id, draft)
            local.update {
                if (ok) it.copy(isSaving = false, outcome = EditOutcome.SAVED)
                // Someone moved it out of "pending" meanwhile, or the server refused: reload
                else load().copy(isSaving = false)
            }
        }
    }

    fun openProducerChoice() = local.update { it.copy(choosingProducer = true, assignFailed = false) }
    fun closeProducerChoice() = local.update { it.copy(choosingProducer = false) }

    fun assignProducer(producerId: Int) {
        val order = local.value.order ?: return
        if (local.value.isSaving) return
        viewModelScope.launch {
            local.update { it.copy(isSaving = true, assignFailed = false) }
            val ok = repository.assignProducer(order.id, producerId)
            local.update {
                if (ok) load().copy(outcome = EditOutcome.PRODUCER_ASSIGNED)
                else it.copy(isSaving = false, choosingProducer = false, assignFailed = true)
            }
        }
    }

    fun askDelete() = local.update { it.copy(showDeleteDialog = true, deleteExpired = false, deleteBlocked = false) }
    fun dismissDelete() = local.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        val order = local.value.order ?: return
        viewModelScope.launch {
            when (repository.deleteOrder(order.id)) {
                DeleteOrderResult.SUCCESS -> local.update { it.copy(showDeleteDialog = false, outcome = EditOutcome.DELETED) }
                DeleteOrderResult.WINDOW_EXPIRED -> local.update { it.copy(showDeleteDialog = false, deleteExpired = true) }
                DeleteOrderResult.NOT_FOUND -> local.update { it.copy(showDeleteDialog = false, order = null) }
                DeleteOrderResult.HAS_DEPENDENCIES -> local.update { it.copy(showDeleteDialog = false, deleteBlocked = true) }
            }
        }
    }

    fun askCancel() = local.update { it.copy(showCancelDialog = true, cancelFailed = false) }
    fun dismissCancel() = local.update { it.copy(showCancelDialog = false) }

    fun confirmCancel(reason: String) {
        val order = local.value.order ?: return
        if (reason.isBlank() || local.value.isSaving) return
        viewModelScope.launch {
            local.update { it.copy(isSaving = true) }
            val ok = repository.cancelOrder(order.id, reason)
            local.update {
                if (ok) it.copy(isSaving = false, showCancelDialog = false, outcome = EditOutcome.CANCELLED)
                else it.copy(isSaving = false, showCancelDialog = false, cancelFailed = true)
            }
        }
    }

    private companion object {
        /** 11200.0 → "11200", 850.5 → "850.5" (no grouping, so it can be edited). */
        fun formatKgForInput(kg: Double): String =
            NumberFormat.getNumberInstance(Locale.ROOT).apply {
                maximumFractionDigits = 1
                isGroupingUsed = false
            }.format(kg)
    }
}
