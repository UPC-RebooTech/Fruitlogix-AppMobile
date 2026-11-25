package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.qualityControl.stock.data.NewProduct
import com.rebootech.fruitlogix.producer.qualityControl.stock.data.ProducerProductSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductField { NAME, FRUIT_TYPE, PRICE, AVAILABLE, MIN_ORDER }

data class NewProductForm(
    val name: String = "",
    val fruitType: String = "",
    val variety: String = "",
    val description: String = "",
    val unitPrice: String = "",
    val availableKg: String = "",
    val minOrderKg: String = ""
)

data class NewProductUiState(
    val form: NewProductForm = NewProductForm(),
    /** Fields with a problem: required or not a valid number. */
    val errors: Set<ProductField> = emptySet(),
    val isSaving: Boolean = false,
    /** The server refused it: its own text (blank = generic message). */
    val serverError: String? = null,
    val saved: Boolean = false
)

/** Publishes a product in the producer's catalog (POST /products). Clients order from it. */
class NewProductViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NewProductUiState())
    val uiState: StateFlow<NewProductUiState> = _uiState.asStateFlow()

    fun onChange(field: ProductField?, transform: (NewProductForm) -> NewProductForm) = _uiState.update {
        it.copy(form = transform(it.form), errors = if (field != null) it.errors - field else it.errors, serverError = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val form = state.form
        val price = parse(form.unitPrice)
        val available = parse(form.availableKg)
        val minOrder = parse(form.minOrderKg)
        val errors = buildSet {
            if (form.name.isBlank()) add(ProductField.NAME)
            if (form.fruitType.isBlank()) add(ProductField.FRUIT_TYPE)
            if (price == null || price <= 0.0) add(ProductField.PRICE)
            if (available == null || available < 0.0) add(ProductField.AVAILABLE)
            if (minOrder == null || minOrder <= 0.0) add(ProductField.MIN_ORDER)
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, serverError = null) }
            val result = ProducerProductSync.create(
                NewProduct(
                    name = form.name,
                    fruitType = form.fruitType,
                    variety = form.variety,
                    description = form.description,
                    unitPrice = price!!,
                    availableKg = available!!,
                    minOrderKg = minOrder!!
                )
            )
            _uiState.update {
                it.copy(isSaving = false, saved = result.ok, serverError = if (result.ok) null else result.serverMessage ?: "")
            }
        }
    }

    private fun parse(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()
}
