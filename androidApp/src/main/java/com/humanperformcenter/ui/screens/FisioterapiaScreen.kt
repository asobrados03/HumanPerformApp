package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@Composable
fun FisioterapiaScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Sesión fisioterapia", 38),
        ServicioItem("Bono 5 Nutrición - Fisioterapia", 175),
        ServicioItem("Bono 10 Nutri - Fisioterapia", 330),
        ServicioItem("Aprende a entrenar", 70)
    )

    ServicioScreenTemplate(productos, navController)
}
