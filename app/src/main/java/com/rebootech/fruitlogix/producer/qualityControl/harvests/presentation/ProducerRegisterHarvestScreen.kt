package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository

@Composable
fun ProducerRegisterHarvestScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var intakeWeight by remember { mutableStateOf(150) }
    var selectedVariety by remember { mutableStateOf("Kent Mango") }

    // Estados editables para turnos, sector y observaciones
    var shiftText by remember { mutableStateOf("Today, Oct 31, 2026 • Morning Shift (08:00 - 1)") }
    var farmPlotText by remember { mutableStateOf("Fundo Santa Rosa • Block 3B • Mango Grove") }
    var observationsText by remember { mutableStateOf("Firmness measured at 14.2 lbs/cm². Sugar brix baseline: 12.8%. Field pick crew #4 completed harvesting before morning heat peak.") }

    // Diálogos de edición rápida
    var showShiftDialog by remember { mutableStateOf(false) }
    var showPlotDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(16.dp)
    ) {
        // 1. Barra Superior con Título y Número de Lote abajo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Regresar",
                        tint = FruitLogixTheme.colors.textOnLight
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "STOCK INTAKE",
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textMuted
                    )
                    Text(
                        text = "Register Harvest",
                        style = FruitLogixTheme.typography.displayLarge.copy(fontSize = 24.sp),
                        color = FruitLogixTheme.colors.textOnLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = FruitLogixTheme.colors.surfaceLight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, FruitLogixTheme.colors.surfaceTint)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(FruitLogixTheme.colors.dangerStrong, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOT #H-2024-884",
                                style = FruitLogixTheme.typography.labelSmall,
                                color = FruitLogixTheme.colors.textOnLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Surface(
                color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ready to Save",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.success,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contenido Desplazable Principal
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A. Banner de Estado
            item {
                Surface(
                    color = FruitLogixTheme.colors.success.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FruitLogixTheme.colors.success.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Harvest registered. Stock updated.",
                                style = FruitLogixTheme.typography.bodyMedium,
                                color = FruitLogixTheme.colors.success,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Log daily orchard harvest directly into cold storage allocations.",
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.textMuted
                            )
                        }
                    }
                }
            }

            // B. Sección: Select Variety
            item {
                FruitLogixCard(
                    variant = CardVariant.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Variety (Active Cultivar)",
                                style = FruitLogixTheme.typography.titleMedium,
                                color = FruitLogixTheme.colors.textOnLight
                            )
                            Surface(
                                color = FruitLogixTheme.colors.surfaceTint,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "4 Cultivars",
                                    style = FruitLogixTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = FruitLogixTheme.colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CultivarGridCard(
                                title = "Kent Mango",
                                subtitle = "Std 10kg",
                                isSelected = selectedVariety == "Kent Mango",
                                modifier = Modifier.weight(1f)
                            ) { selectedVariety = "Kent Mango" }

                            CultivarGridCard(
                                title = "Hass Avocado",
                                subtitle = "Lugs 12kg",
                                isSelected = selectedVariety == "Hass Avocado",
                                modifier = Modifier.weight(1f)
                            ) { selectedVariety = "Hass Avocado" }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CultivarGridCard(
                                title = "Blueberries",
                                subtitle = "Punnet 3kg",
                                isSelected = selectedVariety == "Blueberries",
                                modifier = Modifier.weight(1f)
                            ) { selectedVariety = "Blueberries" }

                            CultivarGridCard(
                                title = "Red Globe",
                                subtitle = "Crate 8kg",
                                isSelected = selectedVariety == "Red Globe",
                                modifier = Modifier.weight(1f)
                            ) { selectedVariety = "Red Globe" }
                        }
                    }
                }
            }

            // C. Sección: Gross Intake Weight
            item {
                FruitLogixCard(
                    variant = CardVariant.Dark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_bus),
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GROSS INTAKE WEIGHT",
                                    style = FruitLogixTheme.typography.labelSmall,
                                    color = FruitLogixTheme.colors.primary
                                )
                            }
                            Surface(
                                color = FruitLogixTheme.colors.surfaceDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, FruitLogixTheme.colors.primary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Certified Scale 04",
                                    style = FruitLogixTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = FruitLogixTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$intakeWeight",
                                style = FruitLogixTheme.typography.displayLarge.copy(fontSize = 52.sp),
                                color = FruitLogixTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "kg",
                                style = FruitLogixTheme.typography.titleLarge,
                                color = FruitLogixTheme.colors.white,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeightAdjustmentButton(text = "-10", modifier = Modifier.weight(1f)) {
                                intakeWeight = (intakeWeight - 10).coerceAtLeast(0)
                            }
                            WeightAdjustmentButton(text = "-1", modifier = Modifier.weight(1f)) {
                                intakeWeight = (intakeWeight - 1).coerceAtLeast(0)
                            }
                            WeightAdjustmentButton(text = "+1", modifier = Modifier.weight(1f)) {
                                intakeWeight += 1
                            }
                            WeightAdjustmentButton(text = "+10", modifier = Modifier.weight(1f)) {
                                intakeWeight += 10
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("~15 crates × 10 kg target", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                            Text("Tare: ~18 kg", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary)
                        }
                    }
                }
            }

            // D. Harvest Shift & Farm Plot
            item {
                FruitLogixCard(
                    variant = CardVariant.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Harvest Shift & Timestamp",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FruitLogixTheme.colors.surfaceLight, RoundedCornerShape(8.dp))
                                .clickable { showShiftDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_id),
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(shiftText, style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                            }
                            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = "Editar", tint = FruitLogixTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Farm Plot & Sector",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FruitLogixTheme.colors.surfaceLight, RoundedCornerShape(8.dp))
                                .clickable { showPlotDialog = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_groups),
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(farmPlotText, style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // E. Product Batch Evidence (Estático y limpio tal como en los mockups)
            item {
                FruitLogixCard(
                    variant = CardVariant.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Product Batch Evidence",
                                style = FruitLogixTheme.typography.titleMedium,
                                color = FruitLogixTheme.colors.textOnLight
                            )
                            Surface(
                                color = FruitLogixTheme.colors.success.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.success,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GPS GEOTAGGED",
                                        style = FruitLogixTheme.typography.labelSmall,
                                        color = FruitLogixTheme.colors.success,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FruitLogixTheme.colors.surfaceLight, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(FruitLogixTheme.colors.surfaceTint, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_id),
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.textMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "IMG_20261031_0942.jpg",
                                        style = FruitLogixTheme.typography.bodyMedium,
                                        color = FruitLogixTheme.colors.textOnLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "420 KB • Captured 09:42 AM",
                                        style = FruitLogixTheme.typography.bodySmall,
                                        color = FruitLogixTheme.colors.textMuted
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { Toast.makeText(context, "Evidencia cargada correctamente", Toast.LENGTH_SHORT).show() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Retake")
                            }
                        }
                    }
                }
            }

            // F. Agronomist Observations
            item {
                FruitLogixCard(
                    variant = CardVariant.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Agronomist Observations", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight)
                            Text("Editable", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = observationsText,
                            onValueChange = { observationsText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = FruitLogixTheme.typography.bodySmall.copy(color = FruitLogixTheme.colors.textOnLight),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FruitLogixTheme.colors.primary,
                                unfocusedBorderColor = FruitLogixTheme.colors.surfaceTint
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Logged by Lead Inspector: Carlos V.",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Barra Inferior de Guardado (Guarda y actualiza el flujo de navegación con feedback real)
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    // Llamada directa al repositorio compartido para que se refleje de inmediato
                    HarvestRepository.addHarvest(
                        fruitName = selectedVariety,
                        weight = intakeWeight.toString(),
                        sector = farmPlotText
                    )
                    Toast.makeText(
                        context,
                        "Lote registrado de $selectedVariety ($intakeWeight kg) guardado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateBack() // Regresa a ProducerHarvestsScreen con la lista actualizada
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Harvest Batch",
                        style = FruitLogixTheme.typography.titleMedium,
                        color = FruitLogixTheme.colors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔒 Offline safe • Queues automatically to farm ledger",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        }
    }

    // Diálogos de edición rápida
    if (showShiftDialog) {
        AlertDialog(
            onDismissRequest = { showShiftDialog = false },
            title = { Text("Editar Turno y Timestamp") },
            text = {
                OutlinedTextField(
                    value = shiftText,
                    onValueChange = { shiftText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showShiftDialog = false }) { Text("Guardar") }
            }
        )
    }

    // Diálogos de edición rápida con bordes corregidos y ajustados
    if (showShiftDialog) {
        AlertDialog(
            onDismissRequest = { showShiftDialog = false },
            shape = RoundedCornerShape(16.dp), // Bordes elegantes y no exagerados
            title = { Text("Editar Turno y Timestamp", style = FruitLogixTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = shiftText,
                    onValueChange = { shiftText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), // Bordes internos limpios
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = { showShiftDialog = false }) {
                    Text("Guardar", color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPlotDialog) {
        AlertDialog(
            onDismissRequest = { showPlotDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Editar Fundo y Sector", style = FruitLogixTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = farmPlotText,
                    onValueChange = { farmPlotText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = { showPlotDialog = false }) {
                    Text("Guardar", color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun CultivarGridCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceTint
    val backgroundColor = if (isSelected) FruitLogixTheme.colors.primary.copy(alpha = 0.08f) else FruitLogixTheme.colors.surfaceLight

    Surface(
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
            Text(subtitle, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
        }
    }
}

@Composable
fun WeightAdjustmentButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FruitLogixTheme.colors.surfaceDark)
            .border(1.dp, FruitLogixTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.white, fontWeight = FontWeight.Bold)
    }
}