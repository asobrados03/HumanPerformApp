package com.humanperformcenter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.humanperformcenter.data.User
import com.humanperformcenter.viewModels.BudgetViewModel
import com.humanperformcenter.viewModels.CategoryViewModel
import com.humanperformcenter.viewModels.TransactionViewModel
import com.humanperformcenter.viewModels.SessionViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    transactionViewModel: TransactionViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
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
        startDestination = Screen.DashboardScreen.route
    ) {
        composable(route = Screen.DashboardScreen.route) {
            DashboardScreen(navController, transactionViewModel)
        }
        composable(route = Screen.UserScreen.route) {
            UserScreen(
                navController   = navController,
                userName        = "Usuario de Prueba",            // cadena vacía
                userEmail       = "prueba@ejemplo.com",            // cadena vacía
                userPhone       = "+34 616 171 171",            // cadena vacía
                userPhotoUrl    = null,          // URL nula
                balance         = 0.0,           // saldo a cero
                onEditProfile   = { navController.navigate(Screen.EditProfileScreen.route) },
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
                user = User("", "", "", "", "", "", "", "", 0.0),
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
        composable(route = Screen.AddEditBudgetScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            )) { entry ->
            val id = if (entry.arguments != null) entry.arguments!!.getLong("id") else 0L
            AddEditDetailBudgetView(
                id = id,
                budgetViewModel = budgetViewModel,
                categoryViewModel = categoryViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }
        composable(route = Screen.AddEditTransactionScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            )) { entry ->
            val id = if (entry.arguments != null) entry.arguments!!.getLong("id") else 0L
            AddEditDetailTransactionView(
                id = id,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                navController = navController
            )
        }
        composable(route = Screen.HistoryScreen.route){
            CalendarScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                categoryViewModel = categoryViewModel,
                onPlaySound = onPlaySound
            )
        }
        composable(route = Screen.StaticsScreen.route) {
            StaticsScreen(
                navController = navController,
                transactionViewModel = transactionViewModel
            )
        }
    }
}