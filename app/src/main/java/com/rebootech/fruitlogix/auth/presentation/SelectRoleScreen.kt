package com.rebootech.fruitlogix.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun SelectRoleScreen(
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectRoleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Logo / Icon header
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(FruitLogixTheme.colors.surfaceDark),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_sensor_waves,
                contentDescription = null,
                tint = FruitLogixTheme.colors.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FruitLogix",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = FruitLogixTheme.colors.textOnLight
        )

        Text(
            text = "PLATAFORMA INTEGRAL AGROINDUSTRIAL",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Selecciona tu perfil de acceso para ingresar a las operaciones:",
            style = FruitLogixTheme.typography.bodyLarge,
            color = FruitLogixTheme.colors.textOnLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Role 1: DISTRIBUTOR
        RoleOptionCard(
            title = "Distribuidor / Exportador",
            subtitle = "LOGÍSTICA Y CENTRO DE COMANDO",
            description = "Monitoreo de cadena de frío en tiempo real, control de flota, geocercas, gestión de pedidos y liquidación a productores.",
            iconId = R.drawable.ic_fleet,
            isSelected = uiState.selectedRole == UserRole.DISTRIBUTOR,
            onClick = {
                viewModel.onSelectRole(UserRole.DISTRIBUTOR, onRoleSelected)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role 2: PRODUCER
        RoleOptionCard(
            title = "Productor Agrícola",
            subtitle = "OPERACIÓN EN FUNDO Y COSECHA",
            description = "Registro de cosechas en campo, control de stock y pallets en almacén, recepción de órdenes de compra y seguimiento de liquidaciones.",
            iconId = R.drawable.ic_producers,
            isSelected = uiState.selectedRole == UserRole.PRODUCER,
            onClick = {
                viewModel.onSelectRole(UserRole.PRODUCER, onRoleSelected)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    description: String,
    iconId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .clickable(onClick = onClick),
        shape = FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = iconId,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.textOnDark
                    )
                    Text(
                        text = subtitle,
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Ingresar como $title",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

