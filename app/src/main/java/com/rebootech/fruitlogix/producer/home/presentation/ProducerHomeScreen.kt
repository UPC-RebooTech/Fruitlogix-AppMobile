package com.rebootech.fruitlogix.producer.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ProducerHomeScreen(
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fundo Los Laureles",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.displayLarge,
                    color = FruitLogixTheme.colors.textOnLight
                )
                Text(
                    text = "ROL: PRODUCTOR AGRÍCOLA",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FruitLogixTheme.colors.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_producers,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Summary Card (Dark)
        FruitLogixCard(
            variant = CardVariant.Dark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RESUMEN DE OPERACIONES EN CAMPO",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "4 Lotes Activos en Cosecha",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleLarge,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Uva Red Globe y Palta Hass listas para despacho al centro de empaque.",
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(label = "PALLETS LISTOS", value = "28", modifier = Modifier.weight(1f))
                    MetricBox(label = "ÓRDENES PENDIENTES", value = "3", modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Info Card (White Variant)
        FruitLogixCard(
            variant = CardVariant.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FruitLogixTheme.colors.surfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            id = R.drawable.ic_orders,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.surfaceDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Órdenes de Distribución",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = FruitLogixTheme.typography.titleMedium,
                            color = FruitLogixTheme.colors.textOnLight
                        )
                        Text(
                            text = "Última sincronización con exportador hace 12 min",
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch role button
        SecondaryButton(
            text = "Cambiar de Rol (Volver a Selector)",
            onClick = onSwitchRole,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FruitLogixTheme.colors.appbar)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = FruitLogixTheme.colors.primary
            )
        }
    }
}

