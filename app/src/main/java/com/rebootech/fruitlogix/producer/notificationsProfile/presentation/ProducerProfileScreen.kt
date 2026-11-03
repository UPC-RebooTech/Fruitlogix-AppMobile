package com.rebootech.fruitlogix.producer.notificationsProfile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ProducerProfileScreen(
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Text(
            text = "Perfil del Fundo",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "DATOS DE CONTACTO, RUC Y CERTIFICACIONES",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        FruitLogixCard(
            variant = CardVariant.Dark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Fundo Agrícola Los Laureles S.A.C.",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RUC: 20491823901 • Ica, Valle Hermoso",
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Certificación GlobalGAP: Válida hasta Octubre 2027",
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SecondaryButton(
            text = "Cambiar de Rol (Cerrar Sesión de Productor)",
            onClick = onSwitchRole,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

