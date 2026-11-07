package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun StockHealthSection(
    stockItems: List<StockItem>,
    onItemClick: (StockItem) -> Unit,
    onNavigateStockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FruitLogixCard(
        variant = CardVariant.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Salud del inventario",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                    Text(
                        text = "Actual vs umbral mínimo 100 kg",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateStockClick),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_arrow_forward,
                        contentDescription = "Ver stock",
                        tint = FruitLogixTheme.colors.textOnLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            stockItems.forEachIndexed { index, item ->
                StockLevelRow(
                    item = item,
                    onClick = { onItemClick(item) }
                )
                if (index < stockItems.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StockLevelRow(
    item: StockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.productName,
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnLight
                )
                if (item.status == StockStatus.LOW) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(
                        text = "Bajo",
                        type = StatusBadgeType.WARNING,
                        maxLines = 1,
                        softWrap = false
                    )
                } else if (item.status == StockStatus.CRITICAL) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(
                        text = "Crítico",
                        type = StatusBadgeType.DANGER,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.availableKg} kg",
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.status == StockStatus.CRITICAL) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.textOnLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(mín ${item.minThresholdKg})",
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra de stock con marca en 0.25 (umbral)
        val barColor = when (item.status) {
            StockStatus.CRITICAL -> FruitLogixTheme.colors.dangerStrong
            StockStatus.LOW -> FruitLogixTheme.colors.warning
            StockStatus.HEALTHY -> FruitLogixTheme.colors.success
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(FruitLogixTheme.colors.bg)
        ) {
            // Relleno
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.fillFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )

            // Línea de marca de umbral en 0.25 (25%)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.4f))
                )
            }
        }
    }
}

