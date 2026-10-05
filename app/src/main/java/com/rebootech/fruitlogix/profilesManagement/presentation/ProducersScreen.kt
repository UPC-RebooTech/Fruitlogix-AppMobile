package com.rebootech.fruitlogix.profilesManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

data class Producer(
    val id: String,
    val name: String,
    val farmName: String,
    val location: String,
    val cropType: String,
    val isPending: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducersScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showInviteDialog by remember { mutableStateOf(false) }

    val producersList = remember {
        mutableStateListOf(
            Producer("1", "Agropecuaria El Valle", "Fundo Las Lilas", "Ica, Perú", "Palta Hass"),
            Producer("2", "Miguel Ángel Torres", "Finca El Pedregal", "Chanchamayo, Junín", "Cítricos y Café"),
            Producer("3", "Agrícola Olmos S.A.C.", "Sector B3", "Lambayeque, Perú", "Arándanos & Mango"),
            Producer("4", "Hacienda San José", "Fundo Central", "Huaral, Lima", "Mandarina Satsuma"),
            Producer("5", "Campos del Sur", "Valle de Majes", "Arequipa, Perú", "Uva de Mesa", isPending = true)
        )
    }

    val filteredProducers = producersList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true) ||
                it.cropType.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Red de Productores",
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${producersList.size} productores vinculados",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showInviteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentLime,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ Invitar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre, valle o cultivo...", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = AccentLime,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducers) { producer ->
                    ProducerCard(producer = producer)
                }
            }
        }

        if (showInviteDialog) {
            InviteProducerDialog(
                onDismiss = { showInviteDialog = false },
                onSendInvite = { newName, newLocation, newCrop ->
                    producersList.add(
                        0,
                        Producer(
                            id = (producersList.size + 1).toString(),
                            name = newName,
                            farmName = "Fundo Nuevo",
                            location = newLocation,
                            cropType = newCrop,
                            isPending = true
                        )
                    )
                    showInviteDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProducerCard(producer: Producer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (producer.isPending) Color(0xFF332B14) else CardBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                tint = if (producer.isPending) Color(0xFFFFC107) else AccentLime,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producer.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (producer.isPending) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF332B14))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Pendiente", color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "${producer.farmName} • ${producer.location}",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cultivo: ${producer.cropType}",
                color = AccentLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteProducerDialog(
    onDismiss: () -> Unit,
    onSendInvite: (name: String, location: String, crop: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var cropInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp), // Fix: define la forma de la ventana emergente correctamente
        containerColor = CardBackground,
        title = {
            Text(
                text = "Invitar Productor a la Red",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ingresa los datos del productor agrícola para enviar la invitación de enlace a FruitLogix.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre o Razón Social", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentLime,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    label = { Text("Ubicación / Fundo (ej. Huaral, Lima)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentLime,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cropInput,
                    onValueChange = { cropInput = it },
                    label = { Text("Cultivo Principal (ej. Palta Hass)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentLime,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSendInvite(
                            nameInput,
                            locationInput.ifBlank { "Sin ubicación" },
                            cropInput.ifBlank { "General" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = BackgroundDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar Invitación", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}