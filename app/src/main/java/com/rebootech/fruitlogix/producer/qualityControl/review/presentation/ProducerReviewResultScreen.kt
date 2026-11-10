package com.rebootech.fruitlogix.producer.qualityControl.review.presentation

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
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.InspectionOrderRepository
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerReviewResultScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onEditInspection: () -> Unit,
    onBackToOrder: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orderData = remember(orderId) { InspectionOrderRepository.getOrderById(orderId) }
    var isReportSent by remember { mutableStateOf(false) }
    var hasPhotosAttached by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quality Report #${orderData.orderId}",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isReportSent) {
                        OutlinedButton(
                            onClick = onEditInspection,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FruitLogixTheme.colors.textOnLight)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isReportSent = true },
                            modifier = Modifier.weight(1.5f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send report →", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onOpenChat,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FruitLogixTheme.colors.textOnLight)
                        ) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open chat", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onBackToOrder,
                            modifier = Modifier.weight(1.5f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to order", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
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
            if (isReportSent) {
                // ESTADO 2: REPORTE ENVIADO (Página de Confirmación)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(FruitLogixTheme.colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Report sent!", style = FruitLogixTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Waiting for distributor review", style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textMuted)

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFE88A1A).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_refresh), contentDescription = null, tint = Color(0xFFE88A1A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pending Quality Review", style = FruitLogixTheme.typography.labelSmall, color = Color(0xFFE88A1A), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tarjeta Resumen de Envíos y Asignación
                FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                                Text(orderData.lotCode, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text("BRIX 8.2%", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Order #${orderData.orderId}", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(orderData.clientName, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Commodity", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                                    Text("${orderData.productName} · ${orderData.netWeight}", style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Transmitted", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                                    Text("Today, 10:48 AM", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Asignado a Carlos Mendoza
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                Text("CM", color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Carlos Mendoza", style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Logistics Coord. · Mercado Central S.A.", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                            }
                            Surface(color = FruitLogixTheme.colors.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text("✓ Assigned", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Stamped Evidentiary Records
                Text("STAMPED EVIDENTIARY RECORDS", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = FruitLogixTheme.colors.surfaceDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3 Photos Stamped", style = FruitLogixTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(color = FruitLogixTheme.colors.surfaceDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_mic), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Note (0:12)", style = FruitLogixTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Next steps (Automated Queue)
                FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Next steps", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                                Text("Automated Queue", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 1: Delivered
                        Row(verticalAlignment = Alignment.Top) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary), contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Delivered to Carlos Mendoza", style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("10:48 AM", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                                }
                                Text("Payload encrypted and pushed via CloudSync node #4.", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Step 2: Distributor Sign-off
                        Row(verticalAlignment = Alignment.Top) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFE88A1A)), contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(id = R.drawable.ic_refresh), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Distributor Commercial Sign-off", style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("In Progress", style = FruitLogixTheme.typography.labelSmall, color = Color(0xFFE88A1A), fontWeight = FontWeight.Bold)
                                }
                                Text("Estimated response within 45 min based on average depot throughput.", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                            }
                        }
                    }
                }

            } else {
                // ESTADO 1: REVISIÓN PREVIA AL ENVÍO (Step 3: Send Report)

                // Indicador de Pasos 1, 2, 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(stepNumber = "1", title = "Prepare", isCompleted = true)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = FruitLogixTheme.colors.primary)
                    StepIndicator(stepNumber = "2", title = "Inspect", isCompleted = true)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = FruitLogixTheme.colors.primary)
                    StepIndicator(stepNumber = "3", title = "Send Report", isActive = true)
                }

                // Advertencia de Fotos No Adjuntadas (si aplica)
                if (!hasPhotosAttached) {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.ic_alert), contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No photos attached", fontWeight = FontWeight.Bold, color = Color(0xFF856404))
                                }
                                Surface(color = Color(0xFFFFEBAA), shape = RoundedCornerShape(6.dp)) {
                                    Text("Action Recommended", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF856404), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No photos attached — you can still send, but approval may be slower. Distributors often require visual verification for priority export clearance.", fontSize = 12.sp, color = Color(0xFF856404))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { hasPhotosAttached = true },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add photos", fontSize = 11.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { isReportSent = true },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBAA)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Send anyway →", fontSize = 11.sp, color = Color(0xFF856404), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Destinatario del Reporte
                Surface(color = FruitLogixTheme.colors.surfaceDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Target Recipient · ${orderData.clientName}", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text("Will be dispatched to Carlos Mendoza (distributor QA lead) for formal clearance.", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }

                // Tarjeta Principal de Inspección (Datos Métricos y Calibre)
                FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("HARVEST INSPECTION REPORT", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                            Icon(painter = painterResource(id = R.drawable.ic_shield_check), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${orderData.productName} · ${orderData.netWeight}", style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(orderData.cratesInfo, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)

                        Spacer(modifier = Modifier.height(12.dp))

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(orderData.visualScore) {
                                        Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${orderData.visualScore}.0 / 5.0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Surface(color = FruitLogixTheme.colors.primary, shape = RoundedCornerShape(8.dp)) {
                                    Text(orderData.gradeText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), fontSize = 11.sp, color = FruitLogixTheme.colors.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricReviewBox("Core Temp", "${orderData.initialTemp} °C", if(orderData.isTempError) "Exceeded" else "In range", modifier = Modifier.weight(1f), isAlert = orderData.isTempError)
                            MetricReviewBox("Rel. Humidity", orderData.initialMoisture, "Optimal", modifier = Modifier.weight(1f))
                            MetricReviewBox("Defects", orderData.initialDefects, if(orderData.isRejected) "Exceeded limit" else "≤ 4% Limit", modifier = Modifier.weight(1f), isAlert = orderData.isRejected)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reproductor de Nota de Voz
                        Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f), shape = RoundedCornerShape(10.dp)) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(FruitLogixTheme.colors.primary), contentAlignment = Alignment.Center) {
                                    Icon(painter = painterResource(id = R.drawable.ic_play), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Voice Note: 0:12", style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Recorded by Lead Inspector", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                                }
                                Icon(painter = painterResource(id = R.drawable.ic_mic), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Certificación SENASA / HACCP Hash
                        Surface(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painter = painterResource(id = R.drawable.ic_shield_check), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SENASA / HACCP certified audit hash: #FLX-88219-OK · Jorge Ramos (Lead Inspector)", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            }
                        }
                    }
                }

                // AUTOMATED ACTIONS ON SEND
                FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AUTOMATED ACTIONS ON SEND", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatches encrypted PDF & WhatsApp brief to buyer", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Releases lot #${orderData.lotCode} for cold storage transit bay 3", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncs cloud ledger timestamp & inspector biometric sign", style = FruitLogixTheme.typography.bodySmall, color = Color.White)
                        }
                    }
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
fun MetricReviewBox(label: String, value: String, status: String, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    Surface(
        color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            Text(status, fontSize = 10.sp, color = if (isAlert) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}