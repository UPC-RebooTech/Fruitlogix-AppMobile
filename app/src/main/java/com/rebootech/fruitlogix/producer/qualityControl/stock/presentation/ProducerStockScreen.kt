package com.rebootech.fruitlogix.producer.qualityControl.stock.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ProducerStockScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRegisterHarvest: () -> Unit,
    onNavigateToHarvestsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stockItems by remember {
        mutableStateOf(
            listOf(
                StockItem("lot-104", "Kent Mango", 315, 95, 100),
                StockItem("lot-882", "Hass Avocado", 420, 130, 150),
                StockItem("lot-094", "Blueberries", 140, 95, 150),
                StockItem("lot-402", "Red Globe Grapes", 85, 71, 100)
            )
        )
    }

    val totalAvailable = stockItems.sumOf { it.availableKg }
    val criticalCount = stockItems.count { it.status == StockStatus.CRITICAL }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(16.dp)
    ) {
        // Título Principal
        Text(
            text = "Stock en Fundo",
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "INVENTARIO DE CAMPO Y CÁMARAS LOCALES",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Resumen Superior
        FruitLogixCard(
            variant = CardVariant.Dark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AVAILABLE", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text("$totalAvailable kg", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.primary)
                }
                Column {
                    Text("ACTIVE SKUS", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text("${stockItems.size} lots", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.white)
                }
                Column {
                    Text("CRITICAL", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text("$criticalCount item", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.dangerStrong)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pestañas (Inventory activo aquí, Harvests navega a su ruta)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FruitLogixTheme.colors.surfaceTint)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StockTabButton(
                text = "Inventory (${stockItems.size})",
                selected = true,
                modifier = Modifier.weight(1f)
            ) { }
            StockTabButton(
                text = "Harvests (12)",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHarvestsTab
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Inventario de Stock
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stockItems) { item ->
                InventoryCardItem(
                    item = item,
                    onIncrement = {
                        stockItems = stockItems.map {
                            if (it.productId == item.productId) it.copy(availableKg = it.availableKg + 5) else it
                        }
                    },
                    onDecrement = {
                        stockItems = stockItems.map {
                            if (it.productId == item.productId) it.copy(availableKg = (it.availableKg - 5).coerceAtLeast(0)) else it
                        }
                    },
                    onClick = { onNavigateToDetail(item.productId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Registrar Cosecha
        Button(
            onClick = onNavigateToRegisterHarvest,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "+ Register Harvest",
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.onPrimary
            )
        }
    }
}

@Composable
fun StockTabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val backgroundColor = if (selected) FruitLogixTheme.colors.surfaceDark else Color.Transparent
    val textColor = if (selected) FruitLogixTheme.colors.white else FruitLogixTheme.colors.textOnLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, style = FruitLogixTheme.typography.bodyMedium)
    }
}

@Composable
fun InventoryCardItem(
    item: StockItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    FruitLogixCard(
        variant = CardVariant.Dark,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.productName,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.white,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FruitLogixTheme.colors.surfaceTint),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable(onClick = onDecrement)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "-", color = FruitLogixTheme.colors.textOnLight, style = FruitLogixTheme.typography.bodyMedium)
                    }
                    Box(
                        modifier = Modifier
                            .background(FruitLogixTheme.colors.primary)
                            .clickable(onClick = onIncrement)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = FruitLogixTheme.colors.onPrimary, style = FruitLogixTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val isCritical = item.status == StockStatus.CRITICAL
            val statusText = if (isCritical) "DEFICIT -15KG" else "HEALTHY"
            val statusColor = if (isCritical) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.success

            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.availableKg} kg total",
                style = FruitLogixTheme.typography.titleLarge,
                color = FruitLogixTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { item.fillFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.status == StockStatus.CRITICAL) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.success,
                trackColor = FruitLogixTheme.colors.surfaceTint
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available: ${item.freeKg} kg", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                Text("Reserved: ${item.reservedKg} kg", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
            }
        }
    }
}