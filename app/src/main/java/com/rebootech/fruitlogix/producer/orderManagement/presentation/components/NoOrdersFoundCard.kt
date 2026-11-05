package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun NoOrdersFoundCard(
    onClearFilters: () -> Unit,
    onApplyInPrep: () -> Unit,
    onApplyInTransit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Empty Filter Card
        FruitLogixCard(
            variant = CardVariant.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle Tint Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FruitLogixTheme.colors.surfaceTint),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_diagnostic,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.textOnLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.producer_orders_empty_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleLarge,
                    color = FruitLogixTheme.colors.textOnLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.producer_orders_no_results_desc),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                SecondaryButton(
                    text = stringResource(R.string.producer_orders_clear_filters),
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tip row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIcon(
                        id = R.drawable.ic_link,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.producer_orders_filter_tip),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Suggested Views Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FruitLogixTheme.shapes.Card)
                .background(FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.45f))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.producer_orders_suggested_views),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleSmall,
                color = FruitLogixTheme.colors.textOnLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SuggestedViewChip(
                    label = stringResource(R.string.producer_orders_suggested_in_prep),
                    iconRes = R.drawable.ic_orders,
                    onClick = onApplyInPrep,
                    modifier = Modifier.weight(1f)
                )

                SuggestedViewChip(
                    label = stringResource(R.string.producer_orders_suggested_in_transit),
                    iconRes = R.drawable.ic_truck_pin,
                    onClick = onApplyInTransit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SuggestedViewChip(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FruitLogixTheme.colors.white)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textOnLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = FruitLogixTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FruitLogixTheme.colors.textOnLight,
                maxLines = 1
            )
        }
    }
}