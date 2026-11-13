package com.rebootech.fruitlogix.shared.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import java.text.NumberFormat

/**
 * Quantities are shown in kg, the unit the server uses: "850 kg", or "11 200 kg · 11.2 t"
 * from one tonne up (the tonne helps when thinking in trucks).
 */
@Composable
fun formatKg(kg: Double): String {
    val number = NumberFormat.getNumberInstance(appLocale()).apply { maximumFractionDigits = 1 }.format(kg)
    return if (kg >= 1000) stringResource(R.string.common_kg_with_tonnes, number, formatDecimal(kg / 1000))
    else stringResource(R.string.common_kg, number)
}
