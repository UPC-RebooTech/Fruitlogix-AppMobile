package com.rebootech.fruitlogix.profilesManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R

private val BackgroundDark = Color(0xFF141A14)
private val CardBackground = Color(0xFF1E261D)
private val CardBorder = Color(0xFF2E382C)
private val AccentLime = Color(0xFFD2F852)
private val TextWhite = Color(0xFFF5F5F5)
private val TextMuted = Color(0xFF9EABA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("Carlos Mendoza") }
    var roleTitle by remember { mutableStateOf("Distribuidor Logístico / Admin") }
    var companyHub by remember { mutableStateOf("Lima Central Hub - Panamericana") }
    var email by remember { mutableStateOf("carlos.mendoza@fruitlogix.pe") }
    var phone by remember { mutableStateOf("+51 987 654 321") }
    var ruc by remember { mutableStateOf("20601234567") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Avatar",
                        tint = BackgroundDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = roleTitle,
                        color = AccentLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = companyHub,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "DATOS DE PERFIL Y CONTACTO",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    ProfileTextField(label = "Nombre Completo", value = fullName, onValueChange = { fullName = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileTextField(label = "Correo Electrónico", value = email, onValueChange = { email = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileTextField(label = "Teléfono de Contacto", value = phone, onValueChange = { phone = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileTextField(label = "RUC Empresa / Operación", value = ruc, onValueChange = { ruc = it })
                } else {
                    ProfileInfoRow(label = "Nombre", value = fullName)
                    ProfileInfoRow(label = "Correo", value = email)
                    ProfileInfoRow(label = "Teléfono", value = phone)
                    ProfileInfoRow(label = "RUC Operativo", value = ruc)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentLime,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEditing) R.drawable.ic_save else R.drawable.ic_edit
                    ),
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Guardar Cambios" else "Editar Perfil",
                    fontSize = 15.sp,
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
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = AccentLime,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = BackgroundDark,
            unfocusedContainerColor = BackgroundDark
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}