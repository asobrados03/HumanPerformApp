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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.app.navigation.*
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val user by userViewModel.userData.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            sessionViewModel.setUserCredentials(token = "", id = it.id)
            sessionViewModel.cargarServiciosPermitidos(it.id)
        }
    }

    val userServices by sessionViewModel.allowedServices.collectAsState()

    val services = listOf(
        1 to ("NUTRICIÓN" to R.drawable.nutricion),
        2 to ("ENTRENAMIENTO" to R.drawable.entrenamiento),
        3 to ("FISIOTERAPIA" to R.drawable.fisioterapia),
        4 to ("PILATES" to R.drawable.pilates),
        5 to ("PRESOTERAPIA" to R.drawable.presoterapia),
        6 to ("ENTRENAMIENTO OPOSITORES" to R.drawable.opositores),
        7 to ("SERVICIO DE TAQUILLA PERSONAL" to R.drawable.taquilla),
        8 to ("ALTER G Cinta antigravedad" to R.drawable.alterg)
    )

    val misProductos = services.filter { (id, _) -> id in userServices }
    val otrosProductos = services.filterNot { (id, _) -> id in userServices }

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
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (misProductos.isNotEmpty()) {
                item {
                    Text(
                        "Mis productos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(misProductos) { (_, service) ->
                    renderServiceCard(service, navController)
                }
            }

            item {
                Text(
                    "Otros productos disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(otrosProductos) { (_, service) ->
                renderServiceCard(service, navController)
            }
        }
    }
}

@Composable
fun renderServiceCard(service: Pair<String, Int>, navController: NavHostController) {
    val (name, iconRes) = service
    val isDarkTheme = isSystemInDarkTheme()

    AppCard(
        onClick = {
            when (name) {
                "NUTRICIÓN" -> navController.navigate(Nutricion)
                "ENTRENAMIENTO" -> navController.navigate(Entrenamiento)
                "FISIOTERAPIA" -> navController.navigate(Fisioterapia)
                "PILATES" -> navController.navigate(Pilates)
                "PRESOTERAPIA" -> navController.navigate(Presoterapia)
                "ENTRENAMIENTO OPOSITORES" -> navController.navigate(Opositores)
                "SERVICIO DE TAQUILLA PERSONAL" -> navController.navigate(Taquilla)
                "ALTER G Cinta antigravedad" -> navController.navigate(AlterG)
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isDarkTheme) {
                            Modifier
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        } else {
                            Modifier.padding(4.dp)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir"
            )
        }
    }
}