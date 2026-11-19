package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Bottom sheet to pick the order in preparation to inspect. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPickerSheet(
    orders: List<Order>,
    onOrderSelected: (Order) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surfaceContainerLow,
        shape = FruitLogixTheme.shapes.BottomSheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FruitLogixTheme.spacing.md)
        ) {
            Text(
                text = stringResource(R.string.producer_home_qa_new_inspection),
                style = FruitLogixTheme.typography.titleLarge,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            Text(
                text = stringResource(R.string.producer_home_picker_subtitle),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

            LazyColumn {
                items(orders) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
                            .clickable { onOrderSelected(order) }
                            .padding(vertical = FruitLogixTheme.spacing.inner),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            id = R.drawable.ic_orders,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                        )
                        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${stringResource(R.string.producer_home_order_title, order.id)} · ${order.clientName}",
                                style = FruitLogixTheme.typography.titleSmall,
                                color = scheme.onSurface
                            )
                            Text(
                                text = order.items.joinToString(", ") { "${it.productName} ${it.kg} kg" },
                                style = FruitLogixTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        AppIcon(
                            id = R.drawable.ic_chevron_right,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.md))
        }
    }
}
