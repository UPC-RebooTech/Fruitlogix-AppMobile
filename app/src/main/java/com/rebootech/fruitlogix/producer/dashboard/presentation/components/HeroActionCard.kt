package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.StockShortage
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Highlighted "next action" card (inverseSurface). One main action (accept, Lima pill)
 * and one text action (view details), both full width for easy tapping in the field.
 */
@Composable
fun HeroActionCard(
    order: Order?,
    shortage: StockShortage?,
    onAcceptClick: (Order) -> Unit,
    onReviewDetailsClick: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors

    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (order == null) {
                Text(
                    text = stringResource(R.string.producer_home_all_caught_up_title),
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.inverseOnSurface
                )
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                Text(
                    text = stringResource(R.string.producer_home_all_caught_up_body),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = colors.onInverseSurfaceVariant
                )
                return@Column
            }

            val hoursLeft = order.respondByHoursLeft ?: 2

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = stringResource(R.string.producer_home_next_action),
                    type = StatusBadgeType.HIGHLIGHT,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_schedule,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
                    Text(
                        text = stringResource(R.string.producer_home_hours_left, hoursLeft),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = colors.warning,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            Text(
                text = stringResource(R.string.producer_home_order_title, order.id),
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.inversePrimary
            )
            Text(
                text = order.clientName,
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.inverseOnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))

            Text(
                text = order.items.joinToString(" · ") { "${it.productName} ${it.kg} kg" },
                style = FruitLogixTheme.typography.bodyMedium,
                color = colors.onInverseSurfaceVariant
            )
            Text(
                text = stringResource(R.string.producer_home_reply_within, hoursLeft),
                style = FruitLogixTheme.typography.bodySmall,
                color = colors.onInverseSurfaceVariant
            )

            if (shortage != null) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        id = R.drawable.ic_warning,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    Text(
                        text = stringResource(
                            R.string.producer_orders_short_by,
                            shortage.shortKg,
                            shortage.productName
                        ),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = colors.warning
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

            PrimaryButton(
                text = stringResource(R.string.producer_orders_btn_accept),
                onClick = { onAcceptClick(order) },
                modifier = Modifier.fillMaxWidth()
            )
            TextActionButton(
                text = stringResource(R.string.producer_orders_btn_view_details),
                onClick = { onReviewDetailsClick(order) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

