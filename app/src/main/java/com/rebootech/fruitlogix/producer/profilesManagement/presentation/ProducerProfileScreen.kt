package com.rebootech.fruitlogix.producer.profilesManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.*

@Composable
fun ProducerProfileScreen(
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Título de la sección
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

            // Header: Avatar e Información del Fundo (Estilo similar a Distribuidores)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorSurfaceDark)
                    .border(1.dp, ColorAppbar, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Avatar Fundo",
                        tint = ColorOnPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fundo Agrícola Los Laureles S.A.C.",
                        color = ColorTextOnDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Productor Agrícola / Admin",
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Ica, Valle Hermoso",
                        color = ColorTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sección de Detalle (RUC y Certificaciones)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorSurfaceDark)
                    .border(1.dp, ColorAppbar, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "DETALLES DE OPERACIÓN Y LEGAL",
                    color = ColorTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileInfoRow(label = "RUC Operativo", value = "20491823901")
                ProfileInfoRow(label = "Ubicación", value = "Ica, Valle Hermoso")
                ProfileInfoRow(label = "Certificación GlobalGAP", value = "Válida hasta Octubre 2027")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para cambiar de rol / volver al inicio
            Button(
                onClick = onSwitchRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Cambiar de Rol (Cerrar Sesión de Productor)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = label, color = ColorTextMuted, fontSize = 11.sp)
        Text(text = value, color = ColorTextOnDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = ColorAppbar, thickness = 0.5.dp)
    }
}