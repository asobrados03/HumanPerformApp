package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@Composable
fun TaquillaScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val productos = listOf(
        ServicioItem("Servicio de taquilla personal", 10)
    )

    ServicioScreenTemplate(productos, navController)
}
