package com.rebootech.fruitlogix.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rebootech.fruitlogix.R

data class BottomNavTab(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
)

sealed class Route(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    object Home : Route("home", R.string.nav_home, R.drawable.ic_home)
    object Orders : Route("orders", R.string.nav_orders, R.drawable.ic_orders)
    object Fleet : Route("fleet", R.string.nav_fleet, R.drawable.ic_fleet)
    object Invoices : Route("invoices", R.string.nav_invoices, R.drawable.ic_invoices)
    object More : Route("more", R.string.nav_more, R.drawable.ic_more)
    object Producers : Route("producers", R.string.nav_producers, R.drawable.ic_producers)
}

fun Route.toNavTab(): BottomNavTab = BottomNavTab(route, labelRes, iconRes)

val primaryNavRoutes = listOf(
    Route.Home,
    Route.Orders,
    Route.Fleet,
    Route.Invoices
)

val bottomNavRoutes = primaryNavRoutes
