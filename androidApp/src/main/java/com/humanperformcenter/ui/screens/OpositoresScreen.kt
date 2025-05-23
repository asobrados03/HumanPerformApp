package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@Composable
fun OpositoresScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Opositores - 2 entrenamientos a elegir/semana", 100),
        ServicioItem("Opositores - 3 entrenamientos fijos/semana", 100)
    )

    ServicioScreenTemplate(productos, navController)
}
