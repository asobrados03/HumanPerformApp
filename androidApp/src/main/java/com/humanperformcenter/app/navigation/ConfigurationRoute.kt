package com.humanperformcenter.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.screens.ConfigurationScreen
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.state.DeleteUserState
import kotlinx.coroutines.launch

@Composable
fun ConfigurationRoute(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    // 1) Estado de borrado
    val deleteState by userViewModel.deleteState.collectAsStateWithLifecycle()
    // 2) Email actual de sesión
    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val currentEmail = user?.email.orEmpty()

    // 3) Contexto para Toasts
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    // State to track logout operation
    var isLoggingOut by remember { mutableStateOf(false) }

    // 4) Side-effects en un único LaunchedEffect
    LaunchedEffect(deleteState) {
        when (deleteState) {
            DeleteUserState.Success -> {
                SecureStorage.clear()
                navController.navigate(Welcome) {
                    popUpTo(Welcome) { inclusive = true }
                }
                userViewModel.resetDeleteState()
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
                // Prevent multiple logout attempts
                if (isLoggingOut) return@ConfigurationScreen

                isLoggingOut = true
                scope.launch {
                    SecureStorage.clear()

                    navController.navigate(Welcome) {
                        popUpTo(Welcome) {
                            inclusive = true
                        }
                    }
                    isLoggingOut = false
                }

            },
            onDeleteAccount = {
                userViewModel.deleteUser(currentEmail)
            },
            onChangePasswordRequested = {
                navController.navigate(ChangePassword)
            }
        )

        if (deleteState is DeleteUserState.Loading) {
            FullScreenLoading()
        }
    }
}
