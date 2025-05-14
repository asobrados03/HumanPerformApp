package com.humanperformcenter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humaneperformcenter.shared.data.model.User
import com.humanperformcenter.viewModels.SessionViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    sessionViewModel: SessionViewModel,
    navController: NavHostController,
    onPlaySound: (Int) -> Unit
) {
    // Configuración del color de la barra de estado
    SetStatusBarColor(
        statusBarColor = Color(0xFFB71C1C),
        navigationBarColor = Color(0xFFB71C1C)
    )

    NavHost(
        navController = navController,
        startDestination = if (sessionViewModel.isLoggedIn()) Screen.NewProductScreen.route
        else Screen.LoginScreen.route
    ) {
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(
                navController = navController,
                onRegistroExitoso = {
                    navController.navigate(Screen.LoginScreen) {
                        popUpTo(Screen.RegisterScreen) { inclusive = true }
                    }
                },
                onNavigateToLogin = { /* TODO */ }
            )
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.NewProductScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { /* TODO */ }
            )
        }
        composable(route = Screen.NewProductScreen.route) {
            NewProductScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                onPlaySound = onPlaySound
            )
        }
        composable(route = Screen.UserScreen.route) {
            UserScreen(
                navController = navController,
                user = User(
                    id = "",
                    name = "Usuario de Prueba",
                    lastName = "",
                    email = "prueba@ejemplo.com",
                    phone = "+34 616 171 171",
                    dateOfBirth = "",
                    gender = "",
                    profilePictureUrl = "",
                    balance = 0.0
                ),
                onEditProfile = { navController.navigate(Screen.EditProfileScreen.route) },
                onMenuClick = { option ->
                    when (option) {
                        MenuOption.FAVORITOS -> navController.navigate(Screen.FavoritesScreen.route)
                        MenuOption.CHAT      -> navController.navigate(Screen.ChatScreen.route)
                        MenuOption.DOCUMENTO -> navController.navigate(Screen.DocumentScreen.route)
                        MenuOption.PAGO      -> navController.navigate(Screen.PaymentScreen.route)
                        MenuOption.VER_PAGO  -> navController.navigate(Screen.ViewPaymentScreen.route)
                    }
                }
            )
        }
        composable(route = Screen.EditProfileScreen.route) {
            EditProfileScreen(
                user = User(
                    id = "",
                    name = "",
                    lastName = "",
                    email = "",
                    phone = "",
                    dateOfBirth = "",
                    gender = "",
                    profilePictureUrl = "",
                    balance = 0.0
                ),
                onSave = { updatedUser ->
                    // Aquí puedes actualizar el ViewModel, hacer llamada a API, etc.
                    navController.popBackStack() // Vuelve a la pantalla anterior
                }
            )
        }
        composable(Screen.FavoritesScreen.route) {
            FavoritesScreen(
                onSelect = { prof ->
                    // Aquí podrías navegar a "detalle de profe" u otra acción
                },
                navController   = navController
            )
        }
        composable(route = Screen.CalendarScreen.route){
            CalendarScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                onPlaySound = onPlaySound
            )
        }
        composable(route = Screen.NewBlogScreen.route) {
            NewBlogScreen(navController = navController)
        }
    }
}