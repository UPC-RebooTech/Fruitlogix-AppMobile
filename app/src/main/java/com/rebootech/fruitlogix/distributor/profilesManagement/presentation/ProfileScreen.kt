package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.data.FakeProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.DistributorProfile
import com.rebootech.fruitlogix.shared.ui.theme.*

@Composable
fun ProfileScreen(
    onSwitchRole: () -> Unit = {}, // <--- Añadido parámetro para cambiar de rol
    modifier: Modifier = Modifier
) {
    val savedProfile by FakeProfilesRepository.distributorProfile.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var hasLoadError by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var fullName by remember(savedProfile) { mutableStateOf(savedProfile.fullName) }
    var roleTitle by remember(savedProfile) { mutableStateOf(savedProfile.roleTitle) }
    var companyHub by remember(savedProfile) { mutableStateOf(savedProfile.companyHub) }
    var email by remember(savedProfile) { mutableStateOf(savedProfile.email) }
    var phone by remember(savedProfile) { mutableStateOf(savedProfile.phone) }
    var ruc by remember(savedProfile) { mutableStateOf(savedProfile.ruc) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        if (hasLoadError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorDangerStrong, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fallo al cargar el perfil",
                        color = ColorTextOnDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se pudo establecer comunicación con el servidor para obtener tus datos personales.",
                        color = ColorTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { hasLoadError = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reintentar carga", fontWeight = FontWeight.Bold)
                    }
                }
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header: Avatar e Información Principal
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
                        contentDescription = "Avatar",
                        tint = ColorOnPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = savedProfile.fullName,
                        color = ColorTextOnDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = roleTitle,
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = companyHub,
                        color = ColorTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            errorMessage?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorDanger)
                        .padding(12.dp)
                ) {
                    Text(text = err, color = ColorTextOnDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(text = msg, color = ColorPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Sección de Detalle de Contacto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorSurfaceDark)
                    .border(1.dp, ColorAppbar, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "DATOS DE PERFIL Y CONTACTO",
                    color = ColorTextMuted,
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
                    ProfileTextField(label = "RUC Empresa / Operación (11 dígitos)", value = ruc, onValueChange = { ruc = it })
                } else {
                    ProfileInfoRow(label = "Nombre", value = savedProfile.fullName)
                    ProfileInfoRow(label = "Correo", value = savedProfile.email)
                    ProfileInfoRow(label = "Teléfono", value = savedProfile.phone)
                    ProfileInfoRow(label = "RUC Operativo", value = savedProfile.ruc)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Acción Principal (Editar / Guardar)
            Button(
                onClick = {
                    if (isEditing) {
                        val result = FakeProfilesRepository.updateDistributorProfile(
                            DistributorProfile(
                                fullName = fullName,
                                roleTitle = roleTitle,
                                companyHub = companyHub,
                                email = email,
                                phone = phone,
                                ruc = ruc
                            )
                        )
                        result.fold(
                            onSuccess = {
                                errorMessage = null
                                statusMessage = "Perfil actualizado exitosamente."
                                isEditing = false
                            },
                            onFailure = { err ->
                                statusMessage = null
                                errorMessage = err.message
                            }
                        )
                    } else {
                        errorMessage = null
                        statusMessage = null
                        isEditing = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorOnPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEditing) R.drawable.ic_save else R.drawable.ic_edit
                    ),
                    contentDescription = null,
                    tint = ColorOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Guardar Cambios" else "Editar Perfil",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Secundario de Cambiar de Rol (Cerrar Sesión / Elegir cuenta)
            Button(
                onClick = onSwitchRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorSurfaceDark,
                    contentColor = ColorTextOnDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Cambiar de Rol (Cerrar Sesión de Distribuidor)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
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

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ColorTextMuted) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ColorTextOnDark,
            unfocusedTextColor = ColorTextOnDark,
            focusedBorderColor = ColorPrimary,
            unfocusedBorderColor = ColorAppbar,
            focusedContainerColor = ColorAppbar,
            unfocusedContainerColor = ColorAppbar
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}