package com.humanperformcenter.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.ui.screens.EditProfileScreen
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModelFactory

@Composable
fun EditProfileRoute(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(AppModule.userUseCase)
    )
    val loading by userViewModel.isLoading.collectAsState()
    val userState by userViewModel.userData.collectAsState()

    when {
        loading -> {
            // Mostrar spinner mientras isLoading == true
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        userState ==  null -> {
            LaunchedEffect(Unit) {
                navController.navigate(Login) {
                    popUpTo(EditProfile) { inclusive = true }
                }
            }
        }
        else -> {
            EditProfileScreen(
                user = userState!!,
                userViewModel = userViewModel,
                onSave = { updatedUser, profilePicBytes ->
                    userViewModel.updateUser(updatedUser, profilePicBytes)
                },
                onDeleteProfilePic = {userViewModel.deleteProfilePic(userState!!)},
                navController = navController
            )
        }
    }
}
