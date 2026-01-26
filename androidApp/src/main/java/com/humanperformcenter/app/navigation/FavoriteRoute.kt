package com.humanperformcenter.app.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.components.FullScreenTextLoading
import com.humanperformcenter.ui.screens.FavoriteScreen
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.shared.presentation.ui.CoachState
import com.humanperformcenter.shared.presentation.ui.GetPreferredCoachState

@Composable
fun FavoriteRoute(
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val coachState by userViewModel.coachesState.collectAsStateWithLifecycle()
    val getPreferredCoachState by userViewModel.getPreferredCoachState.collectAsStateWithLifecycle()
    val markFavoriteState by userViewModel.markFavoriteState.collectAsStateWithLifecycle()
    val userState by userViewModel.userData.collectAsStateWithLifecycle()
    val userId = userState?.id

    // Sólo para getCoaches() al montar la pantalla
    LaunchedEffect(Unit) {
        userViewModel.getCoaches()
    }

    // Observa userId y, cuando deje de ser null, pide el favorito
    LaunchedEffect(userId) {
        userId?.let { userViewModel.getPreferredCoach(userId = it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(getPreferredCoachState) {
        if (getPreferredCoachState is GetPreferredCoachState.Error) {
            snackbarHostState.showSnackbar((getPreferredCoachState as GetPreferredCoachState.Error).message)
            userViewModel.clearGetPreferredCoachState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (coachState) {
            is CoachState.Loading -> {
                FullScreenTextLoading("Cargando entrenadores...", paddingValues)
            }

            is CoachState.Error -> {
                val message = (coachState as CoachState.Error).message
                Text("Error: $message")
            }

            is CoachState.Success -> {
                val coaches = (coachState as CoachState.Success).coaches
                when (getPreferredCoachState) {
                    GetPreferredCoachState.Loading -> {
                        FullScreenTextLoading("Cargando entrenador favorito...", paddingValues)
                    }
                    is GetPreferredCoachState.Success -> {
                        // Ya tengo el favorito: lo paso a la pantalla
                        val favoriteId = (getPreferredCoachState as GetPreferredCoachState.Success).coachId
                        FavoriteScreen(
                            coaches = coaches,
                            preferredCoachId = favoriteId,
                            onSelect = { prof ->
                                userViewModel.markFavorite(prof.id, prof.service, userId)
                            },
                            markFavoriteState = markFavoriteState,
                            userViewModel = userViewModel,
                            userId = userId,
                            navController = navController
                        )
                    }
                    else -> {
                        // Estado Idle (aún no he recuperado el favorito) o Error (tras snackbar)
                        // Pinto igualmente la pantalla, sin ninguno seleccionado
                        FavoriteScreen(
                            coaches = coaches,
                            preferredCoachId = -1,
                            onSelect = { prof ->
                                userViewModel.markFavorite(prof.id, prof.service, userId)
                            },
                            markFavoriteState = markFavoriteState,
                            userViewModel = userViewModel,
                            userId = userId,
                            navController = navController,
                        )
                    }
                }
            }

            CoachState.Idle -> {
                FullScreenLoading()
            }
        }
    }
}
