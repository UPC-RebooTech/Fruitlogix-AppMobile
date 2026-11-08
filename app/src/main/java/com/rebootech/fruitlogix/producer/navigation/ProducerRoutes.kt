package com.rebootech.fruitlogix.producer.navigation

import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.app.navigation.BottomNavTab
import com.rebootech.fruitlogix.app.navigation.MoreMenuItem
import com.rebootech.fruitlogix.producer.infrastructureIot.ProducerInfrastructureIotRoutes
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.paymentManagement.ProducerPaymentManagementRoutes
import com.rebootech.fruitlogix.producer.profilesManagement.ProducerProfilesManagementRoutes
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes

sealed class ProducerRoute(val route: String) {
    object Home : ProducerRoute("home")
    object Orders : ProducerRoute("orders")
    object Stock : ProducerRoute("stock")
    object Reports : ProducerRoute("reports")
}

val producerNavTabs = listOf(
    BottomNavTab(ProducerRoute.Home.route, R.string.producer_nav_home, R.drawable.ic_home),
    BottomNavTab(ProducerRoute.Orders.route, R.string.producer_nav_orders, R.drawable.ic_orders),
    BottomNavTab(ProducerRoute.Stock.route, R.string.producer_nav_stock, R.drawable.ic_dispatch_truck),
    BottomNavTab(ProducerRoute.Reports.route, R.string.producer_nav_reports, R.drawable.ic_shield_check)
)

val producerMoreItems = listOf(
    MoreMenuItem("producer_harvests", R.string.producer_nav_harvests, R.drawable.ic_producers, ProducerQualityControlRoutes.Harvests),
    MoreMenuItem("producer_payouts", R.string.producer_nav_payouts, R.drawable.ic_invoices, ProducerPaymentManagementRoutes.Payments),
    MoreMenuItem("producer_messaging", R.string.producer_nav_messaging, R.drawable.ic_contact_driver, ProducerOrderManagementRoutes.Inbox),
    MoreMenuItem("producer_notifications", R.string.producer_nav_notifications, R.drawable.ic_notifications, ProducerInfrastructureIotRoutes.Notifications),
    MoreMenuItem("producer_profile", R.string.producer_nav_profile, R.drawable.ic_producers, ProducerProfilesManagementRoutes.Profile)
)


