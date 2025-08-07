package com.humanperformcenter.app.navigation

import kotlin.reflect.KClass

data class NavItem(
    val route: KClass<*>,
    val title: String,
    val icon: Int
)