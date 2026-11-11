package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.CompactKpiTile
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat

/**
 * Three KPI tiles at the top of Orders (inverseSurface, same rule as the Home KPIs).
 */
@Composable
fun OrderSummaryTiles(
    awaitingCount: Int,
    inShedCount: Int,
    activeVolumeKg: Int,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
    ) {
        CompactKpiTile(
            label = stringResource(R.string.producer_orders_tile_awaiting_action),
            value = pluralStringResource(R.plurals.producer_orders_tile_orders, awaitingCount, awaitingCount),
            modifier = Modifier.weight(1f)
        )
        CompactKpiTile(
            label = stringResource(R.string.producer_orders_tile_in_shed),
            value = stringResource(R.string.producer_orders_tile_active, inShedCount),
            modifier = Modifier.weight(1f)
        )
        CompactKpiTile(
            label = stringResource(R.string.producer_orders_tile_active_vol),
            value = stringResource(
                R.string.producer_orders_tile_volume_format,
                NumberFormat.getNumberInstance(locale).format(activeVolumeKg)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
