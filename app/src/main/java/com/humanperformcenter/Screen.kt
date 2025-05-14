package com.humanperformcenter

sealed class Screen(val route: String) {
    data object RegisterScreen: Screen("register")
    data object LoginScreen: Screen("login")
    data object NewProductScreen: Screen("new_product")
    data object CalendarScreen: Screen("calendar")
    data object AddEditTransactionScreen: Screen("add")
    data object NewBlogScreen: Screen("new_blog")
    data object UserScreen: Screen("user")
    data object EditProfileScreen: Screen("edit_profile")
    data object FavoritesScreen: Screen("favorites")
    data object ChatScreen: Screen("chat")
    data object DocumentScreen: Screen("document")
    data object PaymentScreen: Screen("payment")
    data object ViewPaymentScreen: Screen("view_payment")
}
