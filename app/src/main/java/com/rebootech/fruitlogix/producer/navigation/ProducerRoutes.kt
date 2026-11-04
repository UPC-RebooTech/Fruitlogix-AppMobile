package com.rebootech.fruitlogix.producer.navigation

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.app.navigation.BottomNavTab
import com.rebootech.fruitlogix.app.navigation.MoreMenuItem

object ProducerRoutes {
    const val Home = "producer_home"
    const val Orders = "producer_orders"
    const val Stock = "producer_stock"
    const val Reports = "producer_reports"

    // More menu destinations
    const val Harvests = "producer_harvests"
    const val Payouts = "producer_payouts"
    const val Messaging = "producer_messaging"
    const val Notifications = "producer_notifications"
    const val Profile = "producer_profile"
}

val producerNavTabs = listOf(
    BottomNavTab(ProducerRoutes.Home, R.string.producer_nav_home, R.drawable.ic_home),
    BottomNavTab(ProducerRoutes.Orders, R.string.producer_nav_orders, R.drawable.ic_orders),
    BottomNavTab(ProducerRoutes.Stock, R.string.producer_nav_stock, R.drawable.ic_dispatch_truck),
    BottomNavTab(ProducerRoutes.Reports, R.string.producer_nav_reports, R.drawable.ic_shield_check)
)

val producerMoreItems = listOf(
    MoreMenuItem("producer_harvests", R.string.producer_nav_harvests, R.drawable.ic_producers, ProducerRoutes.Harvests),
    MoreMenuItem("producer_payouts", R.string.producer_nav_payouts, R.drawable.ic_invoices, ProducerRoutes.Payouts),
    MoreMenuItem("producer_messaging", R.string.producer_nav_messaging, R.drawable.ic_contact_driver, ProducerRoutes.Messaging),
    MoreMenuItem("producer_notifications", R.string.producer_nav_notifications, R.drawable.ic_notifications, ProducerRoutes.Notifications),
    MoreMenuItem("producer_profile", R.string.producer_nav_profile, R.drawable.ic_producers, ProducerRoutes.Profile)
)
