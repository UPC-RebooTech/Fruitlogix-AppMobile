package com.rebootech.fruitlogix.distributor.dashboard.presentation

import com.rebootech.fruitlogix.distributor.dashboard.domain.DashboardData
import java.time.LocalDate
import java.time.LocalTime

enum class DayPart { MORNING, AFTERNOON, EVENING }

data class HomeUiState(
    val isLoading: Boolean = true,
    /** First name of the signed-in distributor. */
    val userName: String = "",
    val today: LocalDate = LocalDate.now(),
    val dayPart: DayPart = dayPartOf(LocalTime.now()),
    val data: DashboardData? = null
)

fun dayPartOf(time: LocalTime): DayPart = when (time.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..18 -> DayPart.AFTERNOON
    else -> DayPart.EVENING
}
