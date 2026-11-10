package com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.InspectionOrderRepository
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerInspectionScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onReviewReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Carga de datos dinámicos según el orderId (1044 Kent Mango o 1048 Hass Avocado)
    val orderData = remember(orderId) { InspectionOrderRepository.getOrderById(orderId) }

    var selectedStar by remember { mutableStateOf(if (orderData.isRejected) 3 else 4) }

    var selectedCal12 by remember { mutableStateOf(false) }
    var selectedCal14 by remember { mutableStateOf(false) }
    var selectedCal16 by remember { mutableStateOf(true) }
    var selectedCal18 by remember { mutableStateOf(true) }
    var selectedCal20 by remember { mutableStateOf(false) }

    var isOfflineMode by remember { mutableStateOf(false) }
    var coreTemp by remember { mutableStateOf(orderData.initialTemp) }
    var inspectionNotes by remember {
        mutableStateOf(
            if (orderData.isRejected)
                "Re-inspection requested due to initial defect flag. Verified lot sampling across pallet layer 2."
            else
                "Slight superficial rubbing on 2 boxes in layer 3. Flesh firmness tested 92 Sh, clean skin uniformity."
        )
    }

    val hasTemperatureError = coreTemp.toFloatOrNull()?.let { it > 8.0f || it < 5.0f } ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quality Inspection",
                        style = FruitLogixTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FruitLogixTheme.colors.textOnLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back", tint = FruitLogixTheme.colors.textOnLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FruitLogixTheme.colors.bg)
            )
        },
        containerColor = FruitLogixTheme.colors.bg,
        bottomBar = {
            Surface(
                color = FruitLogixTheme.colors.bg,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (hasTemperatureError) {
                        Surface(
                            color = FruitLogixTheme.colors.dangerStrong,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painter = painterResource(id = R.drawable.ic_alert), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resolve 1 temperature error before submitting", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isOfflineMode = !isOfflineMode },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FruitLogixTheme.colors.textOnLight)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOfflineMode) "Saved locally" else "Save offline", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { if (!hasTemperatureError) onReviewReport() },
                            enabled = !hasTemperatureError,
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FruitLogixTheme.colors.primary,
                                disabledContainerColor = FruitLogixTheme.colors.surfaceTint
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Review report →", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Alerta si es una Re-Inspección por Rechazo previo (Caso Kent Mango #1044)
            if (orderData.isRejected) {
                Surface(
                    color = FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_alert), contentDescription = null, tint = FruitLogixTheme.colors.dangerStrong, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("RE-INSPECTION REQUIRED", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.dangerStrong, fontWeight = FontWeight.Bold)
                            Text(orderData.rejectionReason ?: "Quality rejected", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }
            }

            // Barra de Progreso de Pasos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(stepNumber = "1", title = "Prepare", isCompleted = true)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = FruitLogixTheme.colors.primary)
                StepIndicator(stepNumber = "2", title = "Inspect", isActive = true)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = FruitLogixTheme.colors.surfaceTint)
                StepIndicator(stepNumber = "3", title = "Report")
            }

            // Información Dinámica de Lote y Pedido (Kent Mango o Hass Avocado)
            FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = FruitLogixTheme.colors.primary, shape = RoundedCornerShape(4.dp)) {
                            Text(orderData.lotCode, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Text("${orderData.clientName} · Order #${orderData.orderId}", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(orderData.productName, style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(orderData.cratesInfo, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(orderData.netWeight, style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.primary)
                        }
                    }
                }
            }

            // Calidad Visual y Calibre
            FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Visual Quality & Sizing", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.3f), shape = RoundedCornerShape(6.dp)) {
                            Text("Spec: Export", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textOnLight)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Visual Quality Score", style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textMuted)
                        Text("$selectedStar / 5", style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        (1..5).forEach { star ->
                            Surface(
                                color = if (star <= selectedStar) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedStar = star }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$star", fontWeight = FontWeight.Bold, color = if (star <= selectedStar) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (selectedStar >= 4) "Grade A - Optimal export condition" else "Grade B - Standard condition, re-evaluating", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.primary)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Verified Caliber Spread", style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textMuted)
                        Text("Target confirmed", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CaliberChip("Caliber 12 (300g)", selectedCal12) { selectedCal12 = !selectedCal12 }
                            CaliberChip("Caliber 14 (250g)", selectedCal14) { selectedCal14 = !selectedCal14 }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CaliberChip("Caliber 16 (220g)", selectedCal16) { selectedCal16 = !selectedCal16 }
                            CaliberChip("Caliber 18 (200g)", selectedCal18) { selectedCal18 = !selectedCal18 }
                        }
                        CaliberChip("Caliber 20 (175g)", selectedCal20) { selectedCal20 = !selectedCal20 }
                    }
                }
            }

            // Mediciones Técnicas
            FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Technical Measurements", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        if (hasTemperatureError) {
                            Surface(color = FruitLogixTheme.colors.dangerStrong, shape = RoundedCornerShape(6.dp)) {
                                Text("1 Issue Flagged", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    MeasurementRow(
                        title = "Pulp Core Temp (°C)",
                        allowed = "Allowed 5.0 – 8.0 °C",
                        value = coreTemp,
                        isError = hasTemperatureError,
                        errorMsg = "Value out of range — valid range 5.0-8.0 °C. Overheating risk.",
                        onValueChange = { coreTemp = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementRowStatic("Pulp Moisture", "Allowed 70 – 82 %", orderData.initialMoisture, "Optimal")
                    Spacer(modifier = Modifier.height(10.dp))
                    MeasurementRowStatic("Defects & Shrink", "Tolerance threshold ≤ 4%", orderData.initialDefects, if (orderData.isRejected) "Exceeded" else "Within limit")
                }
            }

            // Notas de Inspección
            FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Inspection Notes", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${inspectionNotes.length} / 300", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inspectionNotes,
                        onValueChange = { inspectionNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f),
                            unfocusedContainerColor = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = FruitLogixTheme.colors.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun StepIndicator(stepNumber: String, title: String, isCompleted: Boolean = false, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted || isActive) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(16.dp))
            } else {
                Text(stepNumber, fontWeight = FontWeight.Bold, color = if (isActive) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, style = FruitLogixTheme.typography.bodySmall, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.textMuted)
    }
}

@Composable
fun CaliberChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight)
        }
    }
}

@Composable
fun MeasurementRow(
    title: String,
    allowed: String,
    value: String,
    isError: Boolean,
    errorMsg: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        color = if (isError) FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.2f) else FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text(allowed, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                }
                Surface(
                    color = if (isError) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        Text("°C", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg, fontSize = 11.sp, color = FruitLogixTheme.colors.dangerStrong, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MeasurementRowStatic(titleStr: String, allowed: String, value: String, status: String) {
    Surface(
        color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(titleStr, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(allowed, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(status, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
            }
        }
    }
}