package com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.inspection.data.FakeInspectionRepository
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private enum class ScanState { SCANNING, MANUAL, MATCH, ERROR }

/**
 * Scan a lot QR/barcode (camera simulated until the scanner library is added).
 * The viewfinder is the only dark area; the result panel is a light card.
 * Codes are resolved against open lots, so a typed code is really validated.
 */
@Composable
fun ProducerScanLotScreen(
    orderId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToInspection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = FakeInspectionRepository.instance
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    val expectedLot = orderId?.takeIf { it.isNotBlank() }?.let { repository.lotForOrder(it) }
    var state by rememberSaveable { mutableStateOf(ScanState.SCANNING) }
    var matchedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var manualCode by rememberSaveable { mutableStateOf("") }
    val matchedLot = matchedOrderId?.let { repository.lotForOrder(it) }

    fun resolve(code: String) {
        val lot = repository.findByLotCode(code)
        if (lot != null && (expectedLot == null || lot.orderId == expectedLot.orderId)) {
            matchedOrderId = lot.orderId
            state = ScanState.MATCH
        } else {
            state = ScanState.ERROR
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        BackHeader(
            title = stringResource(R.string.scan_title),
            subtitle = expectedLot?.let { stringResource(R.string.scan_order_context, it.orderId, it.clientName) }
                ?: stringResource(R.string.scan_order_any),
            onBack = onNavigateBack
        )

        Viewfinder(state)

        FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                when (state) {
                    ScanState.SCANNING -> {
                        Text(
                            text = stringResource(R.string.scan_subtitle),
                            style = FruitLogixTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                        PrimaryButton(
                            text = stringResource(R.string.scan_manual),
                            onClick = { state = ScanState.MANUAL },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                AppIcon(id = R.drawable.ic_edit, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                            }
                        )
                        TextActionButton(
                            text = stringResource(R.string.scan_simulate),
                            onClick = { resolve(expectedLot?.lotCode ?: "HA-09") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ScanState.MANUAL -> {
                        Text(
                            text = stringResource(R.string.scan_manual_title),
                            style = FruitLogixTheme.typography.titleMedium,
                            color = scheme.onSurface
                        )
                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it.uppercase() },
                            label = { Text(stringResource(R.string.scan_manual_label)) },
                            placeholder = { Text(stringResource(R.string.scan_manual_hint)) },
                            singleLine = true,
                            textStyle = FruitLogixTheme.extraTypography.dataMono,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            shape = FruitLogixTheme.shapes.Input,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = scheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        )
                        PrimaryButton(
                            text = stringResource(R.string.scan_validate),
                            onClick = { resolve(manualCode) },
                            enabled = manualCode.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextActionButton(
                            text = stringResource(R.string.scan_back_to_camera),
                            onClick = { state = ScanState.SCANNING },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ScanState.MATCH -> matchedLot?.let { lot ->
                        ResultBanner(
                            iconRes = R.drawable.ic_check,
                            title = stringResource(R.string.scan_match_title),
                            message = stringResource(R.string.scan_match_message),
                            ok = true
                        )
                        LotSummary(lot)
                        PrimaryButton(
                            text = stringResource(R.string.scan_continue),
                            onClick = { onNavigateToInspection(lot.orderId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextActionButton(
                            text = stringResource(R.string.scan_again),
                            onClick = {
                                matchedOrderId = null
                                state = ScanState.SCANNING
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ScanState.ERROR -> {
                        ResultBanner(
                            iconRes = R.drawable.ic_alert,
                            title = stringResource(R.string.scan_error_title),
                            message = stringResource(R.string.scan_error_message),
                            ok = false
                        )
                        PrimaryButton(
                            text = stringResource(R.string.scan_try_again),
                            onClick = { state = ScanState.SCANNING },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SecondaryButton(
                            text = stringResource(R.string.scan_manual),
                            onClick = { state = ScanState.MANUAL },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/** Simulated camera viewfinder (dark) with a Lima frame, red on error. */
@Composable
private fun Viewfinder(state: ScanState) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val frameColor = when (state) {
        ScanState.ERROR -> colors.dangerStrong
        else -> scheme.inversePrimary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(FruitLogixTheme.shapes.HeroCard)
            .background(scheme.inverseSurface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .border(3.dp, frameColor, FruitLogixTheme.shapes.Card),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                ScanState.MATCH -> Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(scheme.inversePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                }
                ScanState.ERROR -> AppIcon(id = R.drawable.ic_alert, contentDescription = null, tint = colors.dangerStrong, modifier = Modifier.size(48.dp))
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIcon(id = R.drawable.ic_camera, contentDescription = null, tint = colors.onInverseSurfaceVariant, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                    Text(
                        text = stringResource(R.string.scan_looking),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = scheme.inverseOnSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultBanner(iconRes: Int, title: String, message: String, ok: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val bg = if (ok) colors.successContainer else scheme.errorContainer
    val fg = if (ok) colors.onSuccessContainer else scheme.onErrorContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(bg)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.Top
    ) {
        AppIcon(id = iconRes, contentDescription = null, tint = fg, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Column {
            Text(text = title, style = FruitLogixTheme.typography.titleSmall, color = fg)
            Text(text = message, style = FruitLogixTheme.typography.bodyMedium, color = fg)
        }
    }
}

@Composable
private fun LotSummary(lot: InspectionLot) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.surfaceContainerLow)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(
            text = stringResource(R.string.scan_order_context, lot.orderId, lot.clientName),
            style = FruitLogixTheme.typography.titleSmall,
            color = scheme.onSurface
        )
        Text(
            text = stringResource(R.string.scan_lot_summary, lot.productName, lot.netWeightKg, lot.crates),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.inspection_lot, lot.lotCode),
            style = FruitLogixTheme.extraTypography.dataMonoSmall,
            color = scheme.primary
        )
    }
}
