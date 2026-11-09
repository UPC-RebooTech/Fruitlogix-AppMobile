package com.rebootech.fruitlogix.producer.qualityControl.reports.presentation

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.reports.data.QualityReportRepository
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerReportDetailScreen(
    reportId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }
    var includeHighRes by remember { mutableStateOf(true) }

    // Buscamos el reporte en el repositorio global
    val report = QualityReportRepository.reports.find { it.id == reportId } ?: QualityReportRepository.reports.first()

    val statusBg = when (report.status) {
        QualityStatus.APPROVED -> FruitLogixTheme.colors.success
        QualityStatus.PENDING -> FruitLogixTheme.colors.warning
        QualityStatus.REJECTED -> FruitLogixTheme.colors.dangerStrong
    }

    val statusText = when (report.status) {
        QualityStatus.APPROVED -> "Approved"
        QualityStatus.PENDING -> "Pending Quality"
        QualityStatus.REJECTED -> "Rejected"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report ${report.orderNumber}", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back", tint = FruitLogixTheme.colors.textOnLight)
                    }
                },
                actions = {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = "Share", tint = FruitLogixTheme.colors.textOnLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FruitLogixTheme.colors.bg)
            )
        },
        containerColor = FruitLogixTheme.colors.bg,
        bottomBar = {
            // Botones inferiores fijos de acción (Share & Download PDF)
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
                    OutlinedButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FruitLogixTheme.colors.textOnLight)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Certificado PDF exportado con éxito", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.onPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Cabecera de la Orden
                FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("PURCHASE ORDER", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Surface(color = statusBg.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = statusBg, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${report.orderNumber} • ${report.clientName}", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("🫐 ${report.productName} (Premium Export)", style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = FruitLogixTheme.colors.textOnLight)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${report.weightKg} kg (${report.cratesCount} crates × 10 kg)", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                            Text(report.lotCode, style = FruitLogixTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📍 ${report.locationInfo}", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_shield_check), contentDescription = null, tint = FruitLogixTheme.colors.success, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspected: ${report.timestamp} • Lead Inspector ${report.inspectorName}", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                        }
                    }
                }

                // 2. Telemetría de Control de Calidad
                Text("Quality Control Telemetry", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryCard("Visual Quality", "${report.visualScore} / 5.0", "Within range ✓", "Optimal bloom, uniform", modifier = Modifier.weight(1f))
                        TelemetryCard("Defects (Shrink)", "${report.defectsPercentage}%", "Within range ✓", "Zero soft berries", modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryCard("Core Pulp Temp", "${report.coreTemp} °C", "Within range ✓", "Target calibrated", modifier = Modifier.weight(1f))
                        TelemetryCard("Storage RH", "${report.humidityPercentage}%", "Within range ✓", "Vapor lock stable", modifier = Modifier.weight(1f))
                    }
                }

                // 3. Notas del Agrónomo de Campo
                FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(id = R.drawable.ic_info), contentDescription = null, tint = FruitLogixTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Field Agronomist Notes", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"Fruit firm to touch with 100% natural waxy bloom intact. Uniform caliber distribution (14-16mm). Zero soft berries or fungal decay found across 3 randomized sampling trays.\"\n\nSample Set: 300 berries (Randomized) • Compliance: 100%",
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                }

                // 4. Trayectoria Logística (Order Logistics Trajectory)
                Text("Order Logistics Trajectory", style = FruitLogixTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)

                FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TrajectoryStep("Harvested", "Completed • Oct 30, 2026 • Field Block 4B", isCompleted = true)
                        TrajectoryStep("Inspected", "Completed • ${report.timestamp} • ${report.locationInfo}", isCompleted = true)
                        TrajectoryStep("Loaded (In Transit)", "Active • Handled by Driver Roberto Silva (Mobile #R-402)", isCompleted = false, isActive = true)
                        TrajectoryStep("Received", "Pending • Est. Delivery Nov 01, 2026", isCompleted = false)
                        TrajectoryStep("Invoiced", "Pending • Automated po-is trigger upon acceptance", isCompleted = false)
                    }
                }
            }

            // Bottom Sheet de Compartir (Share Sheet)
            if (showShareSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showShareSheet = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(FruitLogixTheme.colors.bg),
                        color = FruitLogixTheme.colors.bg
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Share Quality Report", style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
                                IconButton(onClick = { showShareSheet = false }) {
                                    Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close", tint = FruitLogixTheme.colors.textOnLight)
                                }
                            }

                            Text("RECENT FIELD CONTACTS", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                ContactAvatar("Carlos M.", "QA Lead")
                                ContactAvatar("Roberto S.", "Driver")
                                ContactAvatar("Elena Q.", "Packhouse")
                            }

                            HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint)

                            Text("SHARE CHANNELS", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                ChannelButton("In-App Chat")
                                ChannelButton("WhatsApp")
                                ChannelButton("Email PDF")
                                ChannelButton("Save Files")
                            }

                            // Switch de High-Res Imagery
                            Surface(
                                color = FruitLogixTheme.colors.surfaceLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Include High-Res Imagery", fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight, fontSize = 13.sp)
                                        Text("Attaches 4 calibrated orchard audit stills", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                                    }
                                    Switch(checked = includeHighRes, onCheckedChange = { includeHighRes = it })
                                }
                            }

                            Button(
                                onClick = { showShareSheet = false },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.surfaceTint),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(title: String, value: String, status: String, subtitle: String, modifier: Modifier = Modifier) {
    FruitLogixCard(variant = CardVariant.Dark, modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = FruitLogixTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.white)
            Spacer(modifier = Modifier.height(2.dp))
            Text(status, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
            Text(subtitle, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
        }
    }
}

@Composable
fun TrajectoryStep(title: String, subtitle: String, isCompleted: Boolean, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted || isActive) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceTint),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = FruitLogixTheme.colors.onPrimary, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = FruitLogixTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isActive) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.textOnLight)
            Text(subtitle, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
        }
    }
}

@Composable
fun ContactAvatar(name: String, role: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FruitLogixTheme.colors.surfaceTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = FruitLogixTheme.colors.textOnLight, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
        Text(role, fontSize = 9.sp, color = FruitLogixTheme.colors.textMuted)
    }
}

@Composable
fun ChannelButton(label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FruitLogixTheme.colors.surfaceLight)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FruitLogixTheme.colors.textOnLight)
    }
}