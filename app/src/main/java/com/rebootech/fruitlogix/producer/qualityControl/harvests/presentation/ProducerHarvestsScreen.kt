package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import androidx.compose.ui.res.painterResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.RemoteHarvestRepository

@Composable
fun ProducerHarvestsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegisterHarvest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDayTab by remember { mutableStateOf("31") }
    val harvestList by RemoteHarvestRepository.instance.recentHarvests.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(16.dp)
    ) {
        Text(
            text = "Control de Cosechas",
            style = FruitLogixTheme.typography.displayLarge,
            color = FruitLogixTheme.colors.textOnLight
        )
        Text(
            text = "PROGRAMACIÓN Y RENDIMIENTO EN CAMPO",
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Weekly Forecast Banner
            item {
                FruitLogixCard(variant = CardVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WEEKLY FORECAST • 11.45 WEEK", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
                            Surface(
                                color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Export Grade", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.white, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Kent Mango: 900 kg requested", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.white)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Committed to: Supermercados Wong • Cencosud Mkt", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { 640f / 900f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = FruitLogixTheme.colors.primary,
                            trackColor = FruitLogixTheme.colors.surfaceTint
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fulfilled so far:", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text("640 / 900 kg (71%)", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary)
                        }
                    }
                }
            }

            // 2. Harvest Schedule (Calendario)
            item {
                Column {
                    Text("Harvest Schedule", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    val scheduleDays = listOf(
                        Pair("27", "Mon"), Pair("28", "Tue"), Pair("29", "Wed"),
                        Pair("30", "Thu"), Pair("31", "Fri"), Pair("1", "Sat"), Pair("2", "Sun")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(scheduleDays) { (day, weekday) ->
                            val isSelected = selectedDayTab == day
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FruitLogixTheme.colors.primary else FruitLogixTheme.colors.surfaceLight)
                                    .clickable { selectedDayTab = day }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = weekday, style = FruitLogixTheme.typography.labelSmall, color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textMuted)
                                Text(text = day, style = FruitLogixTheme.typography.titleMedium, color = if (isSelected) FruitLogixTheme.colors.onPrimary else FruitLogixTheme.colors.textOnLight)
                            }
                        }
                    }
                }
            }

            // 3. Upcoming Harvests
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upcoming Harvests", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight)
                    Text("2 Queued", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted, fontWeight = FontWeight.Bold)
                }
            }

            item {
                UpcomingHarvestCard(
                    fruitName = "Blueberries (Biloxi)",
                    dateTime = "Nov 5 • Wed Morning",
                    estYield = "~80 kg",
                    locationLabel = "STAGING ZONE",
                    locationValue = "Block 4B Staging",
                    assignedCrewText = "Crew Beta (8 pickers)",
                    status = "Planned"
                )
            }

            // 4. Recent Harvests in Stock (Lista dinámica)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Harvests in Stock", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight)
                    Text("Live Ledger", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                }
            }

            items(harvestList, key = { it.id }) { harvest ->
                RecentHarvestCard(
                    fruitName = harvest.fruitName,
                    dateTime = harvest.dateTime,
                    weight = harvest.weight,
                    lotCode = harvest.lotCode,
                    footerLeftIconRes = R.drawable.ic_id,
                    footerLeftText = harvest.crewOrTransport,
                    footerRightText = harvest.sectorOrVault
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNavigateToRegisterHarvest,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "+ Register Harvest", style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.onPrimary)
        }
    }
}

@Composable
fun UpcomingHarvestCard(
    fruitName: String, dateTime: String, estYield: String,
    locationLabel: String, locationValue: String, assignedCrewText: String, status: String
) {
    FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = fruitName, style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight)
                    Text(text = dateTime, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                }
                Surface(color = FruitLogixTheme.colors.surfaceTint, shape = RoundedCornerShape(12.dp)) {
                    Text(text = status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(color = FruitLogixTheme.colors.surfaceLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("EST. YIELD", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text(estYield, style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(locationLabel, style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                            Text(locationValue, style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_groups), contentDescription = null, tint = FruitLogixTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Assigned: $assignedCrewText", style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentHarvestCard(
    fruitName: String, dateTime: String, weight: String, lotCode: String,
    footerLeftIconRes: Int, footerLeftText: String, footerRightIconRes: Int? = null, footerRightText: String
) {
    FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = fruitName, style = FruitLogixTheme.typography.bodyLarge, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                Surface(color = FruitLogixTheme.colors.success.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(text = "Registered", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.success, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateTime, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("LOGGED QUANTITY", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text(weight, style = FruitLogixTheme.typography.titleMedium, color = FruitLogixTheme.colors.primary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("BATCH LOT", style = FruitLogixTheme.typography.labelSmall, color = FruitLogixTheme.colors.textMuted)
                    Text(lotCode, style = FruitLogixTheme.typography.bodyMedium, color = FruitLogixTheme.colors.textOnLight, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = footerLeftIconRes), contentDescription = null, tint = FruitLogixTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(footerLeftText, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (footerRightIconRes != null) {
                        Icon(painter = painterResource(id = footerRightIconRes), contentDescription = null, tint = FruitLogixTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(footerRightText, style = FruitLogixTheme.typography.bodySmall, color = FruitLogixTheme.colors.textMuted)
                }
            }
        }
    }
}