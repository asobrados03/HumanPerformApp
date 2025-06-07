package com.humanperformcenter.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humanperformcenter.app.SetStatusBarColor
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.session.SessionManager
import com.humanperformcenter.ui.screens.AlterGScreen
import com.humanperformcenter.ui.screens.CalendarScreen
import com.humanperformcenter.ui.screens.ChatScreen
import com.humanperformcenter.ui.screens.DocumentScreen
import com.humanperformcenter.ui.screens.EntrenamientoScreen
import com.humanperformcenter.ui.screens.FavoritesScreen
import com.humanperformcenter.ui.screens.FisioterapiaScreen
import com.humanperformcenter.ui.screens.LoginScreen
import com.humanperformcenter.ui.screens.MyProfileScreen
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
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModelFactory

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
            // Instanciamos el ViewModel usando el Factory:
            val userViewModel: UserViewModel = viewModel(
                factory = UserViewModelFactory(AppModule.userUseCase)
            )

            // Ahora, userViewModel.userData es un StateFlow<LoginResponse?>;
            // Use collectAsState para convertirlo en State<LoginResponse?> en Compose:
            val userState = userViewModel.userData.collectAsState()

            if (userState.value == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("login_screen") {
                        popUpTo(Screen.UserScreen) { inclusive = true }
                    }
                }
            } else {
                // -----------------------------------------------------------------
                // Hay un LoginResponse en memoria: pasémoslo a UserScreen:
                UserScreen(
                    navController = navController,
                    user = userState.value!!, // nunca será null aquí
                    onEditProfile = {
                        navController.navigate(Screen.EditProfileScreen.route)
                    },
                    onViewProfile = {
                        navController.navigate(Screen.MyProfileScreen.route)
                    },
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
        composable(Screen.EditProfileScreen.route) {
            EditProfileRoute(navController = navController)
        }
        composable(Screen.MyProfileScreen.route) {
            val userViewModel: UserViewModel = viewModel(
                factory = UserViewModelFactory(AppModule.userUseCase)
            )

            val userState: LoginResponse? by userViewModel.userData
                .collectAsState(initial = SessionManager.getCurrentUser())

            LaunchedEffect(userState) {
                if (userState == null) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.EditProfileScreen.route) { inclusive = true }
                    }
                }
            }
            if (userState == null) return@composable

            MyProfileScreen(
                user = userState!!,
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