package com.humanperformcenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.app.navigation.HireProduct
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.ServiceUiModel
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.viewmodel.DaySessionViewModelFactory
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel

@Composable
fun ServicesScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    serviceProductViewModel: ServiceProductViewModel
) {
    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val serviciosListos by serviceProductViewModel.hireViewUiState.collectAsStateWithLifecycle()

    LaunchedEffect(user) {
        user?.let {
            serviceProductViewModel.loadInitialData(it.id) // Una sola llamada al VM
            userViewModel.fetchUserBookings(it.id)
        }
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController = navController) },
        modifier = Modifier.fillMaxSize()

    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabs = listOf("Mis productos", "Contratar")
            var selectedTab by remember { mutableIntStateOf(0) }

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> MyProductsScreen(
                    viewModel = serviceProductViewModel,
                    navController = navController,
                    userViewModel = userViewModel,
                    daySessionViewModel = viewModel(
                        factory = DaySessionViewModelFactory(AppModule.daySessionUseCase)
                    ),
                    userId = user?.id ?: 0
                )
                1 -> HireView(
                    serviciosUi = serviciosListos,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun HireView(
    serviciosUi: List<ServiceUiModel>,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // Agregamos un poco de padding al final para que el último elemento no quede pegado
        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
    ) {
        items(
            items = serviciosUi,
            // Importante: Usar una clave estable ayuda a Compose a optimizar el scroll
            key = { it.service.id }
        ) { uiModel ->
            val servicio = uiModel.service

            AppCard(
                onClick = {
                    navController.navigate(HireProduct(servicio.id))
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // La URL ya viene procesada desde el ServiceUiModel
                    uiModel.fullImageUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = servicio.name,
                            modifier = Modifier
                                .size(69.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = servicio.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Solo leemos el booleano pre-calculado, sin lógica de búsqueda aquí
                        if (uiModel.isHired) {
                            Text(
                                text = "Ya tienes productos de este servicio",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ir al servicio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
