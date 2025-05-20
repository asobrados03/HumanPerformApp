package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.viewModels.SessionViewModel

@Composable
fun PresoterapiaScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Sesión individual presoterapia", 20),
        ServicioItem("Bono 5 presoterapia", 90),
        ServicioItem("Bono 10 presoterapia", 160)
    )

    ServicioScreenTemplate(productos, navController)
}
