package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerItem
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerKpiSummary
import com.rebootech.fruitlogix.shared.ui.theme.*

@Composable
fun ProducersScreen(
    modifier: Modifier = Modifier,
    viewModel: ProducersViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showRegisterDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var producerToEdit by remember { mutableStateOf<ProducerItem?>(null) }
    var producerToDelete by remember { mutableStateOf<ProducerItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera y botones de acción (US05 y US41)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gestión de Productores",
                        color = ColorTextOnLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Red de abastecimiento y fichas técnicas",
                        color = ColorAppbar,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearMessages()
                            showInviteDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorSurfaceDark),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Invitar Link", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.clearMessages()
                            showRegisterDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("+ Registrar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Banner de confirmación si existe
        state.feedbackMessage?.let { msg ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, ColorPrimary, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = msg,
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK", color = ColorTextOnDark, fontSize = 12.sp)
                    }
                }
            }
        }

        // KPIs de Gestión de Productores (US42)
        item {
            ProducerKpisGrid(kpis = state.kpis)
        }

        // Buscador
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Buscar por productor, fundo, valle o fruta...", color = ColorTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTextOnDark,
                    unfocusedTextColor = ColorTextOnDark,
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorAppbar,
                    focusedContainerColor = ColorSurfaceDark,
                    unfocusedContainerColor = ColorSurfaceDark
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Lista de productores o estado vacío (US06 Escenario 1 y 2)
        if (state.filteredProducers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurfaceDark)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No se encontraron productores registrados",
                            color = ColorTextOnDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Usa '+ Registrar' o 'Invitar Link' para vincular proveedores a tu red.",
                            color = ColorTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(state.filteredProducers, key = { it.id }) { producer ->
                ProducerCardItem(
                    producer = producer,
                    onEditClick = {
                        viewModel.clearMessages()
                        producerToEdit = producer
                    },
                    onDeleteClick = {
                        viewModel.clearMessages()
                        producerToDelete = producer
                    },
                    onContactClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:${producer.phone}")
                        }
                        context.startActivity(dialIntent)
                    }
                )
            }
        }
    }

    // Modal US05: Registrar productor con tipado estricto (Individual / Company)
    if (showRegisterDialog) {
        RegisterProducerDialog(
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                showRegisterDialog = false
            },
            onConfirm = { name, farm, loc, crop, email, phone, entityType ->
                viewModel.registerProducer(
                    name = name,
                    farmName = farm,
                    location = loc,
                    cropType = crop,
                    email = email,
                    phone = phone,
                    entityType = entityType,
                    onSuccess = { showRegisterDialog = false }
                )
            }
        )
    }

    // Modal US41: Invitar productor a la red (Correo o Share Sheet WhatsApp)
    if (showInviteDialog) {
        InviteProducerShareDialog(
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                showInviteDialog = false
            },
            onSendInvite = { name, contact, loc, crop ->
                viewModel.inviteProducer(
                    name = name,
                    emailOrPhone = contact,
                    location = loc,
                    cropType = crop,
                    onShareLinkReady = { shareText ->
                        showInviteDialog = false
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Invitación a FruitLogix")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Enviar invitación al productor vía")
                        )
                    }
                )
            }
        )
    }

    // Modal US39: Editar ficha técnica de productor
    producerToEdit?.let { current ->
        EditProducerDialog(
            producer = current,
            errorMessage = state.errorMessage,
            onDismiss = {
                viewModel.clearMessages()
                producerToEdit = null
            },
            onSave = { updated ->
                viewModel.updateProducer(
                    updated = updated,
                    onSuccess = { producerToEdit = null }
                )
            }
        )
    }

    // Modal US40: Eliminar productor con confirmación
    producerToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { producerToDelete = null },
            containerColor = ColorSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Eliminar Productor", color = ColorTextOnDark, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Estás seguro de eliminar a '${target.name}' (${target.farmName}) de tu red activa de proveedores?",
                    color = ColorTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProducer(target.id)
                        producerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorDangerStrong,
                        contentColor = ColorTextOnDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { producerToDelete = null }) {
                    Text("Cancelar", color = ColorTextMuted)
                }
            }
        )
    }
}

