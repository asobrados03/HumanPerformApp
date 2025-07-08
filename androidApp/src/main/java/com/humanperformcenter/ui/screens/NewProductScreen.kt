package com.humanperformcenter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.R
import com.humanperformcenter.app.navigation.*
import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    serviceProductViewModel: ServiceProductViewModel
) {
    val user by userViewModel.userData.collectAsState()
    val allServices by serviceProductViewModel.allServices.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            sessionViewModel.setUserCredentials(token = "", id = it.id)
            sessionViewModel.cargarServiciosPermitidos(it.id)
            serviceProductViewModel.loadAllServices()
            serviceProductViewModel.loadUserProducts(it.id)
            allServices.forEach { service ->
                serviceProductViewModel.loadServiceProducts(service.id)
            }
        }
    }

    LaunchedEffect(allServices) {
        allServices.forEach { service ->
            serviceProductViewModel.loadServiceProducts(service.id)
        }
    }


    val userServices by sessionViewModel.allowedServices.collectAsState()
    val userServiceIds = userServices.map { it.id }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mis productos", "Contratar")

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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> MisProductosView(viewModel = serviceProductViewModel, navController = navController, userId = user?.id ?: 0)
                1 -> ContratarView(allServices, serviceProductViewModel,navController)
            }
        }
    }
}

@Composable
fun ContratarView(
    servicios: List<ServicioDispo>,
    viewModel: ServiceProductViewModel,
    navController: NavHostController
) {
    val productosContratados by viewModel.userProducts.collectAsState()
    val idsContratados = productosContratados.map { it.id }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(servicios) { servicio ->
            val imageUrl = servicio.image?.let { "http://163.172.71.195:8085/service_images/$it" }
            val contratado = productosContratados.any { producto ->
                producto.service_id.contains(servicio.id)
            }
            AppCard(onClick = {
                navController.navigate("servicio/${servicio.id}")
            }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    imageUrl?.let {
                        AsyncImage(
                            model = it,
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
                        Text(servicio.name, fontWeight = FontWeight.Bold)
                        if (contratado) {
                            Text("Ya tienes productos de este servicio", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ir"
                    )
                }
            }
        }
    }
}
