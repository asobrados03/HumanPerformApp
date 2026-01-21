package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.Service
import com.humanperformcenter.app.navigation.Splash
import com.humanperformcenter.app.navigation.Welcome
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.viewmodel.UserViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    // 1️⃣ Observa el estado de login
    val isLoggedIn by userViewModel.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = null)

    // 2️⃣ Mientras isLoggedIn sea null => mostramos Spinner
    FullScreenLoading()

    // 3️⃣ Cuando isLoggedIn cambia a true/false, navegamos y limpiamos backstack
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null) {
            navController.navigate(
                if (isLoggedIn == true) Service else Welcome
            ) {
                popUpTo(Splash) { inclusive = true }
            }
        }
    }
}
