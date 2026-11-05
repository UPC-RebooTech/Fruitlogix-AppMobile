package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun OrderSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onScanClick: () -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(FruitLogixTheme.colors.surfaceLight)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_troubleshoot,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.producer_orders_search_placeholder),
                            style = FruitLogixTheme.typography.bodyMedium,
                            color = FruitLogixTheme.colors.textMuted,
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = FruitLogixTheme.typography.bodyMedium.copy(
                            color = FruitLogixTheme.colors.textOnLight
                        ),
                        cursorBrush = SolidColor(FruitLogixTheme.colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Scanner icon button (navigates to ScanLotAny)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onScanClick),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_sensor_waves,
                        contentDescription = "Scan Lot",
                        tint = FruitLogixTheme.colors.textOnLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Filter Button with active indicator dot
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FruitLogixTheme.colors.surfaceLight)
                .clickable(onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_menu,
                contentDescription = "Filter Orders",
                tint = FruitLogixTheme.colors.textOnLight,
                modifier = Modifier.size(20.dp)
            )

            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.primary)
                )
            }
        }
    }
}