package com.rebootech.fruitlogix.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rebootech.fruitlogix.R

sealed class Route(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    object Home : Route("home", R.string.nav_home, R.drawable.ic_home)
    object Orders : Route("orders", R.string.nav_orders, R.drawable.ic_orders)
    object Fleet : Route("fleet", R.string.nav_fleet, R.drawable.ic_fleet)
    object Producers : Route("producers", R.string.nav_producers, R.drawable.ic_producers)
    object More : Route("more", R.string.nav_more, R.drawable.ic_more)
}

val bottomNavRoutes = listOf(
    Route.Home,
    Route.Orders,
    Route.Fleet,
    Route.Producers,
    Route.More
)

