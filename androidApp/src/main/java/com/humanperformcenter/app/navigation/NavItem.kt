package com.humanperformcenter.app.navigation

data class NavItem(
    val route: Any,
    val title: String,
    val icon: Int // se guarda como un ID de recurso (R.drawable)
)