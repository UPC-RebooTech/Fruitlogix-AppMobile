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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun PendingAllCaughtUpCard(
    distributorName: String,
    inShedCount: Int,
    lastAcceptedOrder: Order?,
    lastAcceptedTime: LocalTime?,
    onViewInPrep: () -> Unit,
    onCheckStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main "All caught up" Card
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
                // Circle Tint Icon with checkmark badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_task_check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Green check indicator badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.producer_orders_caught_up_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.producer_orders_caught_up_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.producer_orders_caught_up_desc, distributorName),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Recent Activity Card (derived from last accepted order)
        if (lastAcceptedOrder != null) {
            RecentActivityCard(
                order = lastAcceptedOrder,
                timeText = lastAcceptedTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(LocalConfiguration.current.locales[0])).orEmpty()
            )
        }

        // Bottom actions: secondary filled + text action, both full width
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xxs)
        ) {
            SecondaryButton(
                text = stringResource(R.string.producer_orders_btn_view_in_prep, inShedCount),
                onClick = onViewInPrep,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    AppIcon(
                        id = R.drawable.ic_orders,
                        contentDescription = null,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
            )
            TextActionButton(
                text = stringResource(R.string.producer_orders_btn_check_stock),
                onClick = onCheckStock,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    AppIcon(
                        id = R.drawable.ic_warehouse,
                        contentDescription = null,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
            )
        }
    }
}

@Composable
private fun RecentActivityCard(
    order: Order,
    timeText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_orders,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.producer_orders_recent_activity),
                        style = FruitLogixTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = timeText,
                        style = FruitLogixTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.producer_orders_activity_accepted, order.id),
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                val itemsText = order.items.joinToString(" · ") { "${it.kg} kg ${it.productName}" }
                Text(
                    text = itemsText,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
