package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.viewModels.SessionViewModel

@Composable
fun AlterGScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Sesión individual Alter G", 35),
        ServicioItem("Bono 5 sesiones Alter G", 165),
        ServicioItem("Bono 10 sesiones Alter G", 300),
        ServicioItem("1 sesión/semana (recurrente)", 120),
        ServicioItem("2 sesiones/semana (recurrente)", 220)
    )

    ServicioScreenTemplate(productos, navController)
}
