package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderSummaryTiles(
    awaitingCount: Int,
    inShedCount: Int,
    activeVolumeKg: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tile 1: Awaiting action
        SummaryTile(
            label = stringResource(R.string.producer_orders_tile_awaiting_action),
            value = if (awaitingCount == 1) {
                stringResource(R.string.producer_orders_tile_order_singular, awaitingCount)
            } else {
                stringResource(R.string.producer_orders_tile_orders_plural, awaitingCount)
            },
            modifier = Modifier.weight(1f)
        )

        // Tile 2: In shed
        SummaryTile(
            label = stringResource(R.string.producer_orders_tile_in_shed),
            value = stringResource(R.string.producer_orders_tile_active, inShedCount),
            modifier = Modifier.weight(1f)
        )

        // Tile 3: Active volume
        val formattedKg = NumberFormat.getNumberInstance(Locale.US).format(activeVolumeKg)
        SummaryTile(
            label = stringResource(R.string.producer_orders_tile_active_vol),
            value = stringResource(R.string.producer_orders_tile_volume_format, formattedKg),
            isHighlight = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    FruitLogixCard(
        variant = CardVariant.Tint,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textMuted,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = if (isHighlight) FruitLogixTheme.colors.textOnLight else FruitLogixTheme.colors.textOnLight,
                maxLines = 1
            )
        }
    }
}