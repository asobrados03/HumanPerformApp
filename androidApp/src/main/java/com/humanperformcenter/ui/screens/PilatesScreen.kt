package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@Composable
fun PilatesScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Sesión individual de pilates", 30),
        ServicioItem("Bono 5 pilates", 130),
        ServicioItem("Bono 10 pilates", 250)
    )

    ServicioScreenTemplate(productos, navController)
}
