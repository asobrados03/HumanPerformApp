package com.humanperformcenter.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.ui.screens.EditProfileScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditProfileRoute(navController: NavHostController) {
    val userProfileViewModel: UserProfileViewModel = koinViewModel()
    val userSessionViewModel: UserSessionViewModel = koinViewModel()
    val loading by userSessionViewModel.isLoading.collectAsStateWithLifecycle()
    val userState by userSessionViewModel.userData.collectAsStateWithLifecycle()

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
                userProfileViewModel = userProfileViewModel,
                onSave = { updatedUser, profilePicBytes ->
                    userProfileViewModel.updateUser(updatedUser, profilePicBytes, userSessionViewModel.currentUserState())
                },
                onDeleteProfilePic = { userProfileViewModel.deleteProfilePic(userState!!, userSessionViewModel.currentUserState()) },
                navController = navController
            )
        }
    }
}
