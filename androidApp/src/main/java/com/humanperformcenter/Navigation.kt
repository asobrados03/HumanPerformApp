package com.humanperformcenter

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.ui.screens.AlterGScreen
import com.humanperformcenter.ui.screens.CalendarScreen
import com.humanperformcenter.ui.screens.ChatScreen
import com.humanperformcenter.ui.screens.DocumentScreen
import com.humanperformcenter.ui.screens.EditProfileScreen
import com.humanperformcenter.ui.screens.EntrenamientoScreen
import com.humanperformcenter.ui.screens.FavoritesScreen
import com.humanperformcenter.ui.screens.FisioterapiaScreen
import com.humanperformcenter.ui.screens.LoginScreen
import com.humanperformcenter.ui.screens.MenuOption
import com.humanperformcenter.ui.screens.NewBlogScreen
import com.humanperformcenter.ui.screens.NewProductScreen
import com.humanperformcenter.ui.screens.NutricionScreen
import com.humanperformcenter.ui.screens.OpositoresScreen
import com.humanperformcenter.ui.screens.PaymentScreen
import com.humanperformcenter.ui.screens.PilatesScreen
import com.humanperformcenter.ui.screens.PresoterapiaScreen
import com.humanperformcenter.ui.screens.RegisterScreen
import com.humanperformcenter.ui.screens.TaquillaScreen
import com.humanperformcenter.ui.screens.UserScreen
import com.humanperformcenter.ui.screens.ViewPaymentScreen
import com.humanperformcenter.ui.screens.WelcomeScreen
import com.humanperformcenter.viewModels.SessionViewModel

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
        else Screen.WelcomeScreen.route
    ) {
        composable(Screen.WelcomeScreen.route) {
            WelcomeScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.RegisterScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(
                onRegistroExitoso = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route){
                        popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.NewProductScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegisterScreen.route){
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable(route = Screen.NewProductScreen.route) {
            NewProductScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                onPlaySound = onPlaySound
            )
        }
        composable(Screen.EntrenamientoScreen.route) {
            EntrenamientoScreen(navController, sessionViewModel)
        }
        composable(Screen.NutricionScreen.route) {
            NutricionScreen(navController, sessionViewModel)
        }
        composable(Screen.FisioterapiaScreen.route) {
            FisioterapiaScreen(navController, sessionViewModel)
        }
        composable(Screen.PilatesScreen.route) {
            PilatesScreen(navController, sessionViewModel)
        }
        composable(Screen.PresoterapiaScreen.route) {
            PresoterapiaScreen(navController, sessionViewModel)
        }
        composable(Screen.OpositoresScreen.route) {
            OpositoresScreen(navController, sessionViewModel)
        }
        composable(Screen.TaquillaScreen.route) {
            TaquillaScreen(navController, sessionViewModel)
        }
        composable(Screen.AlterGScreen.route) {
            AlterGScreen(navController, sessionViewModel)
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
                        MenuOption.CHAT -> navController.navigate(Screen.ChatScreen.route)
                        MenuOption.DOCUMENTO -> navController.navigate(Screen.DocumentScreen.route)
                        MenuOption.PAGO -> navController.navigate(Screen.PaymentScreen.route)
                        MenuOption.VER_PAGO -> navController.navigate(Screen.ViewPaymentScreen.route)
                    }
                }
            )
        }
        composable(Screen.ChatScreen.route) {
            ChatScreen(navController = navController)
        }
        composable(Screen.DocumentScreen.route) {
            DocumentScreen(navController = navController)
        }
        composable(Screen.PaymentScreen.route) {
            PaymentScreen(navController = navController)
        }
        composable(Screen.ViewPaymentScreen.route) {
            ViewPaymentScreen(navController = navController)
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
                },
                navController = navController
            )
        }
        composable(Screen.FavoritesScreen.route) {
            FavoritesScreen(
                onSelect = { prof ->
                    // Aquí podrías navegar a "detalle de profe" u otra acción
                },
                navController = navController
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