package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPickerSheet(
    orders: List<Order>,
    onOrderSelected: (Order) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FruitLogixTheme.colors.surfaceDark,
        shape = FruitLogixTheme.shapes.BottomSheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FruitLogixTheme.spacing.md)
        ) {
            Text(
                text = "Nueva inspección",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.textOnDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selecciona el pedido en preparación que deseas inspeccionar:",
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(orders) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderSelected(order) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            id = R.drawable.ic_orders,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pedido ${order.id} • ${order.clientName}",
                                style = FruitLogixTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FruitLogixTheme.colors.textOnDark
                            )
                            Text(
                                text = order.items.joinToString { "${it.productName} ${it.kg} kg" },
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.textMuted
                            )
                        }
                        AppIcon(
                            id = R.drawable.ic_chevron_right,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

