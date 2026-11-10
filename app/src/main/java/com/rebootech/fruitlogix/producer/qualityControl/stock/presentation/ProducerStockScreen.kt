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
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockItemModel
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository

@Composable
fun ProducerStockScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRegisterHarvest: () -> Unit,
    onNavigateToHarvestsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockList = StockRepository.stockItems

    val totalAvailable = stockList.sumOf { it.availableKg }
    val criticalCount = stockList.count { it.status == "Critical" || it.availableKg < 100 }

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
                    Text("${stockList.size} lots", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.white)
                }
                Column {
                    Text("CRITICAL", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text("$criticalCount item", style = FruitLogixTheme.typography.titleLarge, color = FruitLogixTheme.colors.dangerStrong)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pestañas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FruitLogixTheme.colors.surfaceTint)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StockTabButton(
                text = "Inventory (${stockList.size})",
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

        // Lista de Inventario de Stock conectada al repositorio compartido
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stockList) { stock ->
                InventoryCardItem(
                    item = stock,
                    onIncrement = {
                        StockRepository.updateStockKg(stock.id, stock.availableKg + 10)
                    },
                    onDecrement = {
                        StockRepository.updateStockKg(stock.id, stock.availableKg - 10)
                    },
                    onClick = { onNavigateToDetail(stock.id) }
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
    item: StockItemModel,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    val fillFraction = (item.availableKg.toFloat() / item.totalCapacityKg.toFloat()).coerceIn(0f, 1f)
    val isCritical = item.availableKg < 100

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
                progress = { fillFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCritical) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.success,
                trackColor = FruitLogixTheme.colors.surfaceTint
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Location: ${item.chamberLocation}", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                Text("Variety: ${item.variety}", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
            }
        }
    }
}