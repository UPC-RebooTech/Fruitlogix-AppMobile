package com.rebootech.fruitlogix.shared.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import java.text.NumberFormat

/** Amount in soles with the separators of the selected language: "S/ 8,900.50" / "S/ 8.900,50". */
@Composable
fun formatPen(amount: Double, decimals: Int = 2): String {
    val format = NumberFormat.getNumberInstance(appLocale()).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return stringResource(R.string.common_amount_pen, format.format(amount))
}
