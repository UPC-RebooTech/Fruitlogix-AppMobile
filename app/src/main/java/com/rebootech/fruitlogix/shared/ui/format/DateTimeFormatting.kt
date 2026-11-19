package com.rebootech.fruitlogix.shared.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Locale of the current (in-app) language. */
@Composable
@ReadOnlyComposable
fun appLocale(): Locale = LocalConfiguration.current.locales.get(0)

/** Short time in the selected language ("10:48 AM" / "10:48"). */
@Composable
@ReadOnlyComposable
fun formatTime(dateTime: LocalDateTime): String =
    dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(appLocale()))

/** "Today, 10:48", "Yesterday, 16:30" or "29 Oct, 14:10", in the selected language. */
@Composable
fun formatRelativeDateTime(dateTime: LocalDateTime): String {
    val locale = appLocale()
    val time = formatTime(dateTime)
    val date = dateTime.toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> stringResource(R.string.common_today_at, time)
        today.minusDays(1) -> stringResource(R.string.common_yesterday_at, time)
        today.plusDays(1) -> stringResource(R.string.common_tomorrow_at, time)
        else -> stringResource(
            R.string.common_date_at,
            date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))
                .replaceFirstChar { it.titlecase(locale) },
            time
        )
    }
}

/** One decimal with the separator of the selected language (4.0 / 4,0). */
@Composable
@ReadOnlyComposable
fun formatDecimal(value: Double): String = String.format(appLocale(), "%.1f", value)

/** "Just now", "5 min ago", "2 h ago"; older than a day falls back to [formatRelativeDateTime]. */
@Composable
fun formatTimeAgo(dateTime: LocalDateTime): String {
    val minutes = java.time.Duration.between(dateTime, LocalDateTime.now()).toMinutes()
    return when {
        minutes < 1 -> stringResource(R.string.common_just_now)
        minutes < 60 -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.common_minutes_ago, minutes.toInt(), minutes.toInt()
        )
        minutes < 24 * 60 -> {
            val hours = (minutes / 60).toInt()
            androidx.compose.ui.res.pluralStringResource(R.plurals.common_hours_ago, hours, hours)
        }
        else -> formatRelativeDateTime(dateTime)
    }
}
