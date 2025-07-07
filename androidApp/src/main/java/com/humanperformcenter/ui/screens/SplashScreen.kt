package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.NewProduct
import com.humanperformcenter.app.navigation.Splash
import com.humanperformcenter.app.navigation.Welcome
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    // 1️⃣ Observa el estado de login
    val isLoggedIn by sessionViewModel
        .isLoggedInFlow
        .collectAsState(initial = null)

    // 2️⃣ Mientras isLoggedIn sea null => mostramos Spinner
    FullScreenLoading()

    // AQUI PODRIAMOS LANZAR UNA CORRUTINA QUE REFRESCARA LOS TOKENS DE ACCESO (LLAMADA A /refresh)

    // 3️⃣ Cuando isLoggedIn cambia a true/false, navegamos y limpiamos backstack
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null) {
            navController.navigate(
                if (isLoggedIn == true) NewProduct else Welcome
            ) {
                popUpTo(Splash) { inclusive = true }
            }
        }
    }
}
