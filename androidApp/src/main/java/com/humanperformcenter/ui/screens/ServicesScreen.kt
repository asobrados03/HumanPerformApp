package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.ui.ServiceUiState
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.components.app.ErrorComponent
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.app.NavigationBar
import com.humanperformcenter.ui.components.service.ServicesShimmer
import kotlinx.coroutines.launch

@Composable
fun ServicesScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    stripeViewModel: StripeViewModel,
    serviceProductViewModel: ServiceProductViewModel
) {
    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val availableServicesState by serviceProductViewModel.serviceUiState.collectAsStateWithLifecycle()

    // Configuramos el estado del Pager (controla las páginas y la animación)
    val tabs = listOf("Mis productos", "Contratar")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user) {
        user?.let {
            userViewModel.fetchUserBookings(it.id)
            serviceProductViewModel.loadAllServices(it.id)
        }
    }

    Scaffold(
        topBar = { LogoAppBar(showBackArrow = false, onBackNavClicked = { navController.popBackStack() }) },
        bottomBar = { NavigationBar(navController = navController) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // TabRow sincronizado con el Pager
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage, // Lee la página actual automáticamente
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // 3. Al hacer clic, el pager se desplaza suavemente
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // contenedor de las pantallas
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> MyProductsScreen(
                        serviceProductViewModel = serviceProductViewModel,
                        stripeViewModel = stripeViewModel,
                        navController = navController,
                        userId = user?.id ?: 0
                    )
                    1 -> when (val state = availableServicesState) {
                        is ServiceUiState.Loading -> {
                            ServicesShimmer()
                        }
                        is ServiceUiState.Success -> {
                            HireView(
                                availableServices = state.services,
                                navController = navController
                            )
                        }
                        is ServiceUiState.Error -> {
                            // Aquí podrías poner un botón de "Reintentar"
                            ErrorComponent(
                                message = state.message,
                                onRetry = {
                                    user?.let {
                                        serviceProductViewModel.loadAllServices(it.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
