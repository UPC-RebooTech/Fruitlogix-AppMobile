package com.rebootech.fruitlogix.producer.qualityControl.reports.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import java.time.LocalDateTime
import java.util.Locale

// Thin wrappers over the shared formatters (shared/ui/format), kept for the reports screens.

@Composable
@ReadOnlyComposable
internal fun reportLocale(): Locale = appLocale()

@Composable
internal fun formatInspectedAt(dateTime: LocalDateTime): String = formatRelativeDateTime(dateTime)

@Composable
@ReadOnlyComposable
internal fun formatDecimal(value: Double): String =
    com.rebootech.fruitlogix.shared.ui.format.formatDecimal(value)
