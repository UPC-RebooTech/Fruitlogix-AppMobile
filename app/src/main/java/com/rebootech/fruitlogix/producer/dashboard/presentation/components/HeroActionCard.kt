package com.rebootech.fruitlogix.producer.dashboard.presentation.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun HeroActionCard(
    order: Order?,
    shortageWarning: String?,
    onAcceptClick: (Order) -> Unit,
    onReviewDetailsClick: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    FruitLogixCard(
        variant = CardVariant.White,
        modifier = modifier.fillMaxWidth()
    ) {
        if (order == null) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Todo al día",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No tienes pedidos pendientes de respuesta.",
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Franja izquierda lima de 6dp
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(180.dp)
                        .background(FruitLogixTheme.colors.primary)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    // Badge y cuenta regresiva
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = "ACCION URGENTE",
                            type = StatusBadgeType.HIGHLIGHT,
                            icon = {
                                AppIcon(
                                    id = R.drawable.ic_alert_diamond,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(
                                id = R.drawable.ic_schedule,
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.textOnLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${order.respondByHoursLeft ?: 2} h restantes",
                                style = FruitLogixTheme.typography.labelSmall,
                                color = FruitLogixTheme.colors.textOnLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pedido ${order.id} • ${order.clientName}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val itemsSummary = order.items.joinToString(" + ") { "${it.productName} ${it.kg} kg" }
                    Text(
                        text = "$itemsSummary — responder en ${order.respondByHoursLeft ?: 2} h",
                        style = FruitLogixTheme.typography.bodySmall,
                        color = FruitLogixTheme.colors.textMuted
                    )

                    if (shortageWarning != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠ $shortageWarning",
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.warning,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryButton(
                            text = "Aceptar Pedido",
                            onClick = { onAcceptClick(order) },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Ver detalles",
                            onClick = { onReviewDetailsClick(order) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

