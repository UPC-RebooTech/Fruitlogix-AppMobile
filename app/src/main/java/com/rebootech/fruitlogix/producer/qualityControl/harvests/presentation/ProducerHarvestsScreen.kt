package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerHarvestsScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var weightKg by remember { mutableIntStateOf(150) }
    var selectedVariety by remember { mutableStateOf("Kent Mango") }
    var observations by remember {
        mutableStateOf("Firmness measured at 14.2 lbs/cm². Sugar brix baseline: 12.8%. Field pick crew #4 completed harvesting before morning heat peak.")
    }
    var showSuccessBanner by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "STOCK INTAKE",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Register Harvest",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = FruitLogixTheme.typography.titleLarge,
                            color = FruitLogixTheme.colors.textOnLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = FruitLogixTheme.colors.textOnLight
                        )
                    }
                },
                actions = {
                    Surface(
                        color = FruitLogixTheme.colors.surfaceTint,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "LOT #H-2024-884",
                            style = FruitLogixTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FruitLogixTheme.colors.textOnLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FruitLogixTheme.colors.bg)
            )
        },
        containerColor = FruitLogixTheme.colors.bg
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de éxito si la cosecha fue actualizada
            if (showSuccessBanner) {
                Surface(
                    color = FruitLogixTheme.colors.success.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = FruitLogixTheme.colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Harvest registered. Stock updated.",
                                style = FruitLogixTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FruitLogixTheme.colors.textOnLight
                            )
                        }
                        IconButton(
                            onClick = { showSuccessBanner = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = FruitLogixTheme.colors.textOnLight
                            )
                        }
                    }
                }
            }

            // Subtítulo Protocolo Diario
            Column {
                Text(
                    text = "DAILY INTAKE PROTOCOL",
                    style = FruitLogixTheme.typography.labelSmall,
                    color = FruitLogixTheme.colors.textMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Log daily orchard harvest directly into cold storage allocations.",
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.textMuted
                )
            }

            // Variedades Activas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Variety (Active Cultivar)",
                    style = FruitLogixTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnLight
                )
                Surface(
                    color = FruitLogixTheme.colors.surfaceTint,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "4 Cultivars",
                        style = FruitLogixTheme.typography.labelSmall,
                        color = FruitLogixTheme.colors.textOnLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Grid 2x2 para Variedades
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VarietyCard(
                        title = "Kent Mango",
                        subtitle = "Std 10kg",
                        isSelected = selectedVariety == "Kent Mango",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedVariety = "Kent Mango" }
                    )
                    VarietyCard(
                        title = "Hass Avocado",
                        subtitle = "Lugs 12kg",
                        isSelected = selectedVariety == "Hass Avocado",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedVariety = "Hass Avocado" }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VarietyCard(
                        title = "Blueberries",
                        subtitle = "Punnet 3kg",
                        isSelected = selectedVariety == "Blueberries",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedVariety = "Blueberries" }
                    )
                    VarietyCard(
                        title = "Red Globe",
                        subtitle = "Crate 8kg",
                        isSelected = selectedVariety == "Red Globe",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedVariety = "Red Globe" }
                    )
                }
            }

            // Card de Peso Bruto (GROSS INTAKE WEIGHT)
            FruitLogixCard(
                variant = CardVariant.Dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GROSS INTAKE WEIGHT",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Certified Scale 04",
                                style = FruitLogixTheme.typography.labelSmall,
                                color = FruitLogixTheme.colors.textMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$weightKg",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = FruitLogixTheme.typography.displayLarge,
                            color = FruitLogixTheme.colors.primary,
                            fontSize = 54.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kg",
                            style = FruitLogixTheme.typography.titleLarge,
                            color = FruitLogixTheme.colors.white,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeightStepButton("-10", modifier = Modifier.weight(1f)) {
                            weightKg = (weightKg - 10).coerceAtLeast(0)
                        }
                        WeightStepButton("-1", modifier = Modifier.weight(1f)) {
                            weightKg = (weightKg - 1).coerceAtLeast(0)
                        }
                        WeightStepButton("+1", modifier = Modifier.weight(1f)) {
                            weightKg += 1
                        }
                        WeightStepButton("+10", modifier = Modifier.weight(1f)) {
                            weightKg += 10
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "~15 crates × 10 kg target",
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                        Text(
                            text = "Tare: ~18 kg",
                            style = FruitLogixTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FruitLogixTheme.colors.primary
                        )
                    }
                }
            }

            // Observaciones Agronómicas
            FruitLogixCard(
                variant = CardVariant.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Agronomist Observations",
                            style = FruitLogixTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FruitLogixTheme.colors.textOnLight
                        )
                        Text(
                            text = "Optional",
                            style = FruitLogixTheme.typography.labelSmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observations,
                        onValueChange = { observations = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = FruitLogixTheme.typography.bodyMedium,
                        maxLines = 4
                    )
                }
            }

            // Botón Guardar Cosecha
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Harvest Batch",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FruitLogixTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
fun VarietyCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) FruitLogixTheme.colors.surfaceDark else FruitLogixTheme.colors.surfaceTint
    val backgroundColor = if (isSelected) FruitLogixTheme.colors.white else FruitLogixTheme.colors.surfaceLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textOnLight
                )
                if (isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.surfaceDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textMuted
            )
        }
    }
}

@Composable
fun WeightStepButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FruitLogixTheme.colors.surfaceDark)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = FruitLogixTheme.colors.white,
            style = FruitLogixTheme.typography.titleMedium
        )
    }
}