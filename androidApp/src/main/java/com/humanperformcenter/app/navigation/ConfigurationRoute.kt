package com.humanperformcenter.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.session.SessionManager
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.screens.ConfigurationScreen
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.state.DeleteUserState

@Composable
fun ConfigurationRoute(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    // 1) Estado de borrado
    val deleteState by userViewModel.deleteState.collectAsState()
    // 2) Email actual de sesión
    val currentEmail = SessionManager.getCurrentUser()?.email.orEmpty()

    // 3) Contexto para Toasts
    val context = LocalContext.current

    // 4) Side-effects en un único LaunchedEffect
    LaunchedEffect(deleteState) {
        when (deleteState) {
            DeleteUserState.Success -> {
                // Limpia sesión y vuelve al login
                SessionManager.clearUser()
                navController.navigate(Screen.WelcomeScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is DeleteUserState.Error -> {
                Toast.makeText(
                    context,
                    "Error: ${(deleteState as DeleteUserState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                userViewModel.resetDeleteState()
            }
            is DeleteUserState.NotFound -> {
                Toast.makeText(
                    context,
                    "Usuario no encontrado: ${(deleteState as DeleteUserState.NotFound).email}",
                    Toast.LENGTH_LONG
                ).show()
                userViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    // 5) Muestra la pantalla y el loading overlay si toca
    Box(Modifier.fillMaxSize()) {
        ConfigurationScreen(
            navController = navController,
            onLogout = {
                SessionManager.clearUser()
                navController.navigate(Screen.WelcomeScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            onDeleteAccount = {
                userViewModel.deleteUser(currentEmail)
            },
            onChangePasswordRequested = {
                navController.navigate(Screen.ChangePasswordScreen.route)
            }
        )

        if (deleteState is DeleteUserState.Loading) {
            FullScreenLoading()
        }
    }
}
