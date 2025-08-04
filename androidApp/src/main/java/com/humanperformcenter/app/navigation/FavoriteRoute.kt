package com.humanperformcenter.app.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.components.FullScreenTextLoading
import com.humanperformcenter.ui.screens.FavoritesScreen
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.state.CoachState
import com.humanperformcenter.ui.viewmodel.state.GetPreferredCoachState

@Composable
fun FavoritesRoute(
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val coachState by userViewModel.coachesState.collectAsState()
    val getPreferredCoachState by userViewModel.getPreferredCoachState.collectAsState()
    val markFavoriteState by userViewModel.markFavoriteState.collectAsState()
    val userState by userViewModel.userData.collectAsState()
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
                        FavoritesScreen(
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
                        FavoritesScreen(
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
