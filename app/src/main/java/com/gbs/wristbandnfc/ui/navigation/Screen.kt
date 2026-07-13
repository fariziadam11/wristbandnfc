package com.gbs.wristbandnfc.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Scan : Screen("scan")
    data object Profile : Screen("profile")
    data object Wallet : Screen("wallet")
    data object Payment : Screen("payment/{uid}/{token}") {
        fun createRoute(uid: String, token: String) = "payment/$uid/$token"
    }
    data object Topup : Screen("topup/{uid}/{token}") {
        fun createRoute(uid: String, token: String) = "topup/$uid/$token"
    }
    data object Register : Screen("register")
    data object RegisterWrite : Screen("register_write/{uid}") {
        fun createRoute(uid: String) = "register_write/$uid"
    }
    data object Ticket : Screen("ticket")
    data object ValidateTicket : Screen("validate_ticket/{uid}/{token}") {
        fun createRoute(uid: String, token: String) = "validate_ticket/$uid/$token"
    }
    data object History : Screen("history")
    data object TransactionHistory : Screen("transaction_history/{wristbandId}") {
        fun createRoute(wristbandId: String) = "transaction_history/$wristbandId"
    }
}
