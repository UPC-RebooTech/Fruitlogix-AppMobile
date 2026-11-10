package com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.harvests.data.FakeHarvestRepository
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerRegisterHarvestScreen(
    onNavigateBack: () -> Unit = {}
) {
    val harvestRepository = FakeHarvestRepository.instance
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fruitType by remember { mutableStateOf("Mango Kent") }
    var lotCode by remember { mutableStateOf("") }
    var weightKgInput by remember { mutableStateOf("") }
    var estimatedBrixInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Estados de validación
    var lotCodeError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var brixError by remember { mutableStateOf<String?>(null) }

    val darkTextColor = Color(0xFF1B2A1E)
    val limeAccentColor = Color(0xFFC8E060)

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = darkTextColor,
        unfocusedTextColor = darkTextColor,
        focusedBorderColor = darkTextColor,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = darkTextColor,
        unfocusedLabelColor = Color(0xFF4A5A4A),
        cursorColor = darkTextColor
    )

    fun validate(): Boolean {
        var isValid = true

        if (lotCode.trim().isBlank()) {
            lotCodeError = "El código de lote es obligatorio"
            isValid = false
        } else if (lotCode.trim().length < 3) {
            lotCodeError = "Ingrese un lote válido (mínimo 3 caracteres)"
            isValid = false
        } else {
            lotCodeError = null
        }

        val weight = weightKgInput.toDoubleOrNull()
        if (weight == null || weight <= 0.0) {
            weightError = "Ingrese un peso mayor a 0 kg"
            isValid = false
        } else if (weight > 50000.0) {
            weightError = "El peso excede la capacidad máxima de galpón (50,000 kg)"
            isValid = false
        } else {
            weightError = null
        }

        val brix = estimatedBrixInput.toDoubleOrNull()
        if (estimatedBrixInput.isNotBlank()) {
            if (brix == null || brix < 0.0 || brix > 35.0) {
                brixError = "Grados Brix debe estar entre 0.0 y 35.0 °Bx"
                isValid = false
            } else {
                brixError = null
            }
        } else {
            brixError = null
        }

        return isValid
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar Cosecha en Fundo",
                        fontWeight = FontWeight.Bold,
                        color = darkTextColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = darkTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Ingrese los datos de la fruta cosechada para asignar trazabilidad y habilitarla en el stock de despacho.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Campo 1: Fruta / Variedad
            OutlinedTextField(
                value = fruitType,
                onValueChange = { fruitType = it },
                label = { Text("Variedad o Producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors
            )

            // Campo 2: Código de Lote
            OutlinedTextField(
                value = lotCode,
                onValueChange = {
                    lotCode = it.uppercase()
                    if (lotCodeError != null) validate()
                },
                label = { Text("Código de Lote (ej. L-MNG-2026)") },
                singleLine = true,
                isError = lotCodeError != null,
                supportingText = {
                    lotCodeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors
            )

            // Campo 3: Kilos Cosechados
            OutlinedTextField(
                value = weightKgInput,
                onValueChange = {
                    weightKgInput = it.filter { char -> char.isDigit() || char == '.' }
                    if (weightError != null) validate()
                },
                label = { Text("Kilos Totales (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = weightError != null,
                supportingText = {
                    weightError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors
            )

            // Campo 4: Calidad estimada (°Brix opcional)
            OutlinedTextField(
                value = estimatedBrixInput,
                onValueChange = {
                    estimatedBrixInput = it.filter { char -> char.isDigit() || char == '.' }
                    if (brixError != null) validate()
                },
                label = { Text("Grados Brix Estimados (°Bx) - Opcional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = brixError != null,
                supportingText = {
                    brixError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors
            )

            // Campo 5: Observaciones de campo
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas de recolección (Opcional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (validate()) {
                        scope.launch {
                            val formattedTime = LocalTime.now().format(
                                DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                            )
                            harvestRepository.addHarvest(
                                fruitName = fruitType.trim(),
                                lotCode = lotCode.trim(),
                                weightKg = weightKgInput.trim(),
                                dateTime = "Hoy, $formattedTime",
                                sector = "Galpón 1"
                            )
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = limeAccentColor)
            ) {
                Text(
                    text = "Registrar y Guardar Lote",
                    color = darkTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}