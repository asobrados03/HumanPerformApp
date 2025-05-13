package com.humanperformcenter

sealed class Screen(val route: String) {
    data object RegisterScreen: Screen("register")
    data object LoginScreen: Screen("login")
    data object DashboardScreen: Screen("dashboard")
    data object HistoryScreen: Screen("history")
    data object AddEditTransactionScreen: Screen("add")
    data object StaticsScreen: Screen("statics")
    data object UserScreen: Screen("user")
    data object EditProfileScreen: Screen("edit_profile")
    data object FavoritesScreen: Screen("favorites")
    data object ChatScreen: Screen("chat")
    data object DocumentScreen: Screen("document")
    data object PaymentScreen: Screen("payment")
    data object ViewPaymentScreen: Screen("view_payment")
}
