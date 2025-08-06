package com.humanperformcenter.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.humanperformcenter.app.navigation.HireProduct
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    daySessionViewModel: DaySessionViewModel
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
            userViewModel.fetchUserBookings(it.id)
        }
    }

    LaunchedEffect(allServices) {
        allServices.forEach { service ->
            serviceProductViewModel.loadServiceProducts(service.id)
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
                    daySessionViewModel = daySessionViewModel,
                    userId = user?.id ?: 0
                )
                1 -> HireView(
                    servicios = allServices,
                    viewModel = serviceProductViewModel,
                    navController = navController
                )
            }
        }
        // ⬇️ Mostrar cuestionario si está activo
        val cuestionarioActivo by daySessionViewModel.cuestionarioActivo.collectAsState()
        val preguntaActual by daySessionViewModel.preguntaActual.collectAsState()
        val contexto = LocalContext.current

        val preguntas = listOf(
            "¿Cómo calificarías tu sueño anoche?",
            "¿Qué nivel de energía sientes ahora mismo?",
            "¿Tienes dolor muscular o rigidez por entrenamientos anteriores?",
            "¿Cómo está tu nivel de estrés hoy?",
            "¿Cómo describirías tu estado de ánimo hoy?"
        )

        val opciones = listOf(
            listOf("😴 Muy malo", "😕 Malo", "😐 Regular", "🙂 Bueno", "😄 Excelente"),
            listOf("🔋 Muy cansado/a", "⚡ Cansado/a", "😐 Normal", "💪 Con energía", "🚀 A tope"),
            listOf("❌ No, me siento bien", "⚠️ Sí, ligera molestia", "🛑 Sí, dolor significativo"),
            listOf("😌 Muy bajo", "🙂 Bajo", "😐 Medio", "😬 Alto", "😵 Muy alto"),
            listOf("😞 Muy bajo", "😕 Bajo", "😐 Normal", "🙂 Bueno" ,"😄 Muy bueno")
        )

        val opcionesTexto = listOf(
            listOf("Muy malo", "Malo", "Regular", "Bueno", "Excelente"),
            listOf("Muy cansado/a", "Cansado/a", "Normal", "Con energía", "A tope"),
            listOf("No, me siento bien", "Sí, ligera molestia", "Sí, dolor significativo"),
            listOf("Muy bajo", "Bajo", "Medio", "Alto", "Muy alto"),
            listOf("Muy bajo", "Bajo", "Normal", "Bueno" ,"Muy bueno")
        )

        if (cuestionarioActivo && preguntaActual in preguntas.indices) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { daySessionViewModel.omitirFormulario() }) {
                        Text("Omitir")
                    }
                },
                title = { Text("Cuestionario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = preguntas[preguntaActual], fontWeight = FontWeight.Bold)
                        opciones[preguntaActual].forEachIndexed { index, opcion ->
                            Button(
                                onClick = { daySessionViewModel.responderPregunta(opcionesTexto[preguntaActual][index]) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(opcion)
                            }
                        }
                    }
                },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                )
            )
        }

    }
}

@Composable
fun HireView(
    servicios: List<ServiceAvailable>,
    viewModel: ServiceProductViewModel,
    navController: NavHostController
) {
    val productosContratados by viewModel.userProducts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(servicios) { servicio ->
            val imageUrl = servicio.image?.let { "https://apihuman.fransdata.com/api/service_images/$it" }
            val contratado = productosContratados.any { producto ->
                producto.serviceIds.contains(servicio.id)
            }
            AppCard(onClick = {
                navController.navigate(HireProduct(servicio.id))
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
