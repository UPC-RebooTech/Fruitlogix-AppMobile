package com.rebootech.fruitlogix.navigation

sealed class Route(val route: String) {
    object Home : Route("home")
    object Orders : Route("orders")
    object Fleet : Route("fleet")
    object Producers : Route("producers")
    object More : Route("more")
}
