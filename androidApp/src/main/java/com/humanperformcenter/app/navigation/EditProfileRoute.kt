package com.humanperformcenter.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.session.SessionManager
import com.humanperformcenter.ui.screens.EditProfileScreen
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.UpdateState

@Composable
fun EditProfileRoute(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(AppModule.userUseCase)
    )

    val userState: LoginResponse? by userViewModel.userData
        .collectAsState(initial = SessionManager.getCurrentUser())

    val updateState: UpdateState by userViewModel.updateState
        .observeAsState(initial = UpdateState.Idle)

    // Si no hay usuario en memoria, redirigimos a Login
    LaunchedEffect(userState) {
        if (userState == null) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.EditProfileScreen.route) { inclusive = true }
            }
        }
    }
    if (userState == null) return

    // Solo llamamos a EditProfileScreen, que ahora contiene su propio Scaffold
    EditProfileScreen(
        user = userState!!,
        updateState = updateState,
        onSave = { updatedUser ->
            userViewModel.updateUser(updatedUser)
        },
        navController = navController
    )
}
