package com.humanperformcenter.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.app.FullScreenLoading
import com.humanperformcenter.ui.screens.ConfigurationScreen
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.shared.presentation.ui.DeleteUserState

@Composable
fun ConfigurationRoute(
    navController: NavHostController,
    userSessionViewModel: UserSessionViewModel
) {
    val deleteState by userSessionViewModel.deleteState.collectAsStateWithLifecycle()
    val user by userSessionViewModel.userData.collectAsStateWithLifecycle()
    val currentEmail = user?.email.orEmpty()

    val context = LocalContext.current

    val isLoggingOut by userSessionViewModel.isLoggingOut.collectAsStateWithLifecycle()

    LaunchedEffect(deleteState) {
        when (deleteState) {
            DeleteUserState.Success -> {
                navController.navigate(Welcome) {
                    popUpTo(Welcome) { inclusive = true }
                }
                userSessionViewModel.resetDeleteState()
            }
            is DeleteUserState.Error -> {
                Toast.makeText(
                    context,
                    "Error: ${(deleteState as DeleteUserState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                userSessionViewModel.resetDeleteState()
            }
            is DeleteUserState.NotFound -> {
                Toast.makeText(
                    context,
                    "Usuario no encontrado: ${(deleteState as DeleteUserState.NotFound).email}",
                    Toast.LENGTH_LONG
                ).show()
                userSessionViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize()) {
        ConfigurationScreen(
            navController = navController,
            onLogout = {
                userSessionViewModel.logout {
                    // Usamos una navegación que evite duplicados si el usuario pulsa dos veces rápido
                    navController.navigate(Welcome) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            },
            onDeleteAccount = {
                userSessionViewModel.deleteUser(currentEmail)
            },
            onChangePasswordRequested = {
                navController.navigate(ChangePassword)
            },
            isLoggingOut = isLoggingOut
        )

        if (deleteState is DeleteUserState.Loading) {
            FullScreenLoading()
        }
    }
}