@Composable
private fun ProducerKpisGrid(kpis: ProducerKpiSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProducerKpiTile(
                label = "TOTAL PRODUCTORES",
                value = kpis.totalProducers.toString(),
                badge = "Red activa",
                modifier = Modifier.weight(1f)
            )
            ProducerKpiTile(
                label = "CALIDAD PROMEDIO",
                value = "${kpis.averageQuality} ★",
                badge = "Óptimo",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProducerKpiTile(
                label = "PRODUCCIÓN SEMANAL",
                value = "${kpis.weeklyProductionTons} tn",
                badge = "Abastecimiento",
                modifier = Modifier.weight(1f)
            )
            ProducerKpiTile(
                label = "INCIDENCIAS (30D)",
                value = kpis.incidentsLast30d.toString(),
                badge = if (kpis.incidentsLast30d == 0) "Sin alertas" else "En revisión",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProducerKpiTile(
    label: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurfaceDark)
            .border(1.dp, ColorAppbar, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = ColorTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                color = ColorTextOnDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = badge,
                color = ColorPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProducerCardItem(
    producer: ProducerItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val initials = producer.name
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ColorSurfaceDark)
            .border(1.dp, ColorAppbar, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (producer.isPending) Color(0xFF332B14) else ColorPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = if (producer.isPending) ColorWarning else ColorOnPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producer.name,
                        color = ColorTextOnDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorAppbar)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = producer.entityType.backendValue,
                            color = ColorPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (producer.isPending) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF332B14))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pendiente",
                                color = ColorWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "${producer.farmName} • ${producer.location}",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Especialidad: ${producer.cropType} • ★ ${producer.qualityRating}",
                    color = ColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ColorAppbar, thickness = 0.7.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "STOCK ACTUAL: ${producer.currentStockSummary}",
            color = ColorTextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${producer.activeOrdersCount} PEDIDOS ASIGNADOS",
                color = ColorPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onContactClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Contactar", color = ColorTextOnDark, fontSize = 11.sp)
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Editar ficha",
                        tint = ColorPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Eliminar productor",
                        tint = ColorDangerStrong,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterProducerDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        farm: String,
        location: String,
        crop: String,
        email: String,
        phone: String,
        entityType: ProducerEntityType
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var farm by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProducerEntityType.INDIVIDUAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Registrar Productor (US05)", color = ColorTextOnDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selecciona la naturaleza comercial estricta (Enum para el Backend):",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProducerEntityType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ColorPrimary else ColorAppbar)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.backendValue,
                                color = if (isSelected) ColorOnPrimary else ColorTextOnDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Text(text = it, color = ColorDangerStrong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                DialogField("Nombre o Razón Social *", name) { name = it }
                DialogField("Nombre del Fundo / Parcela *", farm) { farm = it }
                DialogField("Ubicación / Valle (ej. Ica, Perú) *", location) { location = it }
                DialogField("Cultivo Principal (ej. Palta Hass) *", crop) { crop = it }
                DialogField("Correo Electrónico", email) { email = it }
                DialogField("Teléfono", phone) { phone = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, farm, location, crop, email, phone, selectedType) },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar (${selectedType.backendValue})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun InviteProducerShareDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSendInvite: (name: String, emailOrPhone: String, location: String, crop: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Invitar Productor a la Red (US41)", color = ColorTextOnDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Envía un enlace directo de registro por WhatsApp o correo electrónico mediante el Share Sheet nativo.",
                    color = ColorTextMuted,
                    fontSize = 12.sp
                )
                errorMessage?.let {
                    Text(text = it, color = ColorDangerStrong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                DialogField("Nombre del Productor *", name) { name = it }
                DialogField("Correo o Celular (+51...) *", contact) { contact = it }
                DialogField("Valle / Ubicación", location) { location = it }
                DialogField("Cultivo Principal", crop) { crop = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSendInvite(name, contact, location, crop)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Compartir Invitación", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun EditProducerDialog(
    producer: ProducerItem,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (ProducerItem) -> Unit
) {
    var name by remember { mutableStateOf(producer.name) }
    var farm by remember { mutableStateOf(producer.farmName) }
    var location by remember { mutableStateOf(producer.location) }
    var crop by remember { mutableStateOf(producer.cropType) }
    var stock by remember { mutableStateOf(producer.currentStockSummary) }
    var entityType by remember { mutableStateOf(producer.entityType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Editar Ficha Técnica (US39)", color = ColorTextOnDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                errorMessage?.let {
                    Text(text = it, color = ColorDangerStrong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                DialogField("Nombre o Razón Social *", name) { name = it }
                DialogField("Fundo / Parcela *", farm) { farm = it }
                DialogField("Ubicación *", location) { location = it }
                DialogField("Especialidad / Cultivo *", crop) { crop = it }
                DialogField("Resumen de Stock", stock) { stock = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        producer.copy(
                            name = name,
                            farmName = farm,
                            location = location,
                            cropType = crop,
                            currentStockSummary = stock,
                            entityType = entityType
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = ColorOnPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextMuted)
            }
        }
    )
}

@Composable
private fun DialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ColorTextMuted, fontSize = 11.sp) },
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
        shape = RoundedCornerShape(10.dp)
    )
}